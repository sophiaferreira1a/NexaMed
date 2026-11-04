package br.pucminas.hospital.controller;

import br.pucminas.hospital.dto.AlterarSituacaoRequest;
import br.pucminas.hospital.dto.AtualizarAtendimentoRequest;
import br.pucminas.hospital.model.Atendimento;
import br.pucminas.hospital.service.AtendimentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Consultas = rotina, especializada e emergência.
 * O campo "tipo" do JSON define o subtipo: ROTINA | ESPECIALIZADA | EMERGENCIA.
 * Consultas não são removidas (histórico): use PATCH /{id}/situacao com CANCELADA.
 */
@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final AtendimentoService service;

    public ConsultaController(AtendimentoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Atendimento criar(@Valid @RequestBody Atendimento consulta) {
        return service.criarConsulta(consulta);
    }

    @GetMapping
    public List<Atendimento> listar(@RequestParam(required = false) String pacienteId) {
        return service.listarConsultas(pacienteId);
    }

    @GetMapping("/{id}")
    public Atendimento buscar(@PathVariable String id) {
        return service.buscarConsulta(id);
    }

    @PutMapping("/{id}")
    public Atendimento atualizar(@PathVariable String id, @RequestBody AtualizarAtendimentoRequest req) {
        return service.atualizar(service.buscarConsulta(id), req);
    }

    @PatchMapping("/{id}/situacao")
    public Atendimento alterarSituacao(@PathVariable String id, @Valid @RequestBody AlterarSituacaoRequest req) {
        return service.alterarSituacao(service.buscarConsulta(id), req.situacao());
    }
}
