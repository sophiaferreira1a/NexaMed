package br.pucminas.hospital.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Campos opcionais: só o que vier preenchido é alterado.
 * descricaoProcedimento, tipoCirurgia, observacoesPreOp e observacoesPosOp valem apenas para procedimentos cirúrgicos.
 */
public record AtualizarAtendimentoRequest(LocalDate data,
                                          LocalTime horario,
                                          String observacoes,
                                          String descricaoProcedimento,
                                          String tipoCirurgia,
                                          String observacoesPreOp,
                                          String observacoesPosOp) {

    public boolean temDadosCirurgicos() {
        return descricaoProcedimento != null || tipoCirurgia != null || observacoesPreOp != null || observacoesPosOp != null;
    }
}
