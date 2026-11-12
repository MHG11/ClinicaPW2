package com.example.hospital.controller;

import com.example.hospital.model.Perfil;
import com.example.hospital.model.Usuario;
import com.example.hospital.repository.PerfilRepository;
import com.example.hospital.repository.UsuarioRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    private final PerfilRepository perfilRepository;

    @GetMapping("/perfis")
    public String telaPerfis(Model model) {
        model.addAttribute("usuarios", repository.findAll());
        model.addAttribute("perfis", perfilRepository.findAll());
        return "usuario/perfis";
    }

    @PostMapping("/{id}/perfil")
    public String associarPerfil(@PathVariable Long id,
                                 @RequestParam Long perfilId,
                                 RedirectAttributes redirect) {
        Usuario u = repository.findById(id).orElseThrow();
        Perfil p = perfilRepository.findById(perfilId).orElseThrow();

        u.getPerfis().clear(); 
        u.getPerfis().add(p);
        repository.save(u);

        redirect.addFlashAttribute("msg", "Perfil de " + u.getLogin() + " atualizado.");
        return "redirect:/usuario/perfis";
    }

    public UsuarioController(UsuarioRepository repository, PasswordEncoder passwordEncoder, PerfilRepository perfilRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.perfilRepository = perfilRepository;
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
