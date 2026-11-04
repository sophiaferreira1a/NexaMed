package br.pucminas.hospital.repository;

import br.pucminas.hospital.model.Paciente;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PacienteRepository extends MongoRepository<Paciente, String> {
    boolean existsByCpf(String cpf);
}
