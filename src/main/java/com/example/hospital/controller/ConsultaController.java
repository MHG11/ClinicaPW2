package com.example.hospital.controller;

import com.example.hospital.model.*;
import com.example.hospital.repository.ConsultaRepository;
import com.example.hospital.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("consultas")
public class ConsultaController {

    private final ConsultaRepository repository;
    private final UsuarioRepository usuarioRepository;

    public ConsultaController(ConsultaRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/list")
    public ModelAndView consultas(@AuthenticationPrincipal UserDetails usuarioLogado, ModelMap model){
        Usuario usuario = usuarioRepository.findByLogin(usuarioLogado.getUsername()).orElseThrow();

        Pessoa pessoa = usuario.getPessoa();
        List<Consulta> consultas;

        boolean administrativo = usuarioLogado.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_SECRETARIA"));

        if (pessoa instanceof Medico) {
            consultas = repository.findByMedicoId(pessoa.getId());
        } else if (pessoa instanceof Paciente) {
            consultas = repository.findByPacienteId(pessoa.getId());
        } else if (administrativo) {
            consultas = repository.findAll();
        } else {
            consultas = List.of();
        }

        model.addAttribute("consulta", consultas);
        model.addAttribute("medicoLogado", pessoa instanceof Medico);
        return new ModelAndView("consultas/list", model);
    }

    @PostMapping("/{id}/observacao")
    @Transactional
    public ModelAndView atualizarObservacao(@PathVariable Long id,
                                            @RequestParam(required = false) String observacao,
                                            @AuthenticationPrincipal UserDetails usuarioLogado,
                                            RedirectAttributes attributes) {
        Usuario usuario = usuarioRepository.findByLogin(usuarioLogado.getUsername()).orElseThrow();
        Consulta consulta = repository.findById(id).orElse(null);
        if (consulta == null) {
            attributes.addFlashAttribute("erro", "Consulta não encontrada.");
            return new ModelAndView("redirect:/consultas/list");
        }
        if (!(usuario.getPessoa() instanceof Medico medico)
                || !consulta.getMedico().getId().equals(medico.getId())) {
            throw new AccessDeniedException("Somente o médico responsável pode alterar a observação.");
        }
        if (observacao != null && observacao.length() > 255) {
            attributes.addFlashAttribute("erro", "A observação deve ter no máximo 255 caracteres.");
            return new ModelAndView("redirect:/consultas/list");
        }

        consulta.setObservacao(observacao == null || observacao.isBlank() ? null : observacao.trim());
        repository.save(consulta);
        attributes.addFlashAttribute("mensagem", "Observação da consulta salva com sucesso.");
        return new ModelAndView("redirect:/consultas/list");
    }
}
