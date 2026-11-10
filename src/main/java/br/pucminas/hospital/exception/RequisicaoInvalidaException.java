package br.pucminas.hospital.exception;

/** Dado obrigatório ausente ou inválido na requisição (responde 400). */
public class RequisicaoInvalidaException extends RuntimeException {
    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
