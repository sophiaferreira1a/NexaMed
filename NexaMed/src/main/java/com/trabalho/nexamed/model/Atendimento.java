package br.pucminas.hospital.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Document(collection = "atendimentos")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tipo")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ConRotina.class, name = "ROTINA"),
        @JsonSubTypes.Type(value = ConEspecializada.class, name = "ESPECIALIZADA"),
        @JsonSubTypes.Type(value = AtEmergencia.class, name = "EMERGENCIA"),
        @JsonSubTypes.Type(value = ProCirurgico.class, name = "CIRURGICO")
})
public abstract class Atendimento {
    @Id
    private String id;
    private String pacienteId;
    private String profissionalId;
    @NotNull
    private LocalDate data;
    @NotNull
    private LocalTime horario;
    private String observacoes;

    /** Verifica se o profissional é compatível com esta modalidade; lança RegraNegocioException se não for. */
    public abstract void validarCondicoes(Profissional profissional);

    /** Indica se este atendimento ainda "ocupa" o horário do profissional (cancelados liberam o horário). */
    public abstract boolean ocupaHorario();
}
