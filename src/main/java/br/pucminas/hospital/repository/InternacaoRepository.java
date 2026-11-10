package br.pucminas.hospital.repository;

import br.pucminas.hospital.model.Internacao;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InternacaoRepository extends MongoRepository<Internacao, String> {
    List<Internacao> findByPacienteId(String pacienteId);

    boolean existsByPacienteId(String pacienteId);

    boolean existsByProfissionalId(String profissionalId);

    boolean existsByPacienteIdAndDataEfetivaAltaIsNull(String pacienteId);
}
