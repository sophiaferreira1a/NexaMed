package br.pucminas.hospital.model;

import br.pucminas.hospital.model.enums.Especialidade;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Especialista extends Profissional {
    @NotNull
    private Especialidade especialidade;
}
