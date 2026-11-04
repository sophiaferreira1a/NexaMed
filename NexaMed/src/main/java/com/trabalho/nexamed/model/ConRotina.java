package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.enums.SituacaoConsulta;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConRotina extends Atendimento {
    private String motivo;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private SituacaoConsulta situacao = SituacaoConsulta.AGENDADA;

    @Override
    public void validarCondicoes(Profissional profissional) {
        if (!(profissional instanceof ClinicoGeral)) {
            throw new RegraNegocioException("Consulta de rotina deve ser realizada por um clínico geral.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException("O motivo da consulta é obrigatório.");
        }
    }

    @Override
    public boolean ocupaHorario() {
        return situacao != SituacaoConsulta.CANCELADA && situacao != SituacaoConsulta.NAO_COMPARECEU;
    }

    public void alterarSituacao(SituacaoConsulta nova) {
        if (!situacao.podeTransicionarPara(nova)) {
            throw new RegraNegocioException("Transição não permitida: " + situacao + " -> " + nova);
        }
        situacao = nova;
    }
}
