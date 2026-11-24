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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AgendaService {

    private static final int MAX_HORARIOS_POR_OPERACAO = 10_000;
    private static final Set<Integer> DURACOES_PERMITIDAS = Set.of(15, 20, 30, 45, 60);

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
    public ResultadoDisponibilidade cadastrarHorarios(Long medicoId,
                                                        LocalDate dataInicio,
                                                        LocalDate dataFim,
                                                        Set<DayOfWeek> diasSemana,
                                                        LocalTime horaInicio,
                                                        LocalTime horaFim,
                                                        int duracaoMinutos) {
        if (dataInicio == null || dataFim == null || dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Informe um período de datas válido.");
        }
        if (dataInicio.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A data inicial não pode estar no passado.");
        }
        if (dataFim.isAfter(dataInicio.plusYears(1))) {
            throw new IllegalArgumentException("O período não pode ultrapassar um ano.");
        }
        if (diasSemana == null || diasSemana.isEmpty()) {
            throw new IllegalArgumentException("Selecione pelo menos um dia da semana.");
        }
        if (horaInicio == null || horaFim == null || !horaInicio.isBefore(horaFim)) {
            throw new IllegalArgumentException("O horário final deve ser posterior ao horário inicial.");
        }
        if (!DURACOES_PERMITIDAS.contains(duracaoMinutos)) {
            throw new IllegalArgumentException("Selecione uma duração disponível para a consulta.");
        }
        if (horaInicio.getSecond() != 0 || horaInicio.getNano() != 0
                || horaFim.getSecond() != 0 || horaFim.getNano() != 0) {
            throw new IllegalArgumentException("Informe horários com precisão de minutos.");
        }
        if (horaInicio.plusMinutes(duracaoMinutos).isAfter(horaFim)) {
            throw new IllegalArgumentException("A faixa de atendimento é menor que a duração da consulta.");
        }

        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado."));
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioPeriodo = dataInicio.atStartOfDay();
        LocalDateTime fimPeriodo = dataFim.plusDays(1).atStartOfDay().minusNanos(1);
        Set<LocalDateTime> horariosExistentes = new HashSet<>();
        agendaRepository.findByMedicoIdAndDataHoraBetween(medicoId, inicioPeriodo, fimPeriodo)
                .forEach(agenda -> horariosExistentes.add(agenda.getDataHora()));

        List<Agenda> novosHorarios = new ArrayList<>();
        int ignorados = 0;
        for (LocalDate data = dataInicio; !data.isAfter(dataFim); data = data.plusDays(1)) {
            if (!diasSemana.contains(data.getDayOfWeek())) {
                continue;
            }

            LocalTime horario = horaInicio;
            while (!horario.plusMinutes(duracaoMinutos).isAfter(horaFim)) {
                LocalDateTime dataHora = LocalDateTime.of(data, horario);
                if (dataHora.isBefore(agora)) {
                    horario = horario.plusMinutes(duracaoMinutos);
                    continue;
                }
                if (!horariosExistentes.add(dataHora)) {
                    ignorados++;
                    horario = horario.plusMinutes(duracaoMinutos);
                    continue;
                }

                Agenda agenda = new Agenda();
                agenda.setMedico(medico);
                agenda.setDataHora(dataHora);
                agenda.setStatus(StatusAgenda.DISPONIVEL);
                novosHorarios.add(agenda);
                if (novosHorarios.size() > MAX_HORARIOS_POR_OPERACAO) {
                    throw new IllegalArgumentException("A geração não pode criar mais de 10.000 horários por operação.");
                }
                horario = horario.plusMinutes(duracaoMinutos);
            }
        }

        agendaRepository.saveAll(novosHorarios);
        return new ResultadoDisponibilidade(novosHorarios.size(), ignorados);
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

    public record ResultadoDisponibilidade(int criados, int ignorados) {
    }
}
