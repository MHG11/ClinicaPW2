package com.example.hospital.controller;

import com.example.hospital.model.Medico;
import com.example.hospital.repository.MedicoRepository;
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
    public ModelAndView salvar(@Valid @ModelAttribute("medicos") Medico medico, BindingResult result,
                               RedirectAttributes attributes){
        if (result.hasErrors()) {
            return new ModelAndView("medicos/form");
        }

        repository.save(medico);
        attributes.addFlashAttribute("mensagem", "Médico cadastrado com sucesso.");
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
