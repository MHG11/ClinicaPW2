package com.example.hospital.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.validation.constraints.NotBlank;

@Entity
@PrimaryKeyJoinColumn(name = "id_pessoa")
public class PessoaFisica extends Pessoa{

    @NotBlank(message = "Adicione um CPF.")
    private String cpf;

    public String getCpf() {
        return cpf;
    }
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

}
