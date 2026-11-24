package com.example.hospital.services;

import com.example.hospital.model.*;
import com.example.hospital.repository.PerfilRepository;
import com.example.hospital.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CadastroUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    public CadastroUsuarioService(UsuarioRepository usuarioRepository,
                                  PerfilRepository perfilRepository,
                                  PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void cadastrarPaciente(String login, String senha, String nome, String cpf, String telefone) {
        Usuario usuario = criarUsuario(login, senha, "PACIENTE");
        Paciente paciente = new Paciente();
        paciente.setNome(nome.trim());
        paciente.setCpf(cpf.trim());
        paciente.setTelefone(telefone.trim());
        usuario.setPessoa(paciente);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void solicitarCadastroMedico(String login,
                                        String senha,
                                        String nome,
                                        String cpf,
                                        String crm,
                                        String especialidade) {
        Usuario usuario = criarUsuario(login, senha, "MEDICO");
        Medico medico = new Medico();
        medico.setNome(nome.trim());
        medico.setCpf(cpf.trim());
        medico.setCrm(crm.trim());
        medico.setEspecialidade(especialidade.trim());
        usuario.setPessoa(medico);
        usuario.setAtivo(false);
        usuario.setStatusCadastroMedico(StatusCadastroMedico.PENDENTE);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void aprovarMedico(Long usuarioId) {
        Usuario usuario = buscarMedicoPendente(usuarioId);
        Perfil perfilMedico = perfilRepository.findByNome("MEDICO")
                .orElseThrow(() -> new IllegalStateException("O perfil MEDICO não está configurado."));
        usuario.getPerfis().clear();
        usuario.adicionarPerfil(perfilMedico);
        usuario.setStatusCadastroMedico(StatusCadastroMedico.APROVADO);
        usuario.setAtivo(true);
    }

    @Transactional
    public void rejeitarMedico(Long usuarioId) {
        Usuario usuario = buscarMedicoPendente(usuarioId);
        usuario.setStatusCadastroMedico(StatusCadastroMedico.REJEITADO);
        usuario.setAtivo(false);
    }

    private Usuario criarUsuario(String login, String senha, String nomePerfil) {
        if (login == null || login.isBlank() || login.length() > 50) {
            throw new IllegalArgumentException("Informe um usuário com até 50 caracteres.");
        }
        if (senha == null || senha.length() < 6 || senha.length() > 72) {
            throw new IllegalArgumentException("A senha deve ter entre 6 e 72 caracteres.");
        }
        if (usuarioRepository.existsByLogin(login.trim())) {
            throw new IllegalArgumentException("Este nome de usuário já está em uso.");
        }

        Perfil perfil = perfilRepository.findByNome(nomePerfil)
                .orElseThrow(() -> new IllegalStateException("O perfil " + nomePerfil + " não está configurado."));
        Usuario usuario = new Usuario();
        usuario.setLogin(login.trim());
        usuario.setPassword(passwordEncoder.encode(senha));
        usuario.setAtivo(true);
        usuario.adicionarPerfil(perfil);
        return usuario;
    }

    private Usuario buscarMedicoPendente(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação de médico não encontrada."));
        if (usuario.getStatusCadastroMedico() != StatusCadastroMedico.PENDENTE
                || !(usuario.getPessoa() instanceof Medico)) {
            throw new IllegalStateException("Esta solicitação não está mais pendente.");
        }
        return usuario;
    }
}
