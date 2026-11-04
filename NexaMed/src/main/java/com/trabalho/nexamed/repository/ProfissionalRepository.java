package br.pucminas.hospital.repository;

import br.pucminas.hospital.model.Profissional;
import br.pucminas.hospital.model.enums.Especialidade;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProfissionalRepository extends MongoRepository<Profissional, String> {
    boolean existsByRegistroProfissional(String registroProfissional);

    // @Query porque "especialidade" existe só na subclasse Especialista
    @Query("{ 'especialidade': ?0 }")
    List<Profissional> buscarPorEspecialidade(Especialidade especialidade);
}
