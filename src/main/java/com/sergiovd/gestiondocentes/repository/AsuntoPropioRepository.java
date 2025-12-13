package com.sergiovd.gestiondocentes.repository;

import com.sergiovd.gestiondocentes.model.AsuntoPropio;
import com.sergiovd.gestiondocentes.model.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AsuntoPropioRepository extends JpaRepository<AsuntoPropio, Long> {

    // Obtener los días de asuntos propios pendientes de disfrutar (validados y fecha >= hoy)
    @Query("SELECT a FROM AsuntoPropio a WHERE a.docente.id = :idDocente AND a.aprobado = true AND a.diaSolicitado >= CURRENT_DATE")
    List<AsuntoPropio> findPendientesDeDisfrutar(Long idDocente);

    // Docente que más días ha disfrutado
    @Query("SELECT a.docente FROM AsuntoPropio a WHERE a.aprobado = true GROUP BY a.docente ORDER BY COUNT(a) DESC LIMIT 1")
    Docente encontrarDocenteConMasDias();
    List<AsuntoPropio> findByDocenteIdOrderByDiaSolicitadoDesc(Long idDocente);

    // Días pendientes (futuros y aprobados)
    @Query("SELECT a FROM AsuntoPropio a WHERE a.docente.id = :idDocente AND a.aprobado = true AND a.diaSolicitado >= CURRENT_DATE")
    List<AsuntoPropio> encontrarPendientesDeDisfrutar(@Param("idDocente") Long idDocente);
}