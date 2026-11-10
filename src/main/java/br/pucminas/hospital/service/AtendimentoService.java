package br.pucminas.hospital.service;

import br.pucminas.hospital.dto.AtualizarAtendimentoRequest;
import br.pucminas.hospital.dto.EncaminhamentoRequest;
import br.pucminas.hospital.dto.ReagendamentoRequest;
import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.AtEmergencia;
import br.pucminas.hospital.model.Atendimento;
import br.pucminas.hospital.model.ClinicoGeral;
import br.pucminas.hospital.model.ConEspecializada;
import br.pucminas.hospital.model.ConRotina;
import br.pucminas.hospital.model.Consulta;
import br.pucminas.hospital.model.ProCirurgico;
import br.pucminas.hospital.model.Profissional;
import br.pucminas.hospital.model.enums.SituacaoConsulta;
import br.pucminas.hospital.model.enums.SituacaoEmergencia;
import br.pucminas.hospital.model.enums.SituacaoProcedimento;
import br.pucminas.hospital.repository.AtendimentoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Regras comuns a consultas (rotina, especializada, emergência) e procedimentos cirúrgicos. */
@Service
public class AtendimentoService {

    /** Valores aceitos no filtro "tipo", iguais aos usados no campo "tipo" do JSON. */
    private static final Map<String, Class<? extends Atendimento>> TIPOS = Map.of(
            "ROTINA", ConRotina.class,
            "ESPECIALIZADA", ConEspecializada.class,
            "EMERGENCIA", AtEmergencia.class,
            "CIRURGICO", ProCirurgico.class);

    /** Emergências: prioridade mais alta primeiro; empate pela ordem de chegada. */
    private static final Comparator<AtEmergencia> ORDEM_DE_ATENDIMENTO = Comparator
            .comparing((AtEmergencia e) -> e.getPrioridade().ordinal()).reversed()
            .thenComparing(Atendimento::getData)
            .thenComparing(Atendimento::getHorario);

    private final AtendimentoRepository repository;
    private final PacienteService pacienteService;
    private final ProfissionalService profissionalService;

    public AtendimentoService(AtendimentoRepository repository,
                              PacienteService pacienteService,
                              ProfissionalService profissionalService) {
        this.repository = repository;
        this.pacienteService = pacienteService;
        this.profissionalService = profissionalService;
    }

    // ---------- criação ----------

    public Atendimento criarConsulta(Atendimento atendimento) {
        if (atendimento instanceof ProCirurgico) {
            throw new RegraNegocioException(
                    "Procedimentos cirúrgicos são cadastrados em /profissionais/{id}/procedimentos.");
        }
        return criar(atendimento);
    }

    public Atendimento criarProcedimento(String profissionalId, Atendimento atendimento) {
        if (!(atendimento instanceof ProCirurgico)) {
            throw new RegraNegocioException("O corpo da requisição deve ter \"tipo\": \"CIRURGICO\".");
        }
        atendimento.setProfissionalId(profissionalId);
        return criar(atendimento);
    }

    private Atendimento criar(Atendimento atendimento) {
        pacienteService.buscar(atendimento.getPacienteId());
        Profissional profissional = profissionalService.buscar(atendimento.getProfissionalId());

        // Cada modalidade valida as próprias condições (polimorfismo).
        atendimento.validarCondicoes(profissional);
        verificarConflito(atendimento.getProfissionalId(), atendimento.getData(), atendimento.getHorario(), null);

        atendimento.setId(null);
        return repository.save(atendimento);
    }

    // ---------- consultas ----------

