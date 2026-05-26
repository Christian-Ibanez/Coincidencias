package com.SanosySalvos.Coincidencias.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "db_coincidencias")
@Data
@Getter
@Setter
public class Coincidencias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El ID del reporte original (la mascota perdida)
    @Column(name = "reporte_perdido_id", nullable = false)
    private Long reportePerdidoId;

    // El ID del reporte candidato (la mascota encontrada)
    @Column(name = "reporte_encontrado_id", nullable = false)
    private Long reporteEncontradoId;

    // Un valor del 0 al 100 para saber qué tan probable es que sean la misma mascota
    @Column(name = "porcentaje_similitud", nullable = false)
    private Double porcentajeSimilitud;

    // Para saber si el usuario ya vio o descartó este posible match
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