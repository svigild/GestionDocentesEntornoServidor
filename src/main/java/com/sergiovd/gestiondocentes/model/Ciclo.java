package com.sergiovd.gestiondocentes.model;



import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Ciclo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String familia;
    private String codigo;
    private String turno;

    // Relación 1:N con Asignatura
    @OneToMany(mappedBy = "ciclo")
    @JsonIgnore
    private List<Asignatura> asignaturas;
}