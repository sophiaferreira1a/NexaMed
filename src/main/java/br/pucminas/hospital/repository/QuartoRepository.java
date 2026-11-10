package br.pucminas.hospital.repository;

import br.pucminas.hospital.model.Quarto;
import br.pucminas.hospital.model.enums.SituacaoQuarto;
import br.pucminas.hospital.model.enums.TipoQuarto;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuartoRepository extends MongoRepository<Quarto, String> {
    boolean existsByNumId(String numId);

    List<Quarto> findBySituacao(SituacaoQuarto situacao);

    List<Quarto> findByTipo(TipoQuarto tipo);

    List<Quarto> findByTipoAndSituacao(TipoQuarto tipo, SituacaoQuarto situacao);
}
