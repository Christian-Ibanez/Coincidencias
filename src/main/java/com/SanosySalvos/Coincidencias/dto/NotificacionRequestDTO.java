package com.SanosySalvos.Coincidencias.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificacionRequestDTO {
    private String correoDueno;
    private Long idMascotaPerdida;
    private Long idMascotaEncontrada;
}