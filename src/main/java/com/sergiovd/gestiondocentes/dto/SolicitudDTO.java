package com.sergiovd.gestiondocentes.dto;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Data;
import java.time.LocalDate;

@Data
public class SolicitudDTO {

    private Long idDocente;

    private String nombreDocente;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate diaSolicitado;

    private String descripcion;
}