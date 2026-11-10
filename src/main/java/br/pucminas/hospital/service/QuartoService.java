package br.pucminas.hospital.service;

import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.exception.RequisicaoInvalidaException;
import br.pucminas.hospital.model.Quarto;
import br.pucminas.hospital.model.enums.SituacaoQuarto;
import br.pucminas.hospital.model.enums.TipoQuarto;
import br.pucminas.hospital.repository.QuartoRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuartoService {

    private final QuartoRepository repository;

    public QuartoService(QuartoRepository repository) {
        this.repository = repository;
    }

    public Quarto criar(Quarto quarto) {
        if (repository.existsByNumId(quarto.getNumId())) {
            throw new RegraNegocioException("Já existe um quarto com o número " + quarto.getNumId() + ".");
        }
        quarto.validarTipo();
        quarto.setId(null);
        quarto.setOcupacaoAtual(0);
        quarto.atualizarSituacao();
        return repository.save(quarto);
    }

    /** Lista quartos, com filtros opcionais por tipo (ENFERMARIA, APARTAMENTO, UTI) e por disponibilidade. */
    public List<Quarto> listar(TipoQuarto tipo, boolean apenasDisponiveis) {
        if (tipo == null) {
            return apenasDisponiveis ? repository.findBySituacao(SituacaoQuarto.DISPONIVEL) : repository.findAll();
        }
        return apenasDisponiveis
                ? repository.findByTipoAndSituacao(tipo, SituacaoQuarto.DISPONIVEL)
                : repository.findByTipo(tipo);
    }

    public Quarto buscar(String id) {
        if (id == null) {
            throw new RequisicaoInvalidaException("Quarto não informado.");
        }
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quarto não encontrado: " + id));
    }

    public Quarto atualizar(String id, Quarto novo) {
        Quarto existente = buscar(id);
        if (!existente.getNumId().equals(novo.getNumId()) && repository.existsByNumId(novo.getNumId())) {
            throw new RegraNegocioException("Já existe um quarto com o número " + novo.getNumId() + ".");
        }
        if (novo.getCapacidadeMax() < existente.getOcupacaoAtual()) {
            throw new RegraNegocioException("A nova capacidade é menor que a ocupação atual ("
                    + existente.getOcupacaoAtual() + ").");
        }
        existente.setNumId(novo.getNumId());
        existente.setAndar(novo.getAndar());
        existente.setCapacidadeMax(novo.getCapacidadeMax());
        existente.setTipo(novo.getTipo());
        existente.validarTipo();
        existente.atualizarSituacao();
        return salvar(existente);
    }

    public void deletar(String id) {
        Quarto quarto = buscar(id);
        if (quarto.getOcupacaoAtual() > 0) {
            throw new RegraNegocioException("Não é possível remover um quarto ocupado.");
        }
        repository.deleteById(id);
    }

    public Quarto ocupar(String id) {
        Quarto quarto = buscar(id);
        quarto.ocupar();
        return salvar(quarto);
    }

    public Quarto liberar(String id) {
        Quarto quarto = buscar(id);
        quarto.liberar();
        return salvar(quarto);
    }

    private Quarto salvar(Quarto quarto) {
        try {
            return repository.save(quarto);
        } catch (OptimisticLockingFailureException e) {
            throw new RegraNegocioException("O quarto foi alterado por outra operação. Tente novamente.");
        }
    }
}
