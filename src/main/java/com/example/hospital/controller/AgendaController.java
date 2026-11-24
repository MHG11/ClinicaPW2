package com.example.hospital.controller;

import com.example.hospital.model.Agenda;
import com.example.hospital.model.Medico;
import com.example.hospital.model.StatusAgenda;
import com.example.hospital.model.Usuario;
import com.example.hospital.repository.AgendaRepository;
import com.example.hospital.repository.MedicoRepository;
import com.example.hospital.repository.PacienteRepository;
import com.example.hospital.repository.UsuarioRepository;
import com.example.hospital.services.AgendaService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/agenda")
public class AgendaController {

    private final AgendaRepository agendaRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final AgendaService agendaService;

    public AgendaController(AgendaRepository agendaRepository,
                            MedicoRepository medicoRepository,
                            PacienteRepository pacienteRepository,
                            UsuarioRepository usuarioRepository,
                            AgendaService agendaService) {
        this.agendaRepository = agendaRepository;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.agendaService = agendaService;
    }

    @GetMapping("/list")
    public ModelAndView listar(@AuthenticationPrincipal UserDetails usuarioLogado, ModelMap model) {
        Usuario usuario = usuarioRepository.findByLogin(usuarioLogado.getUsername()).orElseThrow();
        List<Agenda> horarios;
        if (usuario.getPessoa() instanceof Medico medico
                && temPerfil(usuarioLogado, "MEDICO")
                && !temPerfil(usuarioLogado, "ADMIN", "SECRETARIA")) {
            horarios = agendaRepository.findByMedicoIdAndStatusOrderByDataHoraAsc(
                    medico.getId(), StatusAgenda.DISPONIVEL);
        } else {
            horarios = agendaRepository.findByStatusOrderByDataHoraAsc(StatusAgenda.DISPONIVEL);
        }
        model.addAttribute("horarios", horarios);
        boolean podeSelecionarPaciente = temPerfil(usuarioLogado, "ADMIN", "SECRETARIA");
        model.addAttribute("pacientes", podeSelecionarPaciente ? pacienteRepository.findAll() : List.of());
        return new ModelAndView("agenda/list", model);
    }

    @GetMapping("/form")
    public ModelAndView form(@AuthenticationPrincipal UserDetails usuarioLogado, ModelMap model) {
        if (temPerfil(usuarioLogado, "ADMIN", "SECRETARIA")) {
            model.addAttribute("medicos", medicoRepository.findAll());
        } else {
            Usuario usuario = usuarioRepository.findByLogin(usuarioLogado.getUsername()).orElseThrow();
            if (usuario.getPessoa() instanceof Medico medico) {
                model.addAttribute("medicos", List.of(medico));
            } else {
                model.addAttribute("medicos", List.of());
                model.addAttribute("erro", "Seu usuário não está vinculado a um cadastro de médico.");
            }
        }
        model.addAttribute("diasSemana", List.of(
                new DiaSemanaOpcao("MONDAY", "Segunda-feira"),
                new DiaSemanaOpcao("TUESDAY", "Terça-feira"),
                new DiaSemanaOpcao("WEDNESDAY", "Quarta-feira"),
                new DiaSemanaOpcao("THURSDAY", "Quinta-feira"),
                new DiaSemanaOpcao("FRIDAY", "Sexta-feira"),
                new DiaSemanaOpcao("SATURDAY", "Sábado"),
                new DiaSemanaOpcao("SUNDAY", "Domingo")));
        model.addAttribute("dataMinima", LocalDate.now());
        return new ModelAndView("agenda/form", model);
    }

    @PostMapping("/cadastrar")
    public ModelAndView cadastrar(@RequestParam(required = false) Long medicoId,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
                                  @RequestParam(required = false) Set<DayOfWeek> diasSemana,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFim,
                                  @RequestParam int duracaoMinutos,
                                  @AuthenticationPrincipal UserDetails usuarioLogado,
                                  RedirectAttributes attributes) {
        try {
            if (!temPerfil(usuarioLogado, "ADMIN", "SECRETARIA")) {
                Usuario usuario = usuarioRepository.findByLogin(usuarioLogado.getUsername()).orElseThrow();
                if (!(usuario.getPessoa() instanceof Medico medico)) {
                    throw new IllegalArgumentException("Seu usuário não está vinculado a um cadastro de médico.");
                }
                medicoId = medico.getId();
            } else if (medicoId == null) {
                throw new IllegalArgumentException("Selecione um médico.");
            }
            AgendaService.ResultadoDisponibilidade resultado = agendaService.cadastrarHorarios(
                    medicoId, dataInicio, dataFim, diasSemana, horaInicio, horaFim, duracaoMinutos);
            attributes.addFlashAttribute("mensagem",
                    resultado.criados() + " horário(s) disponibilizado(s); "
                            + resultado.ignorados() + " duplicado(s) ignorado(s).");
        } catch (IllegalArgumentException | IllegalStateException e) {
            attributes.addFlashAttribute("erro", e.getMessage());
            return new ModelAndView("redirect:/agenda/form");
        } catch (DataIntegrityViolationException e) {
            attributes.addFlashAttribute("erro",
                    "Não foi possível concluir a geração por conflito com outro cadastro simultâneo. Tente novamente.");
            return new ModelAndView("redirect:/agenda/form");
        }
        return new ModelAndView("redirect:/agenda/list");
    }

    private boolean temPerfil(UserDetails usuario, String... perfis) {
        return usuario.getAuthorities().stream()
                .anyMatch(a -> List.of(perfis).contains(a.getAuthority().replace("ROLE_", "")));
    }

    public record DiaSemanaOpcao(String codigo, String nome) {
    }
}
