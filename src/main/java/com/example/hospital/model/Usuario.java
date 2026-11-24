package com.example.hospital.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String login;

    @Column(nullable = false, length = 100)
    private String password; // hash BCrypt, nunca a senha em texto puro

    private boolean ativo = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_cadastro_medico", length = 20)
    private StatusCadastroMedico statusCadastroMedico;

    @ManyToMany
    @JoinTable(name = "usuario_perfil", // opcional: apenas explicita a tabela de associação
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "perfil_id"))
    private List<Perfil> perfis = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "pessoa_id")
    private PessoaFisica pessoa;

    public Pessoa getPessoa() {
        return pessoa;
    }

    public void setPessoa(PessoaFisica pessoa) {
        this.pessoa = pessoa;

    }

    public Usuario() {
    }

    public Usuario(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public void adicionarPerfil(Perfil perfil) {
        perfis.add(perfil);
        perfil.getUsuarios().add(this);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public StatusCadastroMedico getStatusCadastroMedico() { return statusCadastroMedico; }
    public void setStatusCadastroMedico(StatusCadastroMedico statusCadastroMedico) {
        this.statusCadastroMedico = statusCadastroMedico;
    }

    public List<Perfil> getPerfis() { return perfis; }
}