package com.sergiovd.gestiondocentes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Data
@Entity
public class Departamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String codigo;
    private String telefono;

    // Relación 1:N con Docente
    @OneToMany(mappedBy = "departamento")
    @JsonIgnore
    private List<Docente> docentes;
}