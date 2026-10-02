package com.example.hospital.controller;

import ch.qos.logback.core.model.Model;
import com.example.hospital.model.Consulta;
import com.example.hospital.model.Medico;
import com.example.hospital.model.Paciente;
import com.example.hospital.repository.ConsultaRepository;
import com.example.hospital.repository.MedicoRepository;
import com.example.hospital.repository.PacienteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Transactional
@Controller
@RequestMapping("consultas")
public class ConsultaController {

    @Autowired
    ConsultaRepository repository;

    @Autowired
    PacienteRepository pacienteRepository;

    @Autowired
    MedicoRepository medicoRepository;

    public ConsultaController(){
        repository = new ConsultaRepository();
        pacienteRepository = new PacienteRepository();
        medicoRepository = new MedicoRepository();
    }

    @GetMapping("/form")
    public ModelAndView form(ModelMap model) {
        // 1. Envia a consulta vazia para o th:object="${consulta}" não quebrar
        model.addAttribute("consulta", new Consulta());

        // 2. Busca as listas no banco de dados para preencher os <select>
        model.addAttribute("medicos", medicoRepository.medicos());
        model.addAttribute("pacientes", pacienteRepository.pacientes());

        return new ModelAndView("consultas/form", model);
    }

    @GetMapping("/list")
    public ModelAndView consultas(ModelMap model){
        model.addAttribute("consulta", repository.consultas());
        return new ModelAndView("consultas/list", model);
    }

    @PostMapping("/save")
    public ModelAndView salvar(Consulta consulta){
        repository.save(consulta);
        return new ModelAndView("consultas/list");
    }

    @GetMapping("/edit/{id}")
    public ModelAndView edit(@PathVariable("id") Long id,ModelMap model){
        model.addAttribute("consulta", repository.consulta(id));
        model.addAttribute("medicos", medicoRepository.medicos());
        model.addAttribute("pacientes", pacienteRepository.pacientes());
        return new ModelAndView("consultas/form",model);
    }

    @GetMapping("/remove/{id}")
    public ModelAndView deletar(@PathVariable("id") Long id){
        repository.remove(id);
        return new ModelAndView("redirect:/consultas/list");
    }

    @PostMapping("/update")
    public ModelAndView update(Consulta consulta){
        repository.update(consulta);
        return new ModelAndView("redirect:/consultas/list");
    }

}
