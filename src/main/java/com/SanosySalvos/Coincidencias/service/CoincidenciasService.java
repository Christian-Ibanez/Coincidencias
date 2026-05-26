package com.SanosySalvos.Coincidencias.service;

import com.SanosySalvos.Coincidencias.dto.NotificacionRequestDTO;
import com.SanosySalvos.Coincidencias.dto.ReporteCruzeDTO;
import com.SanosySalvos.Coincidencias.model.Coincidencias;
import com.SanosySalvos.Coincidencias.model.EstadoCoincidencia;
import com.SanosySalvos.Coincidencias.repository.CoincidenciasRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoincidenciasService {

    @Autowired
    private CoincidenciasRepository coincidenciasRepository;

    @Autowired
    private NotificacionClient notificacionClient;

    @Value("${coincidencias.radio-busqueda-km:5.0}")
    private double radioBusquedaKm;

    public void procesarNuevasCoincidencias(ReporteCruzeDTO reporteNuevo, List<ReporteCruzeDTO> reportesCandidatos) {
        for (ReporteCruzeDTO candidato : reportesCandidatos) {
            
            double distanciaKm = calcularDistanciaHaversine(
                    reporteNuevo.getLatitud(), reporteNuevo.getLongitud(),
                    candidato.getLatitud(), candidato.getLongitud()
            );

            if (distanciaKm <= radioBusquedaKm) {
                double porcentaje = 100.0 - (distanciaKm * 10); 

                Coincidencias coincidencia = new Coincidencias();
                coincidencia.setReportePerdidoId(
                        reporteNuevo.getTipoReporte().equals("PERDIDO") ? reporteNuevo.getId() : candidato.getId()
                );
                coincidencia.setReporteEncontradoId(
                        reporteNuevo.getTipoReporte().equals("ENCONTRADO") ? reporteNuevo.getId() : candidato.getId()
                );
                coincidencia.setPorcentajeSimilitud(Math.round(porcentaje * 100.0) / 100.0);
                coincidencia.setEstado(EstadoCoincidencia.PENDIENTE);

                coincidenciasRepository.save(coincidencia);

                NotificacionRequestDTO requestDTO = new NotificacionRequestDTO();
                
                requestDTO.setIdMascotaPerdida(coincidencia.getReportePerdidoId());
                requestDTO.setIdMascotaEncontrada(coincidencia.getReporteEncontradoId());
                
                requestDTO.setCorreoDueno(reporteNuevo.getCorreoUsuario()); 
                
                notificacionClient.enviarNotificacion(requestDTO);

                System.out.println("🔥 ¡NUEVO MATCH ENCONTRADO! Similitud: " + coincidencia.getPorcentajeSimilitud() + "% a " + Math.round(distanciaKm) + " km.");
            }
            
        }
    }

    public Coincidencias descartarCoincidencia(Long id) {

        Coincidencias coincidencia = coincidenciasRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "La coincidencia no existe."));

        coincidencia.setEstado(EstadoCoincidencia.DESCARTADO);
        return coincidenciasRepository.save(coincidencia);
    }

    private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int RADIO_TIERRA_KM = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RADIO_TIERRA_KM * c;
    }
}