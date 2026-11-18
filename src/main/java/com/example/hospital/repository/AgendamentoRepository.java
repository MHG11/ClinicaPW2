package com.example.hospital.repository;

import com.example.hospital.model.Agendamento;
import com.example.hospital.model.StatusAgendamento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    List<Agendamento> findAllByOrderByCriadoEmDesc();

    List<Agendamento> findByStatusOrderByCriadoEmDesc(StatusAgendamento status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Agendamento a where a.id = :id")
    Optional<Agendamento> findByIdForUpdate(@Param("id") Long id);
}
