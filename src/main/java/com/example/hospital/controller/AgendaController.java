package com.example.hospital.controller;

import com.example.hospital.model.Agenda;
import com.example.hospital.repository.MedicoRepository;
import com.example.hospital.services.AgendaService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequestMapping("/agenda")
public class AgendaController {

    private final AgendaService agendaService;
    private final MedicoRepository medicoRepository;

    public AgendaController(AgendaService agendaService, MedicoRepository medicoRepository) {
        this.agendaService = agendaService;
        this.medicoRepository = medicoRepository;
    }

    @GetMapping({"", "/", "/disponiveis"})
    public ModelAndView disponiveis(ModelMap model) {
        model.addAttribute("horarios", agendaService.horariosDisponiveis());
        return new ModelAndView("agenda/disponiveis", model);
    }

    @GetMapping("/list")
    public ModelAndView listar(ModelMap model) {
        model.addAttribute("horarios", agendaService.horarios());
        return new ModelAndView("agenda/list", model);
    }

    @GetMapping("/form")
    public ModelAndView form(ModelMap model) {
        model.addAttribute("agenda", new Agenda());
        model.addAttribute("medicos", medicoRepository.findAll());
        return new ModelAndView("agenda/form", model);
    }

    @PostMapping("/save")
    public ModelAndView salvar(@RequestParam Long medicoId,
                               @RequestParam LocalDate data,
                               @RequestParam LocalTime horario,
                               RedirectAttributes attributes) {
        try {
            agendaService.disponibilizarHorario(medicoId, data, horario);
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("erro", exception.getMessage());
            return new ModelAndView("redirect:/agenda/form");
        } catch (DataIntegrityViolationException exception) {
            attributes.addFlashAttribute("erro",
                    "Este médico já possui um horário cadastrado nessa data e hora.");
            return new ModelAndView("redirect:/agenda/form");
        }
        attributes.addFlashAttribute("mensagem", "Horário disponibilizado com sucesso.");
        return new ModelAndView("redirect:/agenda/list");
    }
}
