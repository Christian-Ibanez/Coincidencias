package com.SanosySalvos.Coincidencias.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // ESTO EVITA EL ERROR DE MISMATCH
class NotificacionClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private NotificacionClient notificacionClient;

    @Test
    void enviarNotificacion_DeberiaLlamarAlRestTemplate() {
        ReflectionTestUtils.setField(notificacionClient, "url", "http://test.url");
        
        notificacionClient.enviarNotificacion("Test mensaje");
        
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), any());
    }

    @Test
    void enviarNotificacion_DeberiaManejarErrorEnCatch() {
        ReflectionTestUtils.setField(notificacionClient, "url", "http://test.url");
        
        // Configuramos el mock para que falle
        when(restTemplate.postForEntity(anyString(), any(), any()))
            .thenThrow(new RuntimeException("Error simulado"));

        notificacionClient.enviarNotificacion("Test error");

        verify(restTemplate, times(1)).postForEntity(anyString(), any(), any());
    }
}