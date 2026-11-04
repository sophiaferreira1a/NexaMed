package br.pucminas.hospital.model.enums;

public enum SituacaoConsulta {
    AGENDADA, CONFIRMADA, EM_ATENDIMENTO, CONCLUIDA, CANCELADA, NAO_COMPARECEU;

    public boolean podeTransicionarPara(SituacaoConsulta nova) {
        return switch (this) {
            case AGENDADA -> nova == CONFIRMADA || nova == CANCELADA || nova == NAO_COMPARECEU;
            case CONFIRMADA -> nova == EM_ATENDIMENTO || nova == CANCELADA || nova == NAO_COMPARECEU;
            case EM_ATENDIMENTO -> nova == CONCLUIDA;
            case CONCLUIDA, CANCELADA, NAO_COMPARECEU -> false;
        };
    }
}