    /** Lista consultas (sem procedimentos), com filtros opcionais por paciente, modalidade e situação. */
    public List<Atendimento> listarConsultas(String pacienteId, String tipo, String situacao) {
        List<Atendimento> todos = pacienteId == null ? repository.findAll() : repository.findByPacienteId(pacienteId);
        Class<? extends Atendimento> classe = tipo == null ? null : converterTipo(tipo);
        if (classe == ProCirurgico.class) {
            throw new RegraNegocioException("Procedimentos cirúrgicos são listados em /profissionais/{id}/procedimentos.");
        }
        return todos.stream()
                .filter(a -> !(a instanceof ProCirurgico))
                .filter(a -> classe == null || classe.isInstance(a))
                .filter(a -> situacao == null || a.situacaoAtual().name().equalsIgnoreCase(situacao.trim()))
                .toList();
    }

    /**
     * Atendimentos de emergência, do mais urgente para o menos urgente.
     * Com "situacao" dá para separar os que aguardam avaliação dos já iniciados ou concluídos.
     */
    public List<AtEmergencia> listarEmergencias(String situacao) {
        SituacaoEmergencia filtro = situacao == null ? null : converter(SituacaoEmergencia.class, situacao);
        return repository.findAll().stream()
                .filter(a -> a instanceof AtEmergencia)
                .map(a -> (AtEmergencia) a)
                .filter(e -> filtro == null || e.getSituacao() == filtro)
                .sorted(ORDEM_DE_ATENDIMENTO)
                .toList();
    }

    public Atendimento buscarConsulta(String id) {
        Atendimento a = buscar(id);
        if (a instanceof ProCirurgico) {
            throw new RecursoNaoEncontradoException("Consulta não encontrada: " + id);
        }
        return a;
    }

    /**
     * Uma consulta cancelada ou com falta não volta a ser agendada nem concluída:
     * é criado um novo agendamento, ligado ao anterior por "reagendamentoDeId".
     */
    public Atendimento reagendar(String consultaId, ReagendamentoRequest req) {
        Atendimento original = buscarConsulta(consultaId);
        if (!(original instanceof Consulta consulta)) {
            throw new RegraNegocioException("Somente consultas de rotina ou especializadas podem ser reagendadas.");
        }
        return criar(consulta.criarReagendamento(req.data(), req.horario()));
    }

    /**
     * Encaminhamento do clínico geral: a partir de uma consulta de rotina em atendimento ou concluída,
     * cria uma consulta especializada para o mesmo paciente com um especialista da área pedida.
     */
    public Atendimento encaminhar(String consultaId, EncaminhamentoRequest req) {
        Atendimento origem = buscarConsulta(consultaId);
        if (!(origem instanceof ConRotina rotina)) {
            throw new RegraNegocioException("Só é possível encaminhar a partir de uma consulta de rotina.");
        }
        if (!(profissionalService.buscar(rotina.getProfissionalId()) instanceof ClinicoGeral)) {
            throw new RegraNegocioException("Somente o clínico geral pode encaminhar para atendimento especializado.");
        }
        if (!rotina.permiteEncaminhamento()) {
            throw new RegraNegocioException("O encaminhamento só pode ser feito durante ou após a consulta"
                    + " (situação atual: " + rotina.getSituacao() + ").");
        }

        ConEspecializada especializada = new ConEspecializada();
        especializada.setPacienteId(rotina.getPacienteId());
        especializada.setProfissionalId(req.profissionalId());
        especializada.setEspecialidade(req.especialidade());
        especializada.setData(req.data());
        especializada.setHorario(req.horario());
        especializada.setConsultaOrigemId(rotina.getId());
        return criar(especializada);
    }

    // ---------- procedimentos ----------

    public List<Atendimento> listarProcedimentos(String profissionalId) {
        profissionalService.buscar(profissionalId);
        return repository.findByProfissionalId(profissionalId).stream()
                .filter(a -> a instanceof ProCirurgico).toList();
    }

    public Atendimento buscarProcedimento(String profissionalId, String id) {
        Atendimento a = buscar(id);
        if (!(a instanceof ProCirurgico) || !profissionalId.equals(a.getProfissionalId())) {
            throw new RecursoNaoEncontradoException("Procedimento não encontrado: " + id);
        }
        return a;
    }

