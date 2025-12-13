package com.sergiovd.gestiondocentes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Entity
public class Horario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer dia;  // 1=Lunes, 5=Viernes...
    private Integer hora; // 1, 2, 3...
    private String aula;

    // Relación N:1 con Docente
    @ManyToOne
    @JoinColumn(name = "docente_id")
    private Docente docente;

    // Relación N:1 con Asignatura
    @ManyToOne
    @JoinColumn(name = "asignatura_id")
    private Asignatura asignatura;

    // Relación 1:N con Falta/Guardia
    @OneToMany(mappedBy = "horario")
    @JsonIgnore
    private List<Falta> faltas;
}