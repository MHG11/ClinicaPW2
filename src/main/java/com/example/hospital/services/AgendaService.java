package com.example.hospital.services;

import com.example.hospital.model.*;
import com.example.hospital.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AgendaService {

    private final AgendaRepository agendaRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ConsultaRepository consultaRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;

    public AgendaService(AgendaRepository agendaRepository,
                        AgendamentoRepository agendamentoRepository,
                        ConsultaRepository consultaRepository,
                        MedicoRepository medicoRepository,
                        PacienteRepository pacienteRepository) {
        this.agendaRepository = agendaRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.consultaRepository = consultaRepository;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
    }

    @Transactional(readOnly = true)
    public List<Agenda> horariosDisponiveis() {
        return agendaRepository.findByStatusOrderByDataAscHorarioAsc(StatusAgenda.DISPONIVEL);
    }

    @Transactional(readOnly = true)
    public List<Agenda> horarios() {
        return agendaRepository.findAllByOrderByDataAscHorarioAsc();
    }

    @Transactional
    public void disponibilizarHorario(Long medicoId, LocalDate data, LocalTime horario) {
        if (data == null || horario == null) {
            throw new IllegalArgumentException("Informe a data e o horário.");
        }
        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado."));
        if (agendaRepository.existsByMedicoIdAndDataAndHorario(medicoId, data, horario)) {
            throw new IllegalArgumentException("Este médico já possui um horário cadastrado nessa data e hora.");
        }

        Agenda agenda = new Agenda();
        agenda.setMedico(medico);
        agenda.setData(data);
        agenda.setHorario(horario);
        agenda.setStatus(StatusAgenda.DISPONIVEL);
        agendaRepository.saveAndFlush(agenda);
    }

    @Transactional
    public void agendar(Long agendaId, Long pacienteId) {
        Agenda agenda = agendaRepository.findByIdForUpdate(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("Horário não encontrado."));
        if (agenda.getStatus() != StatusAgenda.DISPONIVEL) {
            throw new IllegalArgumentException("Este horário não está mais disponível.");
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

    @Transactional(readOnly = true)
    public List<Agendamento> agendamentos() {
        return agendamentoRepository.findAllByOrderByCriadoEmDesc();
    }

    @Transactional
    public void cancelar(Long agendamentoId) {
        Agendamento agendamento = agendamentoRepository.findByIdForUpdate(agendamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado."));
        if (agendamento.getStatus() != StatusAgendamento.AGENDADO) {
            throw new IllegalArgumentException("Somente agendamentos aguardando atendimento podem ser cancelados.");
        }

        Agenda agenda = agendaRepository.findByIdForUpdate(agendamento.getAgenda().getId())
                .orElseThrow(() -> new IllegalArgumentException("Horário da agenda não encontrado."));
        agendamento.setStatus(StatusAgendamento.CANCELADO);
        agenda.setStatus(StatusAgenda.DISPONIVEL);
    }

    @Transactional
    public void confirmarPresenca(Long agendamentoId) {
        Agendamento agendamento = agendamentoRepository.findByIdForUpdate(agendamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado."));
        if (agendamento.getStatus() != StatusAgendamento.AGENDADO) {
            throw new IllegalArgumentException("Somente agendamentos aguardando atendimento podem ser confirmados.");
        }
        if (consultaRepository.existsByAgendamentoId(agendamentoId)) {
            throw new IllegalArgumentException("Este agendamento já possui uma consulta registrada.");
        }

        Agenda agenda = agendamento.getAgenda();
        Consulta consulta = new Consulta();
        consulta.setAgendamento(agendamento);
        consulta.setPaciente(agendamento.getPaciente());
        consulta.setMedico(agenda.getMedico());
        consulta.setData(LocalDateTime.of(agenda.getData(), agenda.getHorario()));
        consultaRepository.save(consulta);
        agendamento.setStatus(StatusAgendamento.REALIZADO);
    }
}
