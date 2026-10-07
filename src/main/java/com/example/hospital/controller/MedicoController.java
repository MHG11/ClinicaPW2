package com.example.hospital.controller;

import com.example.hospital.model.Medico;
import com.example.hospital.repository.MedicoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Transactional
@Controller
@RequestMapping("medicos")
public class MedicoController {

    @Autowired
    MedicoRepository repository;

    @GetMapping("/form")
    public String form(@ModelAttribute("medicos") Medico medicos){
        return "medicos/form";
    }

    @GetMapping("/list")
    public ModelAndView medicos(ModelMap model){
        model.addAttribute("medicos", repository.findAll());
        return new ModelAndView("medicos/list", model);
    }

    @PostMapping("/save")
    public ModelAndView salvar(Medico medico){
        repository.save(medico);
        return new ModelAndView("redirect:/medicos/list");
    }

    @GetMapping("/edit/{id}")
    public ModelAndView edit(@PathVariable("id") Long id, ModelMap model){
        model.addAttribute("medicos", repository.findById(id));
        return new ModelAndView("medicos/form",model);
    }

    @GetMapping("/remove/{id}")
    public ModelAndView deletar(@PathVariable("id") Long id){
        repository.deleteById(id);
        return new ModelAndView("redirect:/medicos/list");
    }
}
