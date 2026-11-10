package br.pucminas.hospital.controller;

import br.pucminas.hospital.dto.AlterarSituacaoRequest;
import br.pucminas.hospital.dto.AtualizarAtendimentoRequest;
import br.pucminas.hospital.model.Atendimento;
import br.pucminas.hospital.model.Profissional;
import br.pucminas.hospital.model.enums.Especialidade;
import br.pucminas.hospital.service.AtendimentoService;
import br.pucminas.hospital.service.ProfissionalService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/profissionais")
public class ProfissionalController {

    private final ProfissionalService service;
    private final AtendimentoService atendimentoService;

    public ProfissionalController(ProfissionalService service, AtendimentoService atendimentoService) {
        this.service = service;
        this.atendimentoService = atendimentoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Profissional criar(@Valid @RequestBody Profissional profissional) {
        return service.criar(profissional);
    }

    /** Filtros opcionais: ?tipo=CLINICO_GERAL|ESPECIALISTA|CIRURGIAO&especialidade=CARDIOLOGIA */
    @GetMapping
    public List<Profissional> listar(@RequestParam(required = false) String tipo,
                                     @RequestParam(required = false) Especialidade especialidade) {
        return service.listar(tipo, especialidade);
    }

    /** Profissionais livres num horário. Ex.: ?especialidade=CARDIOLOGIA&data=2026-11-20&horario=14:00 */
    @GetMapping("/disponiveis")
    public List<Profissional> listarDisponiveis(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Especialidade especialidade,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime horario) {
        return service.listarDisponiveis(tipo, especialidade, data, horario);
    }

    @GetMapping("/{id}")
    public Profissional buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public Profissional atualizar(@PathVariable String id, @Valid @RequestBody Profissional profissional) {
        return service.atualizar(id, profissional);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable String id) {
        service.deletar(id);
    }

    /** Consultas e emergências do profissional (ex.: consultas realizadas: ?situacao=CONCLUIDA). */
    @GetMapping("/{id}/consultas")
    public List<Atendimento> listarConsultas(@PathVariable String id,
                                             @RequestParam(required = false) String situacao) {
        return atendimentoService.listarConsultasDoProfissional(id, situacao);
    }

    // ---------- procedimentos cirúrgicos do cirurgião ----------

    @PostMapping("/{id}/procedimentos")
    @ResponseStatus(HttpStatus.CREATED)
    public Atendimento criarProcedimento(@PathVariable String id, @Valid @RequestBody Atendimento procedimento) {
        return atendimentoService.criarProcedimento(id, procedimento);
    }

    @GetMapping("/{id}/procedimentos")
    public List<Atendimento> listarProcedimentos(@PathVariable String id) {
        return atendimentoService.listarProcedimentos(id);
    }

    @GetMapping("/{id}/procedimentos/{procedimentoId}")
    public Atendimento buscarProcedimento(@PathVariable String id, @PathVariable String procedimentoId) {
        return atendimentoService.buscarProcedimento(id, procedimentoId);
    }

    @PutMapping("/{id}/procedimentos/{procedimentoId}")
    public Atendimento atualizarProcedimento(@PathVariable String id, @PathVariable String procedimentoId,
                                             @RequestBody AtualizarAtendimentoRequest req) {
        return atendimentoService.atualizar(atendimentoService.buscarProcedimento(id, procedimentoId), req);
    }

    @PatchMapping("/{id}/procedimentos/{procedimentoId}/situacao")
    public Atendimento alterarSituacaoProcedimento(@PathVariable String id, @PathVariable String procedimentoId,
                                                   @Valid @RequestBody AlterarSituacaoRequest req) {
        return atendimentoService.alterarSituacao(
                atendimentoService.buscarProcedimento(id, procedimentoId), req.situacao());
    }
}
