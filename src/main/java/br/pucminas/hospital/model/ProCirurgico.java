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
        if (tipoCirurgia == null || tipoCirurgia.isBlank()) {
            throw new RegraNegocioException("O tipo de cirurgia é obrigatório.");
        }
    }

    @Override
    public Enum<?> situacaoAtual() {
        return situacao;
    }

    @Override
    protected boolean permiteAlterarAgendamento() {
        return situacao == SituacaoProcedimento.AGENDADO;
    }

    @Override
    protected boolean permiteRegistrarObservacoes() {
        return situacao == SituacaoProcedimento.EM_ANDAMENTO || situacao == SituacaoProcedimento.CONCLUIDO;
    }

    /** Descrição e tipo da cirurgia podem ser corrigidos enquanto o procedimento está agendado. */
    public void alterarDadosDoProcedimento(String novaDescricao, String novoTipo) {
        if (situacao != SituacaoProcedimento.AGENDADO) {
            throw new RegraNegocioException("Descrição e tipo só podem ser alterados antes do início do procedimento.");
        }
        if (novaDescricao != null) {
            if (novaDescricao.isBlank()) {
                throw new RegraNegocioException("A descrição do procedimento é obrigatória.");
            }
            descricaoProcedimento = novaDescricao;
        }
        if (novoTipo != null) {
            if (novoTipo.isBlank()) {
                throw new RegraNegocioException("O tipo de cirurgia é obrigatório.");
            }
            tipoCirurgia = novoTipo;
        }
    }

    /** Observações pré-operatórias: registradas enquanto o procedimento ainda está agendado. */
    public void registrarObservacoesPreOp(String texto) {
        if (situacao != SituacaoProcedimento.AGENDADO) {
            throw new RegraNegocioException("Observações pré-operatórias só podem ser registradas antes do início.");
        }
        observacoesPreOp = texto;
    }

    /** Observações pós-operatórias: registradas depois que o procedimento foi iniciado. */
    public void registrarObservacoesPosOp(String texto) {
        if (situacao != SituacaoProcedimento.EM_ANDAMENTO && situacao != SituacaoProcedimento.CONCLUIDO) {
            throw new RegraNegocioException("Observações pós-operatórias só podem ser registradas após o início.");
        }
        observacoesPosOp = texto;
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
