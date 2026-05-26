package com.SanosySalvos.Coincidencias.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // Timeout de conexión: 5 segundos máximo para encontrar el servidor
        factory.setConnectTimeout(5000); 
        
        // Timeout de lectura: 10 segundos máximo esperando la respuesta
        factory.setReadTimeout(10000); 
        
        return new RestTemplate(factory);
    }
}