package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.enums.SituacaoConsulta;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Parte comum às consultas agendadas (rotina e especializada): a situação e suas transições,
 * as regras de agenda e o reagendamento. Cada subclasse define só o que é próprio da modalidade.
 */
@Getter
@Setter
public abstract class Consulta extends Atendimento {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private SituacaoConsulta situacao = SituacaoConsulta.AGENDADA;

    @Override
    public boolean ocupaHorario() {
        return situacao != SituacaoConsulta.CANCELADA && situacao != SituacaoConsulta.NAO_COMPARECEU;
    }

    @Override
    public Enum<?> situacaoAtual() {
        return situacao;
    }

    @Override
    protected boolean permiteAlterarAgendamento() {
        return situacao == SituacaoConsulta.AGENDADA || situacao == SituacaoConsulta.CONFIRMADA;
    }

    @Override
    protected boolean permiteRegistrarObservacoes() {
        return situacao == SituacaoConsulta.EM_ATENDIMENTO || situacao == SituacaoConsulta.CONCLUIDA;
    }

    public void alterarSituacao(SituacaoConsulta nova) {
        if (!situacao.podeTransicionarPara(nova)) {
            throw new RegraNegocioException("Transição não permitida: " + situacao + " -> " + nova);
        }
        situacao = nova;
    }

    /**
     * Uma consulta cancelada (ou com falta) não volta a ser agendada nem concluída:
     * ela dá origem a um novo agendamento, ligado a ela por "reagendamentoDeId".
     */
    public Consulta criarReagendamento(LocalDate novaData, LocalTime novoHorario) {
        if (situacao != SituacaoConsulta.CANCELADA && situacao != SituacaoConsulta.NAO_COMPARECEU) {
            throw new RegraNegocioException("Só consultas canceladas ou com falta podem ser reagendadas.");
        }
        Consulta nova = copiarDadosDaModalidade();
        nova.setPacienteId(getPacienteId());
        nova.setProfissionalId(getProfissionalId());
        nova.setData(novaData);
        nova.setHorario(novoHorario);
        nova.setReagendamentoDeId(getId());
        return nova;
    }

    /** Nova consulta da mesma modalidade, com os dados específicos dela (motivo, especialidade...). */
    protected abstract Consulta copiarDadosDaModalidade();
}
