package com.example.hospital.controller;

import com.example.hospital.model.Consulta;
import com.example.hospital.model.Paciente;
import com.example.hospital.repository.ConsultaRepository;
import com.example.hospital.repository.PacienteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Transactional
@Controller
@RequestMapping("pacientes")
public class PacienteController {

    @Autowired
    PacienteRepository repository;

    @GetMapping("/form")
    public String form(@ModelAttribute("pacientes") Paciente pacientes){
        return "pacientes/form";
    }

    @GetMapping("/list")
    public ModelAndView pacientes(ModelMap model){
        model.addAttribute("pacientes", repository.findAll());
        return new ModelAndView("pacientes/list", model);
    }

    @PostMapping("/save")
    public ModelAndView salvar(Paciente paciente){
        repository.save(paciente);
        return new ModelAndView("redirect:/pacientes/list");
    }

    @GetMapping("/edit/{id}")
    public ModelAndView edit(@PathVariable("id") Long id, ModelMap model){
        model.addAttribute("pacientes", repository.findById(id));
        return new ModelAndView("pacientes/form",model);
    }

    @GetMapping("/remove/{id}")
    public ModelAndView deletar(@PathVariable("id") Long id){
        repository.deleteById(id);
        return new ModelAndView("redirect:/pacientes/list");
    }

}
