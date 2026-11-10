package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank
    private String pacienteId;
    private String profissionalId;
    @NotNull
    private LocalDate data;
    @NotNull
    private LocalTime horario;
    private String observacoes;

    /** Id do atendimento cancelado (ou com falta) que deu origem a este novo agendamento. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String reagendamentoDeId;

    /** Verifica se o profissional é compatível com esta modalidade; lança RegraNegocioException se não for. */
    public abstract void validarCondicoes(Profissional profissional);

    /** Indica se este atendimento ainda "ocupa" o horário do profissional (cancelados liberam o horário). */
    public abstract boolean ocupaHorario();

    /** Situação atual na enumeração própria da modalidade (consulta, emergência ou procedimento). */
    public abstract Enum<?> situacaoAtual();

    /** Data e horário só podem mudar enquanto o atendimento ainda não começou. */
    protected abstract boolean permiteAlterarAgendamento();

    /** Observações só podem ser registradas depois que o atendimento começou. */
    protected abstract boolean permiteRegistrarObservacoes();

    public void alterarAgendamento(LocalDate novaData, LocalTime novoHorario) {
        if (!permiteAlterarAgendamento()) {
            throw new RegraNegocioException("Não é possível alterar data e horário de um atendimento "
                    + situacaoAtual() + ".");
        }
        this.data = novaData;
        this.horario = novoHorario;
    }

    public void registrarObservacoes(String texto) {
        if (!permiteRegistrarObservacoes()) {
            throw new RegraNegocioException("As observações só podem ser registradas após o início do atendimento"
                    + " (situação atual: " + situacaoAtual() + ").");
        }
        this.observacoes = texto;
    }
}
