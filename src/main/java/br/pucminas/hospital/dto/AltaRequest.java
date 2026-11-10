package br.pucminas.hospital.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AltaRequest(@NotNull LocalDate data) {
}
