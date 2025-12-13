package com.sergiovd.gestiondocentes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
public class Docente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellidos;
    private String email;
    private String siglas;

    // 1=Carrera, 2=Prácticas, 3=Interino
    private Integer tipoFuncionario;

    private LocalDate fechaAntiguedad;

    private String password;

    private Double notaOposicion;

    // Para indicar si se ha cambiado la contraseña inicial o no, lo cual es obligatorio
    private Boolean passwordChanged = false;

    @ManyToOne
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;

    @ManyToOne
    @JoinColumn(name = "rol_id")
    private Rol rol;

    @OneToMany(mappedBy = "docente")
    @JsonIgnore
    private List<AsuntoPropio> asuntosPropios;

    @OneToMany(mappedBy = "docente")
    @JsonIgnore
    private List<Horario> horarios;

    private Integer guardiasRealizadas = 0;
}
