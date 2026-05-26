package com.SanosySalvos.Coincidencias.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import org.springframework.http.HttpHeaders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.SanosySalvos.Coincidencias.dto.NotificacionRequestDTO;

@Service
public class NotificacionClient {

    private final RestTemplate restTemplate;
    
    @Autowired
    public NotificacionClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${notificacion.service.url}")
    private String url;

    public void enviarNotificacion(String mensaje) {
    try {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Algunos servidores bloquean peticiones sin User-Agent
        headers.set("User-Agent", "SanosySalvos-Coincidencias-Service/1.0");
        
        // Si él te dio un token de seguridad, descomenta la siguiente línea:
        // headers.set("Authorization", "Bearer TU_TOKEN_AQUI");

        HttpEntity<String> entity = new HttpEntity<>(mensaje, headers);
        restTemplate.postForEntity(url, entity, String.class);
        
        System.out.println("✅ ¡Notificación enviada!");
    } catch (Exception e) {
        System.err.println("❌ Error 403: " + e.getMessage());
    }
}
}