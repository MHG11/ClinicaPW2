package com.example.hospital.repository;

import com.example.hospital.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    boolean existsByAgendamentoId(Long agendamentoId);
}
