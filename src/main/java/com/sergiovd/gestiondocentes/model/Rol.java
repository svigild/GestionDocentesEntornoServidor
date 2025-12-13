package com.sergiovd.gestiondocentes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Entity
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    private Integer orden;

    // Relación 1:N con Docente
    @OneToMany(mappedBy = "rol")
    @JsonIgnore
    private List<Docente> docentes;
}