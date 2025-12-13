package com.sergiovd.gestiondocentes.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
public class AsuntoPropio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate diaSolicitado;
    private String descripcion;
    private LocalDateTime fechaTramitacion;
    private Boolean aprobado; // Si es null está pendiente, true aprobado y false rechazado

    // Relación con el docente que solicita el día de permiso
    @ManyToOne
    @JoinColumn(name = "docente_id")
    private Docente docente;

    // Tuve que añadir esta relación para persistir quién es el compañero asignado para cubrir la guardia.
    // Antes de añadir esto, calculaba el sustituto pero se perdía al refrescar la página.
    // Ahora queda guardado en la base de datos vinculado a la solicitud.
    @ManyToOne
    @JoinColumn(name = "sustituto_id")
    private Docente sustituto;
}