    // ---------- atendimentos de um profissional ----------

    /** Consultas e emergências atendidas por um profissional, com filtro opcional de situação. */
    public List<Atendimento> listarConsultasDoProfissional(String profissionalId, String situacao) {
        profissionalService.buscar(profissionalId);
        return repository.findByProfissionalId(profissionalId).stream()
                .filter(a -> !(a instanceof ProCirurgico))
                .filter(a -> situacao == null || a.situacaoAtual().name().equalsIgnoreCase(situacao.trim()))
                .toList();
    }

    // ---------- operações comuns ----------

    public Atendimento atualizar(Atendimento atendimento, AtualizarAtendimentoRequest req) {
        LocalDate novaData = req.data() != null ? req.data() : atendimento.getData();
        LocalTime novoHorario = req.horario() != null ? req.horario() : atendimento.getHorario();

        if (!novaData.equals(atendimento.getData()) || !novoHorario.equals(atendimento.getHorario())) {
            verificarConflito(atendimento.getProfissionalId(), novaData, novoHorario, atendimento.getId());
            atendimento.alterarAgendamento(novaData, novoHorario);
        }
        if (req.observacoes() != null) {
            atendimento.registrarObservacoes(req.observacoes());
        }
        if (req.temDadosCirurgicos()) {
            if (!(atendimento instanceof ProCirurgico cirurgico)) {
                throw new RegraNegocioException("Descrição, tipo de cirurgia e observações pré e pós-operatórias"
                        + " valem apenas para procedimentos cirúrgicos.");
            }
            if (req.descricaoProcedimento() != null || req.tipoCirurgia() != null) {
                cirurgico.alterarDadosDoProcedimento(req.descricaoProcedimento(), req.tipoCirurgia());
            }
            if (req.observacoesPreOp() != null) {
                cirurgico.registrarObservacoesPreOp(req.observacoesPreOp());
            }
            if (req.observacoesPosOp() != null) {
                cirurgico.registrarObservacoesPosOp(req.observacoesPosOp());
            }
        }
        return repository.save(atendimento);
    }

    public Atendimento alterarSituacao(Atendimento atendimento, String valor) {
        if (atendimento instanceof Consulta consulta) {
            consulta.alterarSituacao(converter(SituacaoConsulta.class, valor));
        } else if (atendimento instanceof AtEmergencia emergencia) {
            emergencia.alterarSituacao(converter(SituacaoEmergencia.class, valor));
        } else if (atendimento instanceof ProCirurgico cirurgico) {
            cirurgico.alterarSituacao(converter(SituacaoProcedimento.class, valor));
        }
        return repository.save(atendimento);
    }

    // ---------- auxiliares ----------

    private Atendimento buscar(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento não encontrado: " + id));
    }

    /** Regra 3: um profissional não pode ter dois atendimentos no mesmo horário. */
    private void verificarConflito(String profissionalId, LocalDate data, LocalTime horario, String ignorarId) {
        if (!profissionalService.estaDisponivel(profissionalId, data, horario, ignorarId)) {
            throw new RegraNegocioException("O profissional já possui um atendimento em " + data + " às " + horario + ".");
        }
    }

    private Class<? extends Atendimento> converterTipo(String tipo) {
        Class<? extends Atendimento> classe = TIPOS.get(tipo.trim().toUpperCase());
        if (classe == null) {
            throw new RegraNegocioException("Tipo inválido: " + tipo + ". Valores aceitos: " + TIPOS.keySet());
        }
        return classe;
    }

    private <E extends Enum<E>> E converter(Class<E> tipo, String valor) {
        try {
            return Enum.valueOf(tipo, valor.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new RegraNegocioException("Situação inválida: " + valor
                    + ". Valores aceitos: " + Arrays.toString(tipo.getEnumConstants()));
        }
    }
}
