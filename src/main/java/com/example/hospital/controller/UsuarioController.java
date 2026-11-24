package com.example.hospital.controller;

import com.example.hospital.model.Perfil;
import com.example.hospital.model.StatusCadastroMedico;
import com.example.hospital.model.Usuario;
import com.example.hospital.repository.PerfilRepository;
import com.example.hospital.repository.UsuarioRepository;
import com.example.hospital.services.CadastroUsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioRepository repository;
    private final PerfilRepository perfilRepository;
    private final CadastroUsuarioService cadastroUsuarioService;

    public UsuarioController(UsuarioRepository repository,
                             PerfilRepository perfilRepository,
                             CadastroUsuarioService cadastroUsuarioService) {
        this.repository = repository;
        this.perfilRepository = perfilRepository;
        this.cadastroUsuarioService = cadastroUsuarioService;
    }

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

    @GetMapping("/lista")
    public String listar(Model model) {
        model.addAttribute("usuarios", repository.findAll());
        return "usuario/lista";
    }

    @GetMapping("/dados")
    public String dados(@AuthenticationPrincipal UserDetails usuarioLogado, Model model) {
        Usuario usuario = repository.findByLogin(usuarioLogado.getUsername())
                .orElseThrow();
        model.addAttribute("usuario", usuario);
        return "lista";

    }

    @PostMapping("/cadastro")
    public String cadastro(@RequestParam String login,
                           @RequestParam String senha,
                           @RequestParam String tipoCadastro,
                           @RequestParam String nome,
                           @RequestParam String cpf,
                           @RequestParam(required = false) String telefone,
                           @RequestParam(required = false) String crm,
                           @RequestParam(required = false) String especialidade,
                           Model model,
                           RedirectAttributes redirect) {
        model.addAttribute("login", login);
        model.addAttribute("nome", nome);
        model.addAttribute("cpf", cpf);
        model.addAttribute("telefone", telefone);
        model.addAttribute("crm", crm);
        model.addAttribute("especialidade", especialidade);
        model.addAttribute("tipoCadastro", tipoCadastro);
        if (nome.isBlank() || cpf.isBlank()
                || ("PACIENTE".equals(tipoCadastro) && (telefone == null || telefone.isBlank()))
                || ("MEDICO".equals(tipoCadastro)
                && (crm == null || crm.isBlank() || especialidade == null || especialidade.isBlank()))
                || (!"PACIENTE".equals(tipoCadastro) && !"MEDICO".equals(tipoCadastro))) {
            model.addAttribute("erro", "Preencha os campos obrigatórios do tipo de cadastro selecionado.");
            return "login/cadastro";
        }

        try {
            if ("PACIENTE".equals(tipoCadastro)) {
                cadastroUsuarioService.cadastrarPaciente(login, senha, nome, cpf, telefone);
                redirect.addFlashAttribute("mensagem", "Cadastro concluído. Você já pode entrar.");
            } else {
                cadastroUsuarioService.solicitarCadastroMedico(login, senha, nome, cpf, crm, especialidade);
                redirect.addFlashAttribute("mensagem",
                        "Solicitação enviada. Seu acesso será liberado após aprovação da administração.");
            }
            return "redirect:/login";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("erro", e.getMessage());
            return "login/cadastro";
        }
    }

    @GetMapping("/medicos-pendentes")
    public String medicosPendentes(Model model) {
        model.addAttribute("solicitacoes",
                repository.findByStatusCadastroMedicoOrderByIdAsc(StatusCadastroMedico.PENDENTE));
        return "usuario/medicos-pendentes";
    }

    @PostMapping("/medicos-pendentes/{id}/aprovar")
    public String aprovarMedico(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            cadastroUsuarioService.aprovarMedico(id);
            redirect.addFlashAttribute("msg", "Cadastro do médico aprovado; o acesso está liberado.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/usuario/medicos-pendentes";
    }

    @PostMapping("/medicos-pendentes/{id}/rejeitar")
    public String rejeitarMedico(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            cadastroUsuarioService.rejeitarMedico(id);
            redirect.addFlashAttribute("msg", "Solicitação rejeitada. O acesso permanece bloqueado.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/usuario/medicos-pendentes";
    }
}
