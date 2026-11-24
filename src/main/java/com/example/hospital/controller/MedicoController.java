package com.example.hospital.controller;

import com.example.hospital.model.Medico;
import com.example.hospital.model.Usuario;
import com.example.hospital.repository.MedicoRepository;
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
@RequestMapping("medicos")
public class MedicoController {

    @Autowired
    MedicoRepository repository;

    @Autowired
    UsuarioRepository usuarioRepository;

    private List<Usuario> usuariosSemPessoa() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getPessoa() == null)
                .toList();
    }

    @GetMapping("/form")
    public ModelAndView form(ModelMap model) {
        model.addAttribute("medicos", new Medico());
        model.addAttribute("usuarios", usuariosSemPessoa());
        return new ModelAndView("medicos/form", model);
    }

    @GetMapping("/list")
    public ModelAndView medicos(ModelMap model) {
        model.addAttribute("medicos", repository.findAll());
        model.addAttribute("usuarios", usuariosSemPessoa());
        return new ModelAndView("medicos/list", model);
    }

    @PostMapping("/save")
    public ModelAndView salvar(@Valid @ModelAttribute("medicos") Medico medico,
                               BindingResult result,
                               @RequestParam(required = false) Long usuarioId,
                               ModelMap model,
                               RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("usuarios", usuariosSemPessoa());
            return new ModelAndView("medicos/form", model);
        }

        if (usuarioId != null) {
            Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
            usuario.setPessoa(medico);        // cascade salva o Medico
            usuarioRepository.save(usuario);
        } else {
            repository.save(medico);          // médico sem login
        }

        attributes.addFlashAttribute("mensagem", "Médico cadastrado com sucesso.");
        return new ModelAndView("redirect:/medicos/list");
    }

    @PostMapping("/vincular")
    public String vincular(@RequestParam Long medicoId, @RequestParam Long usuarioId) {
        if (usuarioRepository.existsByPessoaId(medicoId)) {
            return "redirect:/medicos/list"; // já tem usuário
        }
        Medico medico = repository.findById(medicoId).orElseThrow();
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        usuario.setPessoa(medico);
        usuarioRepository.save(usuario);
        return "redirect:/medicos/list";
    }

    @GetMapping("/edit/{id}")
    public ModelAndView edit(@PathVariable("id") Long id, ModelMap model, RedirectAttributes attributes) {
        Medico medico = repository.findById(id).orElse(null);
        if (medico == null) {
            attributes.addFlashAttribute("erro", "Médico não encontrado.");
            return new ModelAndView("redirect:/medicos/list");
        }
        model.addAttribute("medicos", medico);
    public ModelAndView edit(@PathVariable("id") Long id, ModelMap model) {
        model.addAttribute("medicos", repository.findById(id));
        model.addAttribute("usuarios", usuariosSemPessoa());
        return new ModelAndView("medicos/form", model);
    }

    @GetMapping("/remove/{id}")
    public ModelAndView deletar(@PathVariable("id") Long id) {
        repository.deleteById(id);
        return new ModelAndView("redirect:/medicos/list");
    }
}
