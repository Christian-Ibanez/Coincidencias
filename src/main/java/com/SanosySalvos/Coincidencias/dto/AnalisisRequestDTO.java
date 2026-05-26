package com.SanosySalvos.Coincidencias.dto;

import lombok.Data;
import java.util.List;

@Data
public class AnalisisRequestDTO {
    private ReporteCruzeDTO reporteNuevo;
    private List<ReporteCruzeDTO> candidatos;
}