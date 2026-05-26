package com.SanosySalvos.Coincidencias.service;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoincidenciasServiceTest {

    @Mock
    private CoincidenciasRepository coincidenciasRepository;

    @InjectMocks
    private CoincidenciasService coincidenciasService;

    private Coincidencias coincidenciaPendiente;
    private ReporteCruzeDTO reporteNuevo;

    @BeforeEach
    void setUp() {
        // TRUCO VITAL: Le inyectamos el valor de 5.0 a la variable @Value manualmente para el test
        ReflectionTestUtils.setField(coincidenciasService, "radioBusquedaKm", 5.0);

        // Preparamos datos para la H.U. -3 (Descartar)
        coincidenciaPendiente = new Coincidencias();
        coincidenciaPendiente.setId(1L);
        coincidenciaPendiente.setEstado(EstadoCoincidencia.PENDIENTE);

        // Preparamos el reporte nuevo para la H.U. -4 (Crear cruce)
        reporteNuevo = new ReporteCruzeDTO();
        reporteNuevo.setId(10L);
        reporteNuevo.setTipoReporte("PERDIDO");
        reporteNuevo.setLatitud(-36.82);
        reporteNuevo.setLongitud(-73.04);
    }

    // --- TESTS PARA LA H.U. -4: PROCESAR NUEVAS COINCIDENCIAS ---

    @Test
    void procesarNuevasCoincidencias_GuardaMatch_SiEstaDentroDelRadio() {
        // Arrange: Creamos un candidato con coordenadas idénticas (Distancia = 0 km)
        ReporteCruzeDTO candidatoCerca = new ReporteCruzeDTO();
        candidatoCerca.setId(20L);
        candidatoCerca.setTipoReporte("ENCONTRADO");
        candidatoCerca.setLatitud(-36.82);
        candidatoCerca.setLongitud(-73.04);

        // Act
        coincidenciasService.procesarNuevasCoincidencias(reporteNuevo, List.of(candidatoCerca));

        // Assert: Verificamos que se guardó 1 vez en la base de datos
        verify(coincidenciasRepository, times(1)).save(any(Coincidencias.class));
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
        coincidenciasService.procesarNuevasCoincidencias(reporteNuevo, List.of(candidatoLejos));

        // Assert: Verificamos que NUNCA intentó guardar en la base de datos
        verify(coincidenciasRepository, never()).save(any(Coincidencias.class));
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
}