package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.enums.SituacaoQuarto;
import br.pucminas.hospital.model.enums.TipoQuarto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "quartos")
public class Quarto {
    @Id
    private String id;
    @NotBlank
    private String numId;
    private int andar;
    @Min(1)
    private int capacidadeMax;
    @NotNull
    private TipoQuarto tipo;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int ocupacaoAtual = 0;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private SituacaoQuarto situacao = SituacaoQuarto.DISPONIVEL;

    // Controle de concorrência otimista: se duas internações tentarem ocupar a última
    // vaga ao mesmo tempo, o save() da segunda falha com OptimisticLockingFailureException.
    @Version
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long version;

    /** Regra dos tipos de quarto: a capacidade precisa respeitar a faixa do tipo (ver TipoQuarto). */
    public void validarTipo() {
        tipo.validarCapacidade(capacidadeMax);
    }

    public boolean temVaga() {
        return ocupacaoAtual < capacidadeMax;
    }

    public void ocupar() {
        if (!temVaga()) {
            throw new RegraNegocioException("O quarto " + numId + " já atingiu a capacidade máxima.");
        }
        ocupacaoAtual++;
        atualizarSituacao();
    }

    public void liberar() {
        if (ocupacaoAtual == 0) {
            throw new RegraNegocioException("O quarto " + numId + " já está vazio.");
        }
        ocupacaoAtual--;
        atualizarSituacao();
    }

    public void atualizarSituacao() {
        situacao = temVaga() ? SituacaoQuarto.DISPONIVEL : SituacaoQuarto.OCUPADO;
    }
}
