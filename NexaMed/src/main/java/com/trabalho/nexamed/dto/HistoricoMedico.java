package br.pucminas.hospital.dto;

import br.pucminas.hospital.model.Atendimento;
import br.pucminas.hospital.model.Internacao;
import br.pucminas.hospital.model.Paciente;

import java.util.List;

public record HistoricoMedico(Paciente paciente, List<Atendimento> atendimentos, List<Internacao> internacoes) {
}
