package br.pucminas.hospital.service;

import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.Profissional;
import br.pucminas.hospital.model.enums.Especialidade;
import br.pucminas.hospital.repository.AtendimentoRepository;
import br.pucminas.hospital.repository.InternacaoRepository;
import br.pucminas.hospital.repository.ProfissionalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfissionalService {

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

    public List<Profissional> listar(Especialidade especialidade) {
        return especialidade == null ? repository.findAll() : repository.buscarPorEspecialidade(especialidade);
    }

    public Profissional buscar(String id) {
        if (id == null) {
            throw new RecursoNaoEncontradoException("Profissional não informado.");
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
}
