package com.sergiovd.gestiondocentes.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Data
@Entity
public class Falta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha; // El día específico que faltó
    private String anotacion; // "Se ha hecho", "Alumnos no vinieron", etc.
    private String material;  // Material subido para la guardia

    // Relación N:1 con Horario
    @ManyToOne
    @JoinColumn(name = "horario_id")
    private Horario horario;
}