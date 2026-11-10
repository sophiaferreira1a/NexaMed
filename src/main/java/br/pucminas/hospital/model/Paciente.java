package br.pucminas.hospital.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Getter
@Setter
@Document(collection = "pacientes")
public class Paciente {
    @Id
    private String id;
    @NotBlank
    private String nome;
    @NotBlank
    private String cpf;
    @NotNull
    private LocalDate dataDeNascimento;
    private String telefone;
    private String endereco;
    @Email
    private String email;
}
