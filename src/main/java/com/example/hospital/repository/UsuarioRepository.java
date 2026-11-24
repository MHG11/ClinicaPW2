package com.example.hospital.repository;

import com.example.hospital.model.Usuario;
import com.example.hospital.model.StatusCadastroMedico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);

    boolean existsByPessoaId(Long pessoaId);

    boolean existsByLogin(String login);

    List<Usuario> findByStatusCadastroMedicoOrderByIdAsc(StatusCadastroMedico statusCadastroMedico);
}
