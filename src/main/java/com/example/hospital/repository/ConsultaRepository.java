package com.example.hospital.repository;

import com.example.hospital.model.Consulta;
import com.example.hospital.model.Paciente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByPacienteId(Long id);
    List<Consulta> findByMedicoId(Long id);

    boolean existsByAgendamentoId(Long agendamentoId);
}
