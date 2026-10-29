package com.example.hospital.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "medico")
@PrimaryKeyJoinColumn(name = "id_pessoa")
public class Medico extends PessoaFisica implements Serializable {

    @NotBlank(message = "Adicione um CRM.")
    private String crm;

    @OneToMany(mappedBy = "medico")
    private List<Consulta> consultas;

    public String getCrm() {
        return crm;
    }

    public void setCrm(String crm) {
        this.crm = crm;
    }

    public List<Consulta> getConsultas() {
        return consultas;
    }

    public void setConsultas(List<Consulta> consultas) {
        this.consultas = consultas;
    }
}
