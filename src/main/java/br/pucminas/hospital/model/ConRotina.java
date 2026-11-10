package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.enums.SituacaoConsulta;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConRotina extends Consulta {
    private String motivo;

    @Override
    public void validarCondicoes(Profissional profissional) {
        if (!(profissional instanceof ClinicoGeral)) {
            throw new RegraNegocioException("Consulta de rotina deve ser realizada por um clínico geral.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException("O motivo da consulta é obrigatório.");
        }
    }

    /** O clínico geral só encaminha a partir de uma consulta que está em atendimento ou já foi concluída. */
    public boolean permiteEncaminhamento() {
        return getSituacao() == SituacaoConsulta.EM_ATENDIMENTO || getSituacao() == SituacaoConsulta.CONCLUIDA;
    }

    @Override
    protected Consulta copiarDadosDaModalidade() {
        ConRotina nova = new ConRotina();
        nova.setMotivo(motivo);
        return nova;
    }
}
