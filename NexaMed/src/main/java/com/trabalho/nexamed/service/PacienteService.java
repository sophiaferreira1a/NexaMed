package br.pucminas.hospital.service;

import br.pucminas.hospital.dto.HistoricoMedico;
import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.repository.AtendimentoRepository;
import br.pucminas.hospital.repository.InternacaoRepository;
import br.pucminas.hospital.repository.PacienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PacienteService {

    private final PacienteRepository repository;
    private final AtendimentoRepository atendimentoRepository;
    private final InternacaoRepository internacaoRepository;

    public PacienteService(PacienteRepository repository,
                           AtendimentoRepository atendimentoRepository,
                           InternacaoRepository internacaoRepository) {
        this.repository = repository;
        this.atendimentoRepository = atendimentoRepository;
        this.internacaoRepository = internacaoRepository;
    }

    public Paciente criar(Paciente paciente) {
        if (repository.existsByCpf(paciente.getCpf())) {
            throw new RegraNegocioException("Já existe um paciente com o CPF " + paciente.getCpf() + ".");
        }
        paciente.setId(null);
        return repository.save(paciente);
    }

    public List<Paciente> listar() {
        return repository.findAll();
    }

    public Paciente buscar(String id) {
        if (id == null) {
            throw new RecursoNaoEncontradoException("Paciente não informado.");
        }
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado: " + id));
    }

    public Paciente atualizar(String id, Paciente novo) {
        Paciente existente = buscar(id);
        if (!existente.getCpf().equals(novo.getCpf()) && repository.existsByCpf(novo.getCpf())) {
            throw new RegraNegocioException("Já existe um paciente com o CPF " + novo.getCpf() + ".");
        }
        novo.setId(existente.getId());
        return repository.save(novo);
    }

    public void deletar(String id) {
        buscar(id);
        // Regra 6: o histórico de consultas e internações deve ser mantido.
        if (atendimentoRepository.existsByPacienteId(id) || internacaoRepository.existsByPacienteId(id)) {
            throw new RegraNegocioException("Paciente possui histórico de atendimentos e não pode ser removido.");
        }
        repository.deleteById(id);
    }

    public HistoricoMedico historico(String id) {
        Paciente paciente = buscar(id);
        return new HistoricoMedico(paciente,
                atendimentoRepository.findByPacienteId(id),
                internacaoRepository.findByPacienteId(id));
    }
}
