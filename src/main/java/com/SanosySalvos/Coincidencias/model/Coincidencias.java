package com.SanosySalvos.Coincidencias.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "coincidencias")
@Data
@Getter
@Setter
public class Coincidencias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reporte_perdido_id", nullable = false)
    private Long reportePerdidoId;

    @Column(name = "reporte_encontrado_id", nullable = false)
    private Long reporteEncontradoId;

    @Column(name = "porcentaje_similitud", nullable = false)
    private Double porcentajeSimilitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCoincidencia estado;

    @Column(name = "fecha_calculo", updatable = false)
    private LocalDateTime fechaCalculo;

    @PrePersist
    protected void onCreate() {
        this.fechaCalculo = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoCoincidencia.PENDIENTE;
        }
    }
}