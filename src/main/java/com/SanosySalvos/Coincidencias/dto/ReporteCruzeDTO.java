package com.SanosySalvos.Coincidencias.dto;

import lombok.Data;

@Data
public class ReporteCruzeDTO {
    // Los datos que ya tenías:
    private Long id;
    private String tipoReporte;
    private Double latitud;
    private Double longitud;
    private String correoUsuario;

    //campos para hacer comparación física
    private String raza;
    private String color;
    private String tamano;
}