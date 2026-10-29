package com.example.hospital.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "paciente")
@PrimaryKeyJoinColumn(name = "id_pessoa")
public class Paciente extends PessoaFisica implements Serializable {

    @NotBlank(message = "Adicione um numero.")
    private String telefone;

    @OneToMany(mappedBy = "paciente")
    private List<Consulta>consulta;

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
