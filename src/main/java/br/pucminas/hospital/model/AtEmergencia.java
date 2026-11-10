package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.enums.Prioridade;
import br.pucminas.hospital.model.enums.SituacaoEmergencia;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtEmergencia extends Atendimento {
    private Prioridade prioridade;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private SituacaoEmergencia situacao = SituacaoEmergencia.AGUARDANDO_AVALIACAO;

    @Override
    public void validarCondicoes(Profissional profissional) {
        if (prioridade == null) {
            throw new RegraNegocioException("A prioridade do atendimento de emergência é obrigatória.");
        }
    }

    @Override
    public boolean ocupaHorario() {
        return true;
    }

    @Override
    public Enum<?> situacaoAtual() {
        return situacao;
    }

    @Override
    protected boolean permiteAlterarAgendamento() {
        return situacao == SituacaoEmergencia.AGUARDANDO_AVALIACAO;
    }

    @Override
    protected boolean permiteRegistrarObservacoes() {
        return situacao != SituacaoEmergencia.AGUARDANDO_AVALIACAO;
    }

    public void alterarSituacao(SituacaoEmergencia nova) {
        if (!situacao.podeTransicionarPara(nova)) {
            throw new RegraNegocioException("Transição não permitida: " + situacao + " -> " + nova);
        }
        situacao = nova;
    }
}
