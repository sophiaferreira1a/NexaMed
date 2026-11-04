package br.pucminas.hospital.service;

import br.pucminas.hospital.dto.AtualizarAtendimentoRequest;
import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.AtEmergencia;
import br.pucminas.hospital.model.Atendimento;
import br.pucminas.hospital.model.ConEspecializada;
import br.pucminas.hospital.model.ConRotina;
import br.pucminas.hospital.model.ProCirurgico;
import br.pucminas.hospital.model.Profissional;
import br.pucminas.hospital.model.enums.SituacaoConsulta;
import br.pucminas.hospital.model.enums.SituacaoEmergencia;
import br.pucminas.hospital.model.enums.SituacaoProcedimento;
import br.pucminas.hospital.repository.AtendimentoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Regras comuns a consultas (rotina, especializada, emergência) e procedimentos cirúrgicos. */
@Service
public class AtendimentoService {

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

        atendimento.validarCondicoes(profissional);
        verificarConflito(atendimento.getProfissionalId(), atendimento.getData(), atendimento.getHorario(), null);

        atendimento.setId(null);
        return repository.save(atendimento);
    }

    // ---------- consultas ----------

    public List<Atendimento> listarConsultas(String pacienteId) {
        List<Atendimento> todos = pacienteId == null ? repository.findAll() : repository.findByPacienteId(pacienteId);
        return todos.stream().filter(a -> !(a instanceof ProCirurgico)).toList();
    }

    public Atendimento buscarConsulta(String id) {
        Atendimento a = buscar(id);
        if (a instanceof ProCirurgico) {
            throw new RecursoNaoEncontradoException("Consulta não encontrada: " + id);
        }
        return a;
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

    // ---------- operações comuns ----------

    public Atendimento atualizar(Atendimento atendimento, AtualizarAtendimentoRequest req) {
        LocalDate novaData = req.data() != null ? req.data() : atendimento.getData();
        LocalTime novoHorario = req.horario() != null ? req.horario() : atendimento.getHorario();

        if (!novaData.equals(atendimento.getData()) || !novoHorario.equals(atendimento.getHorario())) {
            verificarConflito(atendimento.getProfissionalId(), novaData, novoHorario, atendimento.getId());
        }
        atendimento.setData(novaData);
        atendimento.setHorario(novoHorario);
        if (req.observacoes() != null) {
            atendimento.setObservacoes(req.observacoes());
        }
        return repository.save(atendimento);
    }

    public Atendimento alterarSituacao(Atendimento atendimento, String valor) {
        if (atendimento instanceof ConRotina rotina) {
            rotina.alterarSituacao(converter(SituacaoConsulta.class, valor));
        } else if (atendimento instanceof ConEspecializada especializada) {
            especializada.alterarSituacao(converter(SituacaoConsulta.class, valor));
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
        boolean conflito = repository.findByProfissionalIdAndDataAndHorario(profissionalId, data, horario)
                .stream()
                .anyMatch(a -> a.ocupaHorario() && !a.getId().equals(ignorarId));
        if (conflito) {
            throw new RegraNegocioException("O profissional já possui um atendimento em " + data + " às " + horario + ".");
        }
    }

    private <E extends Enum<E>> E converter(Class<E> tipo, String valor) {
        try {
            return Enum.valueOf(tipo, valor.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new RegraNegocioException("Situação inválida: " + valor
                    + ". Valores aceitos: " + java.util.Arrays.toString(tipo.getEnumConstants()));
        }
    }
}
