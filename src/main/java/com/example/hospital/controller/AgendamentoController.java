package com.example.hospital.controller;

import com.example.hospital.model.Agenda;
import com.example.hospital.model.StatusAgenda;
import com.example.hospital.repository.AgendaRepository;
import com.example.hospital.repository.PacienteRepository;
import com.example.hospital.services.AgendaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/agendamentos")
public class AgendamentoController {

    private final AgendaService agendaService;
    private final AgendaRepository agendaRepository;
    private final PacienteRepository pacienteRepository;

    public AgendamentoController(AgendaService agendaService,
                                 AgendaRepository agendaRepository,
                                 PacienteRepository pacienteRepository) {
        this.agendaService = agendaService;
        this.agendaRepository = agendaRepository;
        this.pacienteRepository = pacienteRepository;
    }

    @GetMapping("/form/{agendaId}")
    public ModelAndView form(@PathVariable Long agendaId, ModelMap model,
                             RedirectAttributes attributes) {
        Agenda agenda = agendaRepository.findById(agendaId).orElse(null);
        if (agenda == null || agenda.getStatus() != StatusAgenda.DISPONIVEL) {
            attributes.addFlashAttribute("erro", "Este horário não está mais disponível.");
            return new ModelAndView("redirect:/agenda/disponiveis");
        }
        model.addAttribute("agenda", agenda);
        model.addAttribute("pacientes", pacienteRepository.findAll());
        return new ModelAndView("agendamentos/form", model);
    }

    @PostMapping("/save")
    public ModelAndView salvar(@RequestParam Long agendaId, @RequestParam Long pacienteId,
                               RedirectAttributes attributes) {
        try {
            agendaService.agendar(agendaId, pacienteId);
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("erro", exception.getMessage());
            return new ModelAndView("redirect:/agenda/disponiveis");
        }
        attributes.addFlashAttribute("mensagem", "Agendamento realizado com sucesso.");
        return new ModelAndView("redirect:/agendamentos/list");
    }

    @GetMapping("/list")
    public ModelAndView listar(ModelMap model) {
        model.addAttribute("agendamentos", agendaService.agendamentos());
        return new ModelAndView("agendamentos/list", model);
    }

    @PostMapping("/{id}/cancelar")
    public ModelAndView cancelar(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            agendaService.cancelar(id);
            attributes.addFlashAttribute("mensagem", "Agendamento cancelado e horário liberado.");
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("erro", exception.getMessage());
        }
        return new ModelAndView("redirect:/agendamentos/list");
    }

    @PostMapping("/{id}/confirmar")
    public ModelAndView confirmar(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            agendaService.confirmarPresenca(id);
            attributes.addFlashAttribute("mensagem", "Presença confirmada e consulta registrada.");
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("erro", exception.getMessage());
        }
        return new ModelAndView("redirect:/agendamentos/list");
    }
}
