package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.enums.SituacaoProcedimento;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ProCirurgico extends Atendimento {
    private String descricaoProcedimento;
    private String tipoCirurgia;
    private String observacoesPreOp;
    private String observacoesPosOp;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDate dataEfetiva;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalTime horarioEfetivo;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private SituacaoProcedimento situacao = SituacaoProcedimento.AGENDADO;

    @Override
    public void validarCondicoes(Profissional profissional) {
        if (!(profissional instanceof Cirurgiao)) {
            throw new RegraNegocioException("Procedimento cirúrgico deve ter um cirurgião responsável.");
        }
        if (descricaoProcedimento == null || descricaoProcedimento.isBlank()) {
            throw new RegraNegocioException("A descrição do procedimento é obrigatória.");
        }
    }

    @Override
    public boolean ocupaHorario() {
        return situacao != SituacaoProcedimento.CANCELADO;
    }

    public void iniciar() {
        if (situacao != SituacaoProcedimento.AGENDADO) {
            throw new RegraNegocioException("Só é possível iniciar um procedimento agendado.");
        }
        situacao = SituacaoProcedimento.EM_ANDAMENTO;
        dataEfetiva = LocalDate.now();
        horarioEfetivo = LocalTime.now().withSecond(0).withNano(0);
    }

    public void concluir() {
        if (situacao != SituacaoProcedimento.EM_ANDAMENTO) {
            throw new RegraNegocioException("Não é permitido concluir um procedimento que não foi iniciado.");
        }
        situacao = SituacaoProcedimento.CONCLUIDO;
    }

    public void cancelar() {
        if (situacao == SituacaoProcedimento.CONCLUIDO || situacao == SituacaoProcedimento.CANCELADO) {
            throw new RegraNegocioException("Procedimento " + situacao + " não pode ser cancelado.");
        }
        situacao = SituacaoProcedimento.CANCELADO;
    }

    public void alterarSituacao(SituacaoProcedimento nova) {
        switch (nova) {
            case EM_ANDAMENTO -> iniciar();
            case CONCLUIDO -> concluir();
            case CANCELADO -> cancelar();
            case AGENDADO -> throw new RegraNegocioException("Um procedimento não pode voltar a ser agendado.");
        }
    }
}
