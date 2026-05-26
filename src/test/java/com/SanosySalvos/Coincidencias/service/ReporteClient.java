package com.SanosySalvos.Coincidencias.service;

import com.SanosySalvos.Coincidencias.dto.ReporteCruzeDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;


@FeignClient(name = "reportes-service", url = "${reportes.service.url}")
public interface ReporteClient {

    @GetMapping("/api/reportes/cercanos")
    @CircuitBreaker(name = "reportesService", fallbackMethod = "fallbackReportes")
    List<ReporteCruzeDTO> obtenerReportesCercanos(
            @RequestParam("lat") Double latitud, 
            @RequestParam("lon") Double longitud, 
            @RequestParam("radio") Double radio
    );

    default List<ReporteCruzeDTO> fallbackReportes(Double latitud, Double longitud, Double radio, Throwable t) {
        System.err.println("⚠️ El servicio de reportes no está respondiendo. No se pudo buscar por geolocalización. Causa: " + t.getMessage());
        // Retornamos una lista vacía para que tu lógica de coincidencias no lance un NullPointerException
        return Collections.emptyList(); 
    }
}