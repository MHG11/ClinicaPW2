package com.example.hospital.controller;

import com.example.hospital.model.Paciente;
import com.example.hospital.model.Usuario;
import com.example.hospital.repository.PacienteRepository;
import com.example.hospital.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Transactional
@Controller
@RequestMapping("pacientes")
public class PacienteController {

    @Autowired
    PacienteRepository repository;
    @Autowired
    UsuarioRepository usuarioRepository;

    private List<Usuario> usuariosSemPessoa() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getPessoa() == null)
                .toList();
    }

    @GetMapping("/form")
    public ModelAndView form(ModelMap model) {
        model.addAttribute("paciente", new Paciente());
        model.addAttribute("usuarios", usuariosSemPessoa());
        return new ModelAndView("pacientes/form", model);
    }

    @GetMapping("/list")
    public ModelAndView pacientes(ModelMap model) {
        model.addAttribute("paciente", repository.findAll());
        model.addAttribute("usuarios", usuariosSemPessoa());
        return new ModelAndView("pacientes/list", model);
    }

    @PostMapping("/save")
    public ModelAndView salvar(@Valid @ModelAttribute("paciente") Paciente paciente,
                               BindingResult result,
                               @RequestParam(required = false) Long usuarioId,
                               ModelMap model) {
        if (result.hasErrors()) {
            model.addAttribute("usuarios", usuariosSemPessoa());
            return new ModelAndView("pacientes/form", model);
        }

        if (usuarioId != null) {
            Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
            usuario.setPessoa(paciente);      // cascade salva o Paciente
            usuarioRepository.save(usuario);
        } else {
            repository.save(paciente);        // paciente sem login
        }
        return new ModelAndView("redirect:/pacientes/list");
    }

    @PostMapping("/vincular")
    public String vincular(@RequestParam Long pacienteId, @RequestParam Long usuarioId) {
        if (usuarioRepository.existsByPessoaId(pacienteId)) {
            return "redirect:/pacientes/list"; // já tem usuário
        }
        Paciente paciente = repository.findById(pacienteId).orElseThrow();
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        usuario.setPessoa(paciente);
        usuarioRepository.save(usuario);
        return "redirect:/pacientes/list";
    }

    @GetMapping("/edit/{id}")
    public ModelAndView edit(@PathVariable("id") Long id,
                             ModelMap model,
                             RedirectAttributes attributes) {
        Paciente paciente = repository.findById(id).orElse(null);

        if (paciente == null) {
            attributes.addFlashAttribute("erro", "Paciente não encontrado.");
            return new ModelAndView("redirect:/pacientes/list");
        }

        model.addAttribute("paciente", paciente);
        model.addAttribute("usuarios", usuariosSemPessoa());
        return new ModelAndView("pacientes/form", model);
    }

    @GetMapping("/remove/{id}")
    public ModelAndView deletar(@PathVariable("id") Long id){
        repository.deleteById(id);
        return new ModelAndView("redirect:/pacientes/list");
    }

}
