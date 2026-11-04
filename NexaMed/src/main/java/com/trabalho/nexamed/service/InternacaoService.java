package br.pucminas.hospital.service;

import br.pucminas.hospital.dto.AtualizarInternacaoRequest;
import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.Internacao;
import br.pucminas.hospital.repository.InternacaoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class InternacaoService {

    private final InternacaoRepository repository;
    private final PacienteService pacienteService;
    private final ProfissionalService profissionalService;
    private final QuartoService quartoService;

    public InternacaoService(InternacaoRepository repository,
                             PacienteService pacienteService,
                             ProfissionalService profissionalService,
                             QuartoService quartoService) {
        this.repository = repository;
        this.pacienteService = pacienteService;
        this.profissionalService = profissionalService;
        this.quartoService = quartoService;
    }

    public Internacao criar(Internacao internacao) {
        pacienteService.buscar(internacao.getPacienteId());
        profissionalService.buscar(internacao.getProfissionalId());

        if (repository.existsByPacienteIdAndDataEfetivaAltaIsNull(internacao.getPacienteId())) {
            throw new RegraNegocioException("O paciente já possui uma internação ativa.");
        }
        if (internacao.getDataEntrada() == null) {
            internacao.setDataEntrada(LocalDate.now());
        }
        if (internacao.getDataPrevistaAlta() != null
                && internacao.getDataPrevistaAlta().isBefore(internacao.getDataEntrada())) {
            throw new RegraNegocioException("A data prevista de alta não pode ser anterior à data de entrada.");
        }

        // Regras 4 e 5: quarto existente, com vaga (lança exceção se estiver lotado)
        quartoService.ocupar(internacao.getQuartoId());

        internacao.setId(null);
        internacao.setDataEfetivaAlta(null);
        try {
            return repository.save(internacao);
        } catch (RuntimeException e) {
            quartoService.liberar(internacao.getQuartoId()); // desfaz a ocupação se o save falhar
            throw e;
        }
    }

    public List<Internacao> listar(String pacienteId) {
        return pacienteId == null ? repository.findAll() : repository.findByPacienteId(pacienteId);
    }

    public Internacao buscar(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Internação não encontrada: " + id));
    }

    public Internacao atualizar(String id, AtualizarInternacaoRequest req) {
        Internacao internacao = buscar(id);
        if (req.dataPrevistaAlta() != null) {
            if (req.dataPrevistaAlta().isBefore(internacao.getDataEntrada())) {
                throw new RegraNegocioException("A data prevista de alta não pode ser anterior à data de entrada.");
            }
            internacao.setDataPrevistaAlta(req.dataPrevistaAlta());
        }
        if (req.observacoes() != null) {
            internacao.setObservacoes(req.observacoes());
        }
        return repository.save(internacao);
    }

    public Internacao darAlta(String id, LocalDate data) {
        Internacao internacao = buscar(id);
        internacao.darAlta(data);
        quartoService.liberar(internacao.getQuartoId());
        return repository.save(internacao);
    }
}
