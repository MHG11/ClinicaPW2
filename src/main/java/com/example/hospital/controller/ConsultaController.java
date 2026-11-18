package com.example.hospital.controller;

import com.example.hospital.repository.ConsultaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaRepository repository;

    public ConsultaController(ConsultaRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/list")
    public ModelAndView consultas(ModelMap model) {
        model.addAttribute("consulta", repository.findAll());
        return new ModelAndView("consultas/list", model);
    }
}
