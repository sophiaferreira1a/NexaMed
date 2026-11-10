package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.enums.Especialidade;
import br.pucminas.hospital.model.enums.SituacaoConsulta;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConEspecializada extends Atendimento {
    private Especialidade especialidade;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private SituacaoConsulta situacao = SituacaoConsulta.AGENDADA;

    @Override
    public void validarCondicoes(Profissional profissional) {
        if (especialidade == null) {
            throw new RegraNegocioException("A especialidade do atendimento é obrigatória.");
        }
        if (!(profissional instanceof Especialista esp) || esp.getEspecialidade() != especialidade) {
            throw new RegraNegocioException(
                    "O profissional deve ser um especialista em " + especialidade + ".");
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
