package com.sergiovd.gestiondocentes.repository;

import com.sergiovd.gestiondocentes.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    // Busca por "Profesor", "Dirección"
    Optional<Rol> findByNombre(String nombre);
}