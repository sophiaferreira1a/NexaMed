package br.pucminas.hospital.service;

import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.exception.RequisicaoInvalidaException;
import br.pucminas.hospital.model.Cirurgiao;
import br.pucminas.hospital.model.ClinicoGeral;
import br.pucminas.hospital.model.Especialista;
import br.pucminas.hospital.model.Profissional;
import br.pucminas.hospital.model.enums.Especialidade;
import br.pucminas.hospital.repository.AtendimentoRepository;
import br.pucminas.hospital.repository.InternacaoRepository;
import br.pucminas.hospital.repository.ProfissionalRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
public class ProfissionalService {

    /** Valores aceitos no filtro "tipo", iguais aos usados no campo "tipo" do JSON. */
    private static final Map<String, Class<? extends Profissional>> TIPOS = Map.of(
            "CLINICO_GERAL", ClinicoGeral.class,
            "ESPECIALISTA", Especialista.class,
            "CIRURGIAO", Cirurgiao.class);

    private final ProfissionalRepository repository;
    private final AtendimentoRepository atendimentoRepository;
    private final InternacaoRepository internacaoRepository;

    public ProfissionalService(ProfissionalRepository repository,
                               AtendimentoRepository atendimentoRepository,
                               InternacaoRepository internacaoRepository) {
        this.repository = repository;
        this.atendimentoRepository = atendimentoRepository;
        this.internacaoRepository = internacaoRepository;
    }

    public Profissional criar(Profissional profissional) {
        if (repository.existsByRegistroProfissional(profissional.getRegistroProfissional())) {
            throw new RegraNegocioException("Já existe um profissional com o registro "
                    + profissional.getRegistroProfissional() + ".");
        }
        profissional.setId(null);
        return repository.save(profissional);
    }

    /** Lista profissionais, com filtros opcionais por categoria e por área de atuação (especialidade). */
    public List<Profissional> listar(String tipo, Especialidade especialidade) {
        Class<? extends Profissional> classe = tipo == null ? null : converterTipo(tipo);
        List<Profissional> base = especialidade == null ? repository.findAll() : repository.buscarPorEspecialidade(especialidade);
        return base.stream()
                .filter(p -> classe == null || classe.isInstance(p))
                .toList();
    }

    /**
     * Profissionais livres numa data e horário, com os mesmos filtros de {@link #listar}.
     * Ex.: especialistas em CARDIOLOGIA disponíveis em 2026-11-20 às 14:00.
     */
    public List<Profissional> listarDisponiveis(String tipo, Especialidade especialidade, LocalDate data, LocalTime horario) {
        if (data == null || horario == null) {
            throw new RegraNegocioException("Informe a data e o horário para consultar a disponibilidade.");
        }
        return listar(tipo, especialidade).stream()
                .filter(p -> estaDisponivel(p.getId(), data, horario, null))
                .toList();
    }

    /**
     * Verifica a disponibilidade do profissional (método verificarDisponibilidade do diagrama de classes):
     * ele está livre se não tem, naquele horário, nenhum atendimento que ainda ocupe a agenda.
     * Atendimentos cancelados ou com falta liberam o horário.
     */
    public boolean estaDisponivel(String profissionalId, LocalDate data, LocalTime horario, String ignorarAtendimentoId) {
        return atendimentoRepository.findByProfissionalIdAndDataAndHorario(profissionalId, data, horario)
                .stream()
                .noneMatch(a -> a.ocupaHorario() && !a.getId().equals(ignorarAtendimentoId));
    }

    public Profissional buscar(String id) {
        if (id == null) {
            throw new RequisicaoInvalidaException("Profissional não informado.");
        }
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional não encontrado: " + id));
    }

    public Profissional atualizar(String id, Profissional novo) {
        Profissional existente = buscar(id);
        if (!existente.getRegistroProfissional().equals(novo.getRegistroProfissional())
                && repository.existsByRegistroProfissional(novo.getRegistroProfissional())) {
            throw new RegraNegocioException("Já existe um profissional com o registro "
                    + novo.getRegistroProfissional() + ".");
        }
        // Os atendimentos já registrados dependem da categoria e da especialidade do profissional
        // (ex.: consulta especializada exige especialista da mesma área), então elas não podem mudar.
        if (atendimentoRepository.existsByProfissionalId(id)) {
            if (existente.getClass() != novo.getClass()) {
                throw new RegraNegocioException("Não é possível mudar a categoria de um profissional com atendimentos registrados.");
            }
            if (existente instanceof Especialista antigo && novo instanceof Especialista atual
                    && antigo.getEspecialidade() != atual.getEspecialidade()) {
                throw new RegraNegocioException("Não é possível mudar a especialidade de um profissional com atendimentos registrados.");
            }
        }
        novo.setId(existente.getId());
        return repository.save(novo);
    }

    public void deletar(String id) {
        buscar(id);
        if (atendimentoRepository.existsByProfissionalId(id) || internacaoRepository.existsByProfissionalId(id)) {
            throw new RegraNegocioException("Profissional possui atendimentos registrados e não pode ser removido.");
        }
        repository.deleteById(id);
    }

    private Class<? extends Profissional> converterTipo(String tipo) {
        Class<? extends Profissional> classe = TIPOS.get(tipo.trim().toUpperCase());
        if (classe == null) {
            throw new RegraNegocioException("Tipo inválido: " + tipo + ". Valores aceitos: " + TIPOS.keySet());
        }
        return classe;
    }
}
