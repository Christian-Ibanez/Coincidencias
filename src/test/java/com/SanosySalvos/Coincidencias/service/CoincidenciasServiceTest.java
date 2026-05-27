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

    @Mock
    private NotificacionClient notificacionClient;

    @Mock
    private ReporteClient reportesClient; // ¡Agregamos el mock del nuevo cliente OpenFeign!

    @InjectMocks
    private CoincidenciasService coincidenciasService;

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

    // --- TESTS PARA LA H.U. -4: PROCESAR NUEVAS COINCIDENCIAS (Orquestación con Feign) ---

    @Test
    void procesarNuevasCoincidencias_GuardaMatchYNotifica_SiCoincideFisicamente() {
        // Arrange: Simulamos un candidato que ya está cerca (PostGIS) y coincide en raza y color
        ReporteCruzeDTO candidatoCerca = new ReporteCruzeDTO();
        candidatoCerca.setId(20L);
        candidatoCerca.setTipoReporte("ENCONTRADO");
        candidatoCerca.setRaza("Poodle");
        candidatoCerca.setColor("Blanco");
        candidatoCerca.setLatitud(-36.82);
        candidatoCerca.setLongitud(-73.04);

        // Act
        coincidenciasService.procesarNuevasCoincidencias(reporteMascotaPerdida, List.of(candidatoCerca));

        // Assert
        verify(coincidenciasRepository, times(1)).save(any(Coincidencias.class));
        verify(notificacionClient, times(1)).enviarNotificacion(any(NotificacionRequestDTO.class));
    }

    @Test
    void procesarNuevasCoincidencias_NoGuardaMatch_SiNoCoincideFisicamente() {
        // Arrange: Simulamos un candidato cerca, pero que es FÍSICAMENTE diferente
        ReporteCruzeDTO candidatoDiferente = new ReporteCruzeDTO();
        candidatoDiferente.setId(30L);
        candidatoDiferente.setTipoReporte("ENCONTRADO");
        candidatoDiferente.setRaza("Pastor Alemán"); // Raza distinta
        candidatoDiferente.setColor("Negro");
        candidatoDiferente.setLatitud(-36.82);
        candidatoDiferente.setLongitud(-73.04);

        // Act
        coincidenciasService.procesarNuevasCoincidencias(reporteMascotaPerdida, List.of(candidatoDiferente));

        // Assert: No debería guardar ni notificar porque no hacen match
        verify(coincidenciasRepository, never()).save(any(Coincidencias.class));
        verify(notificacionClient, never()).enviarNotificacion(any(NotificacionRequestDTO.class));
    }

    @Test
    void procesarNuevasCoincidencias_NoHaceNada_SiLaListaEsVacia() {
        // Act
        coincidenciasService.procesarNuevasCoincidencias(reporteMascotaPerdida, List.of());

        // Assert: Verifica que no se guardó nada
        verify(coincidenciasRepository, never()).save(any(Coincidencias.class));
    }

    @Test
    void procesarNuevasCoincidencias_ManejaCorrectamenteReporteEncontrado() {
        // Arrange: Cambiamos a ENCONTRADO
        reporteMascotaPerdida.setTipoReporte("ENCONTRADO"); 
        ReporteCruzeDTO candidato = new ReporteCruzeDTO();
        candidato.setId(20L);
        candidato.setTipoReporte("PERDIDO"); // Si uno es encontrado, el otro debe ser perdido
        candidato.setRaza("Poodle");
        candidato.setColor("Blanco");
        candidato.setLatitud(-36.82);
        candidato.setLongitud(-73.04);

        // Act
        coincidenciasService.procesarNuevasCoincidencias(reporteMascotaPerdida, List.of(candidato));

        // Assert
        verify(coincidenciasRepository, times(1)).save(any(Coincidencias.class));
    }

    // --- TESTS PARA LA H.U. -3: DESCARTAR COINCIDENCIA (Intactos) ---

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
    void procesarNuevasCoincidencias_NoHaceNada_SiLaListaEsNull() {
        // Obligamos a evaluar la rama "reportesCandidatos == null"
        coincidenciasService.procesarNuevasCoincidencias(reporteMascotaPerdida, null);

        verify(coincidenciasRepository, never()).save(any(Coincidencias.class));
    }

    @Test
    void procesarNuevasCoincidencias_NoGuardaMatch_SiRazaOColorSonNull() {
        // Obligamos a evaluar la rama donde el atributo es nulo
        reporteMascotaPerdida.setRaza(null); 
        
        ReporteCruzeDTO candidato = new ReporteCruzeDTO();
        candidato.setId(20L);
        candidato.setLatitud(-36.82);
        candidato.setLongitud(-73.04);
        
        coincidenciasService.procesarNuevasCoincidencias(reporteMascotaPerdida, List.of(candidato));
        
        verify(coincidenciasRepository, never()).save(any(Coincidencias.class));
    }

    @Test
    void procesarNuevasCoincidencias_NoGuardaMatch_SiEstaFueraDelRadio() {
        // Obligamos a evaluar la rama donde la distancia es mayor a 5km
        ReporteCruzeDTO candidatoLejos = new ReporteCruzeDTO();
        candidatoLejos.setId(20L);
        candidatoLejos.setTipoReporte("ENCONTRADO");
        candidatoLejos.setRaza("Poodle");
        candidatoLejos.setColor("Blanco");
        
        // Ponemos coordenadas en el hemisferio norte (muy lejos de Chile)
        candidatoLejos.setLatitud(40.71);
        candidatoLejos.setLongitud(-74.00);

        coincidenciasService.procesarNuevasCoincidencias(reporteMascotaPerdida, List.of(candidatoLejos));

        verify(coincidenciasRepository, never()).save(any(Coincidencias.class));
    }

    // --- TESTS PARA ALCANZAR EL 100% DE COBERTURA ---

    @Test
    void procesarNuevasCoincidencias_AjustaSimilitudACero_SiDistanciaEsMuyGrande() {
        // 1. Inyectamos un radio enorme (20km) para permitir que el perro entre al if,
        // pero que esté lo suficientemente lejos como para que la similitud dé negativa.
        org.springframework.test.util.ReflectionTestUtils.setField(coincidenciasService, "radioBusquedaKm", 20.0);

        ReporteCruzeDTO candidatoLejos = new ReporteCruzeDTO();
        candidatoLejos.setId(99L);
        candidatoLejos.setTipoReporte("ENCONTRADO");
        candidatoLejos.setRaza("Poodle");
        candidatoLejos.setColor("Blanco");
        
        // Coordenadas a ~14 kilómetros de distancia del punto original (-36.82)
        candidatoLejos.setLatitud(-36.95);
        candidatoLejos.setLongitud(-73.04);

        // 2. Ejecutamos
        coincidenciasService.procesarNuevasCoincidencias(reporteMascotaPerdida, List.of(candidatoLejos));

        // 3. Verificamos que se guardó. Al entrar, la línea 'porcentaje = 0.0' será ejecutada.
        verify(coincidenciasRepository, times(1)).save(any(Coincidencias.class));
        
        // Limpiamos el valor inyectado para no afectar otros tests
        org.springframework.test.util.ReflectionTestUtils.setField(coincidenciasService, "radioBusquedaKm", 0.0);
    }

    @Test
    void procesarNuevasCoincidencias_NoGuardaMatch_SiColorEsNull() {
        // Obligamos a evaluar específicamente la rama donde el COLOR es nulo
        reporteMascotaPerdida.setColor(null); 
        
        ReporteCruzeDTO candidato = new ReporteCruzeDTO();
        candidato.setId(20L);
        candidato.setTipoReporte("ENCONTRADO");
        candidato.setRaza("Poodle");
        candidato.setColor("Blanco");
        candidato.setLatitud(-36.82);
        candidato.setLongitud(-73.04);
        
        // Ejecutamos
        coincidenciasService.procesarNuevasCoincidencias(reporteMascotaPerdida, List.of(candidato));
        
        // Verificamos que no pase la validación física
        verify(coincidenciasRepository, never()).save(any(Coincidencias.class));
    }
}