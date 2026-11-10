package br.pucminas.hospital.controller;

import br.pucminas.hospital.dto.AlterarSituacaoRequest;
import br.pucminas.hospital.dto.AtualizarAtendimentoRequest;
import br.pucminas.hospital.dto.EncaminhamentoRequest;
import br.pucminas.hospital.dto.ReagendamentoRequest;
import br.pucminas.hospital.model.AtEmergencia;
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

    /** Filtros opcionais: ?pacienteId=...&tipo=ROTINA|ESPECIALIZADA|EMERGENCIA&situacao=AGENDADA... */
    @GetMapping
    public List<Atendimento> listar(@RequestParam(required = false) String pacienteId,
                                    @RequestParam(required = false) String tipo,
                                    @RequestParam(required = false) String situacao) {
        return service.listarConsultas(pacienteId, tipo, situacao);
    }

    /** Emergências da mais urgente para a menos urgente. Ex.: ?situacao=AGUARDANDO_AVALIACAO */
    @GetMapping("/emergencias")
    public List<AtEmergencia> listarEmergencias(@RequestParam(required = false) String situacao) {
        return service.listarEmergencias(situacao);
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

    /** Novo agendamento para uma consulta cancelada ou com falta. */
    @PostMapping("/{id}/reagendamento")
    @ResponseStatus(HttpStatus.CREATED)
    public Atendimento reagendar(@PathVariable String id, @Valid @RequestBody ReagendamentoRequest req) {
        return service.reagendar(id, req);
    }

    /** Clínico geral encaminha o paciente de uma consulta de rotina para um especialista. */
    @PostMapping("/{id}/encaminhamento")
    @ResponseStatus(HttpStatus.CREATED)
    public Atendimento encaminhar(@PathVariable String id, @Valid @RequestBody EncaminhamentoRequest req) {
        return service.encaminhar(id, req);
    }
}
