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

    public MedicoController(){
        repository = new MedicoRepository();
    }

    @GetMapping("/form")
    public String form(@ModelAttribute("medicos") Medico medicos){
        return "medicos/form";
    }

    @GetMapping("/list")
    public ModelAndView medicos(ModelMap model){
        model.addAttribute("medicos", repository.medicos());
        return new ModelAndView("medicos/list", model);
    }

    @PostMapping("/save")
    public ModelAndView salvar(Medico medico){
        repository.save(medico);
        return new ModelAndView("medicos/list");
    }

    @GetMapping("/edit/{id}")
    public ModelAndView edit(@PathVariable("id") Long id, ModelMap model){
        model.addAttribute("medicos", repository.medico(id));
        return new ModelAndView("medicos/form",model);
    }

    @GetMapping("/remove/{id}")
    public ModelAndView deletar(@PathVariable("id") Long id){
        repository.remove(id);
        return new ModelAndView("redirect:/medicos/list");
    }

    @PostMapping("/update")
    public ModelAndView update(Medico medico){
        repository.update(medico);
        return new ModelAndView("redirect:/medicos/list");
    }


}
