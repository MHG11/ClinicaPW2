package com.example.hospital.repository;

import com.example.hospital.model.Agenda;
import com.example.hospital.model.StatusAgenda;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AgendaRepository extends JpaRepository<Agenda, Long> {

    boolean existsByMedicoIdAndDataHora(Long medicoId, LocalDateTime dataHora);

    List<Agenda> findByStatusOrderByDataHoraAsc(StatusAgenda status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Agenda a where a.id = :id")
    Optional<Agenda> findByIdForUpdate(@Param("id") Long id);
}
