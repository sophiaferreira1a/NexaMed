package br.pucminas.hospital.model.enums;

public enum SituacaoEmergencia {
    AGUARDANDO_AVALIACAO, INICIADO, CONCLUIDO;

    public boolean podeTransicionarPara(SituacaoEmergencia nova) {
        return switch (this) {
            case AGUARDANDO_AVALIACAO -> nova == INICIADO;
            case INICIADO -> nova == CONCLUIDO;
            case CONCLUIDO -> false;
        };
    }
}
