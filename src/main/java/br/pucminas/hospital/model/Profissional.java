package br.pucminas.hospital.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "profissionais")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tipo")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ClinicoGeral.class, name = "CLINICO_GERAL"),
        @JsonSubTypes.Type(value = Especialista.class, name = "ESPECIALISTA"),
        @JsonSubTypes.Type(value = Cirurgiao.class, name = "CIRURGIAO")
})
public abstract class Profissional {
    @Id
    private String id;
    @NotBlank
    private String nome;
    @NotBlank
    private String registroProfissional;
    private String telefone;
    @Email
    private String email;
}
