package com.sergiovd.gestiondocentes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Entity
public class Asignatura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String siglas;
    private Integer curso;

    // Relación N:1 con Ciclo
    @ManyToOne
    @JoinColumn(name = "ciclo_id")
    private Ciclo ciclo;

    // Relación 1:N con Horario
    @OneToMany(mappedBy = "asignatura")
    @JsonIgnore
    private List<Horario> horarios;
}