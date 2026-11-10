package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.enums.Especialidade;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConEspecializada extends Consulta {
    private Especialidade especialidade;

    /** Consulta de rotina que originou o encaminhamento, quando houver. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String consultaOrigemId;

    @Override
    public void validarCondicoes(Profissional profissional) {
        if (especialidade == null) {
            throw new RegraNegocioException("A especialidade do atendimento é obrigatória.");
        }
        if (!(profissional instanceof Especialista esp) || esp.getEspecialidade() != especialidade) {
            throw new RegraNegocioException(
                    "O profissional deve ser um especialista em " + especialidade + ".");
        }
    }

    @Override
    protected Consulta copiarDadosDaModalidade() {
        ConEspecializada nova = new ConEspecializada();
        nova.setEspecialidade(especialidade);
        nova.setConsultaOrigemId(consultaOrigemId);
        return nova;
    }
}
