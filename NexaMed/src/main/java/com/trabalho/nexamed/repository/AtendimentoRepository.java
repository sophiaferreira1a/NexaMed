package br.pucminas.hospital.repository;

import br.pucminas.hospital.model.Atendimento;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AtendimentoRepository extends MongoRepository<Atendimento, String> {
    List<Atendimento> findByProfissionalIdAndDataAndHorario(String profissionalId, LocalDate data, LocalTime horario);

    List<Atendimento> findByPacienteId(String pacienteId);

    List<Atendimento> findByProfissionalId(String profissionalId);

    boolean existsByPacienteId(String pacienteId);

    boolean existsByProfissionalId(String profissionalId);
}
