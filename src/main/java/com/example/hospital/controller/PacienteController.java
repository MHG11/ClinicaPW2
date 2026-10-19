package com.example.hospital.controller;

import com.example.hospital.model.Consulta;
import com.example.hospital.model.Paciente;
import com.example.hospital.repository.ConsultaRepository;
import com.example.hospital.repository.PacienteRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Transactional
@Controller
@RequestMapping("pacientes")
public class PacienteController {

    @Autowired
    PacienteRepository repository;

    @GetMapping("/form")
    public String form(@ModelAttribute("paciente") Paciente pacientes){
        return "pacientes/form";
    }

    @GetMapping("/list")
    public ModelAndView pacientes(ModelMap model){
        model.addAttribute("paciente", repository.findAll());
        return new ModelAndView("pacientes/list", model);
    }

    @PostMapping("/save")
    public ModelAndView salvar(@Valid @ModelAttribute("paciente") Paciente paciente,
                               BindingResult result, RedirectAttributes attributes){
        if (result.hasErrors()) {
            return new ModelAndView("pacientes/form");
        }
        repository.save(paciente);
        attributes.addFlashAttribute("mensagem", "Paciente cadastrado com sucesso.");
        return new ModelAndView("redirect:/pacientes/list");
    }

    @GetMapping("/edit/{id}")
    public ModelAndView edit(@PathVariable("id") Long id, ModelMap model){
        model.addAttribute("paciente", repository.findById(id));
        return new ModelAndView("pacientes/form",model);
    }

    @GetMapping("/remove/{id}")
    public ModelAndView deletar(@PathVariable("id") Long id){
        repository.deleteById(id);
        return new ModelAndView("redirect:/pacientes/list");
    }

}
