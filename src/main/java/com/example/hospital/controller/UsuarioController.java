package com.example.hospital.controller;

import com.example.hospital.model.Usuario;
import com.example.hospital.repository.UsuarioRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/cadastro")
    public String form() {
        return "login/cadastro";
    }

    @GetMapping("/dados")
    public String dados(@AuthenticationPrincipal UserDetails usuarioLogado, Model model) {
        Usuario usuario = repository.findByLogin(usuarioLogado.getUsername())
                .orElseThrow();
        model.addAttribute("usuario", usuario);
        return "usuario/dados";
        
    }

    @PostMapping("/cadastro")
    public String cadastro(@RequestParam String login, @RequestParam String senha, Model model){

        if (repository.existsByLogin(login)) {
            model.addAttribute("erro", "Usuário já existe.");
            return "login/cadastro";
        }

        Usuario u = new Usuario();
        u.setLogin(login);
        u.setPassword(passwordEncoder.encode(senha));
        u.setAtivo(true);
        repository.save(u);
        return"redirect:/login";

    }
}
