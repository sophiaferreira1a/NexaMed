package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Getter
@Setter
@Document(collection = "internacoes")
public class Internacao {
    @Id
    private String id;
    private String pacienteId;
    private String profissionalId;
    private String quartoId;
    private LocalDate dataEntrada;
    private LocalDate dataPrevistaAlta;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDate dataEfetivaAlta;
    private String observacoes;

    public boolean ativa() {
        return dataEfetivaAlta == null;
    }

    public void darAlta(LocalDate data) {
        if (!ativa()) {
            throw new RegraNegocioException("Esta internação já recebeu alta.");
        }
        if (data.isBefore(dataEntrada)) {
            throw new RegraNegocioException("A data de alta não pode ser anterior à data de entrada.");
        }
        dataEfetivaAlta = data;
    }
}
