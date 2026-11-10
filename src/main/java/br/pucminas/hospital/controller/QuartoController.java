package br.pucminas.hospital.controller;

import br.pucminas.hospital.model.Quarto;
import br.pucminas.hospital.model.enums.TipoQuarto;
import br.pucminas.hospital.service.QuartoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quartos")
public class QuartoController {

    private final QuartoService service;

    public QuartoController(QuartoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Quarto criar(@Valid @RequestBody Quarto quarto) {
        return service.criar(quarto);
    }

    /** Filtros opcionais: ?tipo=ENFERMARIA|APARTAMENTO|UTI&disponiveis=true */
    @GetMapping
    public List<Quarto> listar(@RequestParam(required = false) TipoQuarto tipo,
                               @RequestParam(defaultValue = "false") boolean disponiveis) {
        return service.listar(tipo, disponiveis);
    }

    @GetMapping("/{id}")
    public Quarto buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public Quarto atualizar(@PathVariable String id, @Valid @RequestBody Quarto quarto) {
        return service.atualizar(id, quarto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable String id) {
        service.deletar(id);
    }
}
