package com.SanosySalvos.Coincidencias;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients
public class FeignConfig {
    // Esta clase aísla a Feign para que no rompa tus pruebas de controladores
}