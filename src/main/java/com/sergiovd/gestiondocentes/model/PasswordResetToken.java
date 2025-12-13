package com.sergiovd.gestiondocentes.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne(targetEntity = Docente.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "docente_id")
    private Docente docente;

    private LocalDateTime fechaExpiracion;

    public PasswordResetToken(String token, Docente docente) {
        this.token = token;
        this.docente = docente;
        this.fechaExpiracion = LocalDateTime.now().plusMinutes(60); // 1 hora de validez
    }
}