package br.pucminas.hospital.dto;

import jakarta.validation.constraints.NotBlank;

public record AlterarSituacaoRequest(@NotBlank String situacao) {
}
