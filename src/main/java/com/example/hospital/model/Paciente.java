package com.example.hospital.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "paciente")
public class Paciente implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_paciente;
    @NotBlank(message = "Adicione um nome.")
    private String nome;
    @NotBlank(message = "Adicione um numero.")
    private String telefone;

    @OneToMany(mappedBy = "paciente")
    private List<Consulta>consulta;

    public Long getId() {
        return id_paciente;
    }

    public void setId(Long id_paciente) {
        this.id_paciente = id_paciente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public List<Consulta> getConsulta() {
        return consulta;
    }

    public void setConsultas(List<Consulta> consulta) {
        this.consulta = consulta;
    }
}
