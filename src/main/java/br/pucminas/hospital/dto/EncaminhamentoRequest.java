package br.pucminas.hospital.dto;

import br.pucminas.hospital.model.enums.Especialidade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/** Encaminhamento feito pelo clínico geral: gera uma consulta especializada para o mesmo paciente. */
public record EncaminhamentoRequest(@NotNull Especialidade especialidade,
                                    @NotBlank String profissionalId,
                                    @NotNull LocalDate data,
                                    @NotNull LocalTime horario) {
}
