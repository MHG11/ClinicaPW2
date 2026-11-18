package com.example.hospital.controller;

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

import java.time.LocalDateTime;
import java.util.List;

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
        model.addAttribute("horarios", agendaRepository.findByStatusOrderByDataHoraAsc(StatusAgenda.DISPONIVEL));
        boolean administrativo = usuarioLogado.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_SECRETARIA"));
        model.addAttribute("pacientes", administrativo ? pacienteRepository.findAll() : List.of());
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
        return new ModelAndView("agenda/form", model);
    }

    @PostMapping("/cadastrar")
    public ModelAndView cadastrar(@RequestParam(required = false) Long medicoId,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataHora,
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
            agendaService.cadastrarHorario(medicoId, dataHora);
            attributes.addFlashAttribute("mensagem", "Horário disponibilizado com sucesso.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            attributes.addFlashAttribute("erro", e.getMessage());
            return new ModelAndView("redirect:/agenda/form");
        } catch (DataIntegrityViolationException e) {
            attributes.addFlashAttribute("erro", "Este médico já possui um horário cadastrado nessa data e hora.");
            return new ModelAndView("redirect:/agenda/form");
        }
        return new ModelAndView("redirect:/agenda/list");
    }

    private boolean temPerfil(UserDetails usuario, String... perfis) {
        return usuario.getAuthorities().stream()
                .anyMatch(a -> List.of(perfis).contains(a.getAuthority().replace("ROLE_", "")));
    }
}
