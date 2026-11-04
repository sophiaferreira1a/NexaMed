package br.pucminas.hospital.dto;

import java.time.LocalDate;

public record AtualizarInternacaoRequest(LocalDate dataPrevistaAlta, String observacoes) {
}
