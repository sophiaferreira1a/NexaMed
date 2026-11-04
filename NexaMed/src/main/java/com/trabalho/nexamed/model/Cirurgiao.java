package br.pucminas.hospital.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cirurgiao extends Profissional {
    private String atuacaoCirurgica;
    // Os procedimentos realizados são obtidos em /profissionais/{id}/procedimentos.
}
