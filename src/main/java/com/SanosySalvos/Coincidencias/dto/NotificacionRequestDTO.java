package com.SanosySalvos.Coincidencias.dto;

import lombok.Data;

@Data
public class NotificacionRequestDTO {
    private Long reportePerdidoId;
    private Long reporteEncontradoId;
    private Double porcentajeSimilitud;
    private String mensaje; 
}