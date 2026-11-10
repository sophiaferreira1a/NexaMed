package br.pucminas.hospital.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/** Nova data e horário para uma consulta cancelada ou com falta. */
public record ReagendamentoRequest(@NotNull LocalDate data, @NotNull LocalTime horario) {
}
