package com.SanosySalvos.Coincidencias.repository;

import com.SanosySalvos.Coincidencias.model.Coincidencias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoincidenciasRepository extends JpaRepository<Coincidencias, Long> {
    // Para cuando el usuario quiera ver sus posibles coincidencias
    List<Coincidencias> findByReportePerdidoId(Long reportePerdidoId);
}