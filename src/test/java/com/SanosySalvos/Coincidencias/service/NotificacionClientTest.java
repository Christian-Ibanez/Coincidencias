package com.SanosySalvos.Coincidencias.service;

import com.SanosySalvos.Coincidencias.dto.NotificacionRequestDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificacionClientTest {

    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalErr = System.err;

    // Creamos una implementación anónima "dummy" de la interfaz solo para acceder al método default
    private final NotificacionClient notificacionClient = new NotificacionClient() {
        @Override
        public void enviarNotificacion(NotificacionRequestDTO requestDTO) {
            // Este método lo implementa Feign en la vida real, no lo probamos aquí
        }
    };

    @BeforeEach
    public void setUpStreams() {
        // Redirigimos la salida de error para poder leer qué imprime el System.err.println
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams() {
        // Restauramos la consola a la normalidad
        System.setErr(originalErr);
    }

    @Test
    void fallbackNotificacion_DeberiaManejarElErrorYRegistrarEnConsola() {
        // 1. Preparamos los datos falsos
        NotificacionRequestDTO mockRequest = new NotificacionRequestDTO();
        mockRequest.setCorreoDueno("test@test.com");
        Throwable mockExcepcion = new RuntimeException("Timeout simulado por el test");

        // 2. Ejecutamos tu método fallback directamente
        notificacionClient.fallbackNotificacion(mockRequest, mockExcepcion);

        // 3. Verificamos que tu mensaje personalizado se imprimió correctamente
        String salidaConsola = errContent.toString();
        assertTrue(salidaConsola.contains("Fallback ejecutado"));
        assertTrue(salidaConsola.contains("Timeout simulado por el test"));
    }
}