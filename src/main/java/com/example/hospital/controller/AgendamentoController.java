package com.example.hospital.controller;

import com.example.hospital.model.*;
import com.example.hospital.repository.AgendamentoRepository;
import com.example.hospital.repository.UsuarioRepository;
import com.example.hospital.services.AgendaService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/agendamentos")
public class AgendamentoController {

    private final AgendamentoRepository agendamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AgendaService agendaService;

    public AgendamentoController(AgendamentoRepository agendamentoRepository,
                                 UsuarioRepository usuarioRepository,
                                 AgendaService agendaService) {
        this.agendamentoRepository = agendamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.agendaService = agendaService;
    }

    @GetMapping("/list")
    public ModelAndView listar(@AuthenticationPrincipal UserDetails usuarioLogado, ModelMap model) {
        Usuario usuario = usuarioRepository.findByLogin(usuarioLogado.getUsername()).orElseThrow();
        boolean administrativo = temPerfil(usuarioLogado, "ADMIN", "SECRETARIA");
        List<Agendamento> agendamentos;
        if (administrativo) {
            agendamentos = agendamentoRepository.findAll();
        } else if (usuario.getPessoa() instanceof Paciente paciente) {
            agendamentos = agendamentoRepository.findByPacienteIdOrderByAgendaDataHoraDesc(paciente.getId());
        } else if (usuario.getPessoa() instanceof Medico medico) {
            agendamentos = agendamentoRepository.findByAgendaMedicoIdOrderByAgendaDataHoraAsc(medico.getId());
        } else {
            agendamentos = List.of();
        }

        model.addAttribute("agendamentos", agendamentos);
        model.addAttribute("administrativo", administrativo);
        return new ModelAndView("agendamentos/list", model);
    }

    @PostMapping("/agendar/{agendaId}")
    public ModelAndView agendar(@PathVariable Long agendaId,
                                @RequestParam(required = false) Long pacienteId,
                                @AuthenticationPrincipal UserDetails usuarioLogado,
                                RedirectAttributes attributes) {
        try {
            Usuario usuario = usuarioRepository.findByLogin(usuarioLogado.getUsername()).orElseThrow();
            if (usuario.getPessoa() instanceof Paciente paciente) {
                pacienteId = paciente.getId();
            } else if (!temPerfil(usuarioLogado, "ADMIN", "SECRETARIA") || pacienteId == null) {
                throw new IllegalArgumentException("Selecione um paciente para realizar o agendamento.");
            }
            agendaService.agendar(agendaId, pacienteId);
            attributes.addFlashAttribute("mensagem", "Agendamento realizado com sucesso.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            attributes.addFlashAttribute("erro", e.getMessage());
        }
        return new ModelAndView("redirect:/agenda/list");
    }

    @PostMapping("/confirmar/{id}")
    public ModelAndView confirmar(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            agendaService.confirmarPresenca(id);
            attributes.addFlashAttribute("mensagem", "Presença confirmada e consulta registrada.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            attributes.addFlashAttribute("erro", e.getMessage());
        }
        return new ModelAndView("redirect:/agendamentos/list");
    }

    @PostMapping("/valor/{id}")
    public ModelAndView atualizarValor(@PathVariable Long id,
                                       @RequestParam @NumberFormat BigDecimal valor,
                                       @AuthenticationPrincipal UserDetails usuarioLogado,
                                       RedirectAttributes attributes) {
        try {
            Usuario usuario = usuarioRepository.findByLogin(usuarioLogado.getUsername()).orElseThrow();
            Agendamento agendamento = agendamentoRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado."));
            if (!temPerfil(usuarioLogado, "ADMIN", "SECRETARIA")
                    && (!(usuario.getPessoa() instanceof Medico medico)
                    || !agendamento.getAgenda().getMedico().getId().equals(medico.getId()))) {
                throw new IllegalArgumentException("Você não tem permissão para alterar o valor deste agendamento.");
            }
            agendaService.atualizarValor(id, valor);
            attributes.addFlashAttribute("mensagem", "Valor da consulta atualizado.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            attributes.addFlashAttribute("erro", e.getMessage());
        }
        return new ModelAndView("redirect:/agendamentos/list");
    }

    @PostMapping("/cancelar/{id}")
    public ModelAndView cancelar(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails usuarioLogado,
                                 RedirectAttributes attributes) {
        try {
            Usuario usuario = usuarioRepository.findByLogin(usuarioLogado.getUsername()).orElseThrow();
            Agendamento agendamento = agendamentoRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado."));
            if (!temPerfil(usuarioLogado, "ADMIN", "SECRETARIA")
                    && !pertenceAoUsuario(agendamento, usuario.getPessoa())) {
                throw new IllegalArgumentException("Você não tem permissão para cancelar este agendamento.");
            }
            agendaService.cancelar(id);
            attributes.addFlashAttribute("mensagem", "Agendamento cancelado e horário liberado.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            attributes.addFlashAttribute("erro", e.getMessage());
        }
        return new ModelAndView("redirect:/agendamentos/list");
    }

    private boolean pertenceAoUsuario(Agendamento agendamento, Pessoa pessoa) {
        if (pessoa instanceof Paciente paciente) {
            return agendamento.getPaciente().getId().equals(paciente.getId());
        }
        if (pessoa instanceof Medico medico) {
            return agendamento.getAgenda().getMedico().getId().equals(medico.getId());
        }
        return false;
    }

    private boolean temPerfil(UserDetails usuario, String... perfis) {
        return usuario.getAuthorities().stream()
                .anyMatch(a -> List.of(perfis).contains(a.getAuthority().replace("ROLE_", "")));
    }
}
