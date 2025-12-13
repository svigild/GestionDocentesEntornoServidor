package com.sergiovd.gestiondocentes.repository;

import com.sergiovd.gestiondocentes.model.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, Long> {

    // Obtener los docentes de un departamento por el nombre del departamento
    List<Docente> findByDepartamentoNombre(String nombreDepartamento);

    // Obtener el docente dado su email
    Optional<Docente> findDocenteByEmail(String email);

    List<Docente> findAllByOrderByApellidosAsc();

    long countByDepartamentoCodigo(String codigoDepartamento);

    @Query("SELECT a.docente " +
            "FROM AsuntoPropio a " +
            "WHERE a.aprobado = true AND a.diaSolicitado < CURRENT_DATE " +
            "GROUP BY a.docente " +
            "ORDER BY COUNT(a) DESC")
    List<Docente> findDocenteConMasDiasDisfrutados(Pageable pageable);

    // Número de profesores por departamento
    @Query("SELECT COUNT(d) FROM Docente d WHERE d.departamento.codigo = :codDept")
    long contarDocentesPorDepartamento(@Param("codDept") String codDept);
}