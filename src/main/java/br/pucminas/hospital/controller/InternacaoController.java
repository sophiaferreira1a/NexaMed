package br.pucminas.hospital.controller;

import br.pucminas.hospital.dto.AltaRequest;
import br.pucminas.hospital.dto.AtualizarInternacaoRequest;
import br.pucminas.hospital.model.Internacao;
import br.pucminas.hospital.service.InternacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Internações não são removidas (histórico): encerre com PATCH /{id}/alta. */
@RestController
@RequestMapping("/internacoes")
public class InternacaoController {

    private final InternacaoService service;

    public InternacaoController(InternacaoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Internacao criar(@Valid @RequestBody Internacao internacao) {
        return service.criar(internacao);
    }

    @GetMapping
    public List<Internacao> listar(@RequestParam(required = false) String pacienteId) {
        return service.listar(pacienteId);
    }

    @GetMapping("/{id}")
    public Internacao buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public Internacao atualizar(@PathVariable String id, @RequestBody AtualizarInternacaoRequest req) {
        return service.atualizar(id, req);
    }

    @PatchMapping("/{id}/alta")
    public Internacao darAlta(@PathVariable String id, @Valid @RequestBody AltaRequest req) {
        return service.darAlta(id, req.data());
    }
}
