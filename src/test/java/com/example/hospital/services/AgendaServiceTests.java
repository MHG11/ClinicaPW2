package com.example.hospital.services;

import com.example.hospital.model.Agenda;
import com.example.hospital.model.Medico;
import com.example.hospital.repository.AgendaRepository;
import com.example.hospital.repository.AgendamentoRepository;
import com.example.hospital.repository.ConsultaRepository;
import com.example.hospital.repository.MedicoRepository;
import com.example.hospital.repository.PacienteRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgendaServiceTests {

    @Test
    void geraBlocosPartindoDoHorarioInicialInformado() {
        AgendaRepository agendaRepository = mock(AgendaRepository.class);
        MedicoRepository medicoRepository = mock(MedicoRepository.class);
        LocalDate data = LocalDate.now().plusDays(3);

        when(medicoRepository.findById(1L)).thenReturn(Optional.of(new Medico()));
        when(agendaRepository.findByMedicoIdAndDataHoraBetween(eq(1L), any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(List.of());

        AgendaService service = new AgendaService(
                agendaRepository,
                mock(AgendamentoRepository.class),
                medicoRepository,
                mock(PacienteRepository.class),
                mock(ConsultaRepository.class));

        AgendaService.ResultadoDisponibilidade resultado = service.cadastrarHorarios(
                1L,
                data,
                data,
                Set.of(data.getDayOfWeek()),
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                30);

        assertEquals(4, resultado.criados());
        verify(agendaRepository).saveAll(argThat(horarios -> {
            List<LocalTime> horasGeradas = StreamSupport.stream(horarios.spliterator(), false)
                    .map(Agenda::getDataHora)
                    .map(LocalDateTime::toLocalTime)
                    .toList();
            assertEquals(List.of(
                    LocalTime.of(8, 0),
                    LocalTime.of(8, 30),
                    LocalTime.of(9, 0),
                    LocalTime.of(9, 30)), horasGeradas);
            return true;
        }));
    }
}
