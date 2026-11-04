package br.pucminas.hospital.controller;

import br.pucminas.hospital.dto.HistoricoMedico;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Paciente criar(@Valid @RequestBody Paciente paciente) {
        return service.criar(paciente);
    }

    @GetMapping
    public List<Paciente> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Paciente buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @GetMapping("/{id}/historico")
    public HistoricoMedico historico(@PathVariable String id) {
        return service.historico(id);
    }

    @PutMapping("/{id}")
    public Paciente atualizar(@PathVariable String id, @Valid @RequestBody Paciente paciente) {
        return service.atualizar(id, paciente);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable String id) {
        service.deletar(id);
    }
}
