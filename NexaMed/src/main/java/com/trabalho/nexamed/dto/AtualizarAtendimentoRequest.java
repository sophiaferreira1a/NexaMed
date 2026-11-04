package br.pucminas.hospital.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AtualizarAtendimentoRequest(LocalDate data, LocalTime horario, String observacoes) {
}
