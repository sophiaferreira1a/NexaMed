package br.pucminas.hospital.model.enums;

import br.pucminas.hospital.exception.RegraNegocioException;

/**
 * Cada tipo de quarto define quantos leitos pode ter:
 * enfermaria é coletiva; apartamento e UTI são individuais.
 */
public enum TipoQuarto {
    ENFERMARIA(2, 6),
    APARTAMENTO(1, 1),
    UTI(1, 1);

    private final int capacidadeMinima;
    private final int capacidadeMaxima;

    TipoQuarto(int capacidadeMinima, int capacidadeMaxima) {
        this.capacidadeMinima = capacidadeMinima;
        this.capacidadeMaxima = capacidadeMaxima;
    }

    public int getCapacidadeMinima() {
        return capacidadeMinima;
    }

    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    public void validarCapacidade(int capacidade) {
        if (capacidade < capacidadeMinima || capacidade > capacidadeMaxima) {
            String faixa = capacidadeMinima == capacidadeMaxima
                    ? "exatamente " + capacidadeMinima + " leito (quarto individual)"
                    : "entre " + capacidadeMinima + " e " + capacidadeMaxima + " leitos";
            throw new RegraNegocioException("Quartos do tipo " + this + " devem ter " + faixa + ".");
        }
    }
}
