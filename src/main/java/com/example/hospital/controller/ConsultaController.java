package com.example.hospital.controller;

import ch.qos.logback.core.model.Model;
import com.example.hospital.model.*;
import com.example.hospital.repository.ConsultaRepository;
import com.example.hospital.repository.MedicoRepository;
import com.example.hospital.repository.PacienteRepository;
import com.example.hospital.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @Autowired
    UsuarioRepository usuarioRepository;

    @GetMapping("/form")
    public ModelAndView form(ModelMap model) {
        // 1. Envia a consulta vazia para o th:object="${consulta}" não quebrar
        model.addAttribute("consulta", new Consulta());

        // 2. Busca as listas no banco de dados para preencher os <select>
        model.addAttribute("medicos", medicoRepository.findAll());
        model.addAttribute("pacientes", pacienteRepository.findAll());

        return new ModelAndView("consultas/form", model);
    }

    @GetMapping("/list")
    public ModelAndView consultas(@AuthenticationPrincipal UserDetails usuarioLogado, ModelMap model){
        Usuario usuario = usuarioRepository.findByLogin(usuarioLogado.getUsername()).orElseThrow();

        Pessoa pessoa = usuario.getPessoa();
        List<Consulta> consultas;

        if (pessoa instanceof Medico){
            consultas = repository.findByMedicoId(pessoa.getId());
        } else if (pessoa instanceof Paciente){
            consultas = repository.findByPacienteId(pessoa.getId());
        } else {
            consultas = repository.findAll();
        }

        model.addAttribute("consulta", consultas);
        return new ModelAndView("consultas/list", model);
    }

    @PostMapping("/save")
    public ModelAndView salvar(@Valid @ModelAttribute("consulta") Consulta consulta,
                               BindingResult result, RedirectAttributes attributes){
        if (result.hasErrors()) {
            return new ModelAndView("consultas/form");
        }

        repository.save(consulta);
        attributes.addFlashAttribute("mensagem", "Consulta cadastrada com sucesso.");
        return new ModelAndView("redirect:/consultas/list");

    }

    @GetMapping("/edit/{id}")
    public ModelAndView edit(@PathVariable("id") Long id,ModelMap model){
        model.addAttribute("consulta", repository.findById(id));
        model.addAttribute("medicos", medicoRepository.findById(id));
        model.addAttribute("pacientes", pacienteRepository.findById(id));
        return new ModelAndView("consultas/form",model);
    }

    @GetMapping("/remove/{id}")
    public ModelAndView deletar(@PathVariable("id") Long id){
        repository.deleteById(id);
        return new ModelAndView("redirect:/consultas/list");
    }
}
