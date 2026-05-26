package com.SanosySalvos.Coincidencias.service;

import com.SanosySalvos.Coincidencias.dto.NotificacionRequestDTO;
import com.SanosySalvos.Coincidencias.dto.ReporteCruzeDTO;
import com.SanosySalvos.Coincidencias.model.Coincidencias;
import com.SanosySalvos.Coincidencias.model.EstadoCoincidencia;
import com.SanosySalvos.Coincidencias.repository.CoincidenciasRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoincidenciasServiceTest {

    @Mock
    private CoincidenciasRepository coincidenciasRepository;

    @InjectMocks
    private CoincidenciasService coincidenciasService;

    @Mock
    private NotificacionClient notificacionClient;

    private Coincidencias coincidenciaPendiente;
    private ReporteCruzeDTO reporteMascotaPerdida;

    @BeforeEach
    void setUp() {
        // Preparamos datos para la H.U. -3 (Descartar)
        coincidenciaPendiente = new Coincidencias();
        coincidenciaPendiente.setId(1L);
        coincidenciaPendiente.setEstado(EstadoCoincidencia.PENDIENTE);

        // Preparamos la mascota que el usuario está buscando (Ej: Un Poodle Blanco)
        reporteMascotaPerdida = new ReporteCruzeDTO();
        reporteMascotaPerdida.setId(10L);
        reporteMascotaPerdida.setTipoReporte("PERDIDO");
        reporteMascotaPerdida.setRaza("Poodle");
        reporteMascotaPerdida.setColor("Blanco");
        reporteMascotaPerdida.setLatitud(-36.82);
        reporteMascotaPerdida.setLongitud(-73.04);
    }

    @Test
    void buscarCoincidencias_DeberiaEncontrarUnMatchYNotificar() {
        // 1. PREPARACIÓN (Arrange)
        // Creamos un perro "falso" que el microservicio de reportes nos "devolverá"
        ReporteCruzeDTO reporteFalso = new ReporteCruzeDTO();
        reporteFalso.setId(10L);
        reporteFalso.setRaza("Poodle");
        reporteFalso.setColor("Blanco");
        reporteFalso.setCorreoUsuario("dueño@test.com");

        // Le decimos a Mockito: "Cuando el servicio llame a OpenFeign, devuélvele esta lista falsa"
        when(reporteClient.obtenerReportesCercanos(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(reporteFalso));

        // 2. EJECUCIÓN (Act)
        // Ejecutamos tu método buscando un Poodle Blanco
        coincidenciasService.buscarCoincidencias(-36.0, -72.0, "Poodle", "Blanco");

        // 3. VERIFICACIÓN (Assert)
        // Verificamos que OpenFeign efectivamente fue llamado una vez
        verify(reporteClient, times(1)).obtenerReportesCercanos(-36.0, -72.0, 5000.0);
        
        // Aquí podrías verificar que se llamó al NotificacionClient si el match fue exitoso
        // verify(notificacionClient, times(1)).enviarNotificacion(any());
    }

    // --- TESTS PARA LA H.U. -4: PROCESAR NUEVAS COINCIDENCIAS ---

    @Test
    void procesarNuevasCoincidencias_GuardaMatch_SiEstaDentroDelRadio() {
        // Arrange
        ReporteCruzeDTO candidatoCerca = new ReporteCruzeDTO();
        candidatoCerca.setId(20L);
        candidatoCerca.setTipoReporte("ENCONTRADO");
        candidatoCerca.setLatitud(-36.82);
        candidatoCerca.setLongitud(-73.04);

        // Act
        coincidenciasService.procesarNuevasCoincidencias(reporteMascotaPerdida, List.of(candidatoCerca));

        // Assert
        verify(coincidenciasRepository, times(1)).save(any(Coincidencias.class));
        
        // 2. VERIFICACIÓN CRÍTICA: Aseguramos que la notificación se envió
        // Lo que debes poner (CORRECTO):
        verify(notificacionClient, times(1)).enviarNotificacion(any(NotificacionRequestDTO.class));
    }

    @Test
    void procesarNuevasCoincidencias_NoGuardaMatch_SiEstaFueraDelRadio() {
        // Arrange: Creamos un candidato con coordenadas muy lejanas (ej. lat/lon 0.0)
        ReporteCruzeDTO candidatoLejos = new ReporteCruzeDTO();
        candidatoLejos.setId(30L);
        candidatoLejos.setTipoReporte("ENCONTRADO");
        candidatoLejos.setLatitud(0.0);
        candidatoLejos.setLongitud(0.0);

       // Act
        coincidenciasService.procesarNuevasCoincidencias(reporteMascotaPerdida, List.of(candidatoLejos));

        // Assert
        verify(coincidenciasRepository, never()).save(any(Coincidencias.class));
        
        // 3. Verificamos que NO se notificó
        // Lo que debes poner (CORRECTO):
        verify(notificacionClient, never()).enviarNotificacion(any(NotificacionRequestDTO.class));
    }

    // --- TESTS PARA LA H.U. -3: DESCARTAR COINCIDENCIA ---

    @Test
    void descartarCoincidencia_Exito_CambiaEstadoADescartado() {
        when(coincidenciasRepository.findById(1L)).thenReturn(Optional.of(coincidenciaPendiente));
        when(coincidenciasRepository.save(any(Coincidencias.class))).thenReturn(coincidenciaPendiente);

        Coincidencias resultado = coincidenciasService.descartarCoincidencia(1L);

        assertNotNull(resultado);
        assertEquals(EstadoCoincidencia.DESCARTADO, resultado.getEstado());
        verify(coincidenciasRepository, times(1)).save(coincidenciaPendiente);
    }

    @Test
    void descartarCoincidencia_LanzaExcepcion_SiNoExiste() {
        when(coincidenciasRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            coincidenciasService.descartarCoincidencia(99L);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(coincidenciasRepository, never()).save(any(Coincidencias.class));
    }

    @Test
    void procesarNuevasCoincidencias_DeberiaLlamarNotificacionClient_CuandoHayMatch() {
        // Arrange
        ReporteCruzeDTO candidatoCerca = new ReporteCruzeDTO();
        candidatoCerca.setId(20L);
        candidatoCerca.setLatitud(-36.82);
        candidatoCerca.setLongitud(-73.04);
        
        // Act
        coincidenciasService.procesarNuevasCoincidencias(reporteMascotaPerdida, List.of(candidatoCerca));

        // Assert: Aquí verificamos que, además de guardar, se llamó al cliente de notificaciones
        verify(notificacionClient, times(1)).enviarNotificacion(any(NotificacionRequestDTO.class));
    }

    @Test
    void procesarNuevasCoincidencias_ManejaCorrectamenteReporteEncontrado() {
        // Arrange: Cambiamos a ENCONTRADO
        reporteMascotaPerdida.setTipoReporte("ENCONTRADO"); 
        ReporteCruzeDTO candidato = new ReporteCruzeDTO();
        candidato.setId(20L);
        candidato.setLatitud(-36.82);
        candidato.setLongitud(-73.04);

        // Act
        coincidenciasService.procesarNuevasCoincidencias(reporteMascotaPerdida, List.of(candidato));

        // Assert
        verify(coincidenciasRepository, times(1)).save(any(Coincidencias.class));
    }

    @Test
    void procesarNuevasCoincidencias_NoHaceNada_SiLaListaEsVacia() {
        // Act
        coincidenciasService.procesarNuevasCoincidencias(reporteMascotaPerdida, List.of());

        // Assert: Verifica que no se guardó nada
        verify(coincidenciasRepository, never()).save(any(Coincidencias.class));
    }
}