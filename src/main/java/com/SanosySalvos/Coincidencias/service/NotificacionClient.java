package com.SanosySalvos.Coincidencias.service;

import com.SanosySalvos.Coincidencias.dto.NotificacionRequestDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notificacion-service", url = "${notificacion.service.url}")
public interface NotificacionClient {

    @PostMapping
    @CircuitBreaker(name = "notificacionService", fallbackMethod = "fallbackNotificacion")
    void enviarNotificacion(@RequestBody NotificacionRequestDTO requestDTO);

    default void fallbackNotificacion(NotificacionRequestDTO requestDTO, Throwable t) {
        System.err.println("⚠️ El servicio de notificaciones no está disponible. Fallback ejecutado. Causa: " + t.getMessage());
    }
}