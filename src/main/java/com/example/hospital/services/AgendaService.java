package com.example.hospital.services;

import com.example.hospital.model.*;
import com.example.hospital.repository.AgendaRepository;
import com.example.hospital.repository.AgendamentoRepository;
import com.example.hospital.repository.ConsultaRepository;
import com.example.hospital.repository.MedicoRepository;
import com.example.hospital.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AgendaService {

    private final AgendaRepository agendaRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;
    private final ConsultaRepository consultaRepository;

    public AgendaService(AgendaRepository agendaRepository,
                         AgendamentoRepository agendamentoRepository,
                         MedicoRepository medicoRepository,
                         PacienteRepository pacienteRepository,
                         ConsultaRepository consultaRepository) {
        this.agendaRepository = agendaRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
        this.consultaRepository = consultaRepository;
    }

    @Transactional
    public void cadastrarHorario(Long medicoId, LocalDateTime dataHora) {
        if (dataHora == null) {
            throw new IllegalArgumentException("Informe a data e o horário.");
        }
        if (agendaRepository.existsByMedicoIdAndDataHora(medicoId, dataHora)) {
            throw new IllegalStateException("Este médico já possui um horário cadastrado nessa data e hora.");
        }

        Agenda agenda = new Agenda();
        agenda.setMedico(medicoRepository.findById(medicoId)
                .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado.")));
        agenda.setDataHora(dataHora);
        agenda.setStatus(StatusAgenda.DISPONIVEL);
        agendaRepository.save(agenda);
    }

    @Transactional
    public void agendar(Long agendaId, Long pacienteId) {
        Agenda agenda = agendaRepository.findByIdForUpdate(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("Horário não encontrado."));
        if (agenda.getStatus() != StatusAgenda.DISPONIVEL) {
            throw new IllegalStateException("Este horário não está mais disponível.");
        }

        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado."));
        Agendamento agendamento = new Agendamento();
        agendamento.setAgenda(agenda);
        agendamento.setPaciente(paciente);
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        agendamentoRepository.save(agendamento);

        agenda.setStatus(StatusAgenda.AGENDADO);
    }

    @Transactional
    public void confirmarPresenca(Long agendamentoId) {
        Agendamento agendamento = agendamentoRepository.findByIdForUpdate(agendamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado."));
        if (agendamento.getStatus() != StatusAgendamento.AGENDADO) {
            throw new IllegalStateException("Somente agendamentos aguardando comparecimento podem ser confirmados.");
        }
        if (consultaRepository.existsByAgendamentoId(agendamentoId)) {
            throw new IllegalStateException("Já existe uma consulta registrada para este agendamento.");
        }

        Consulta consulta = new Consulta();
        consulta.setAgendamento(agendamento);
        consulta.setPaciente(agendamento.getPaciente());
        consulta.setMedico(agendamento.getAgenda().getMedico());
        consulta.setData(agendamento.getAgenda().getDataHora());
        consulta.setValor(agendamento.getValor() == null ? null : agendamento.getValor().doubleValue());
        consultaRepository.save(consulta);

        agendamento.setStatus(StatusAgendamento.REALIZADO);
    }

    @Transactional
    public void atualizarValor(Long agendamentoId, BigDecimal valor) {
        if (valor == null || valor.signum() < 0 || valor.scale() > 2) {
            throw new IllegalArgumentException("Informe um valor válido, com até duas casas decimais.");
        }

        Agendamento agendamento = agendamentoRepository.findByIdForUpdate(agendamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado."));
        if (agendamento.getStatus() != StatusAgendamento.AGENDADO) {
            throw new IllegalStateException("O valor só pode ser alterado enquanto o agendamento estiver pendente.");
        }
        agendamento.setValor(valor);
    }

    @Transactional
    public void cancelar(Long agendamentoId) {
        Agendamento agendamento = agendamentoRepository.findByIdForUpdate(agendamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado."));
        if (agendamento.getStatus() != StatusAgendamento.AGENDADO) {
            throw new IllegalStateException("Somente agendamentos pendentes podem ser cancelados.");
        }

        Agenda agenda = agendaRepository.findByIdForUpdate(agendamento.getAgenda().getId())
                .orElseThrow(() -> new IllegalArgumentException("Horário da agenda não encontrado."));
        agendamento.setStatus(StatusAgendamento.CANCELADO);
        agenda.setStatus(StatusAgenda.DISPONIVEL);
    }
}
