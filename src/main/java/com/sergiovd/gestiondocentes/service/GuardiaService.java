package com.sergiovd.gestiondocentes.service;

import com.sergiovd.gestiondocentes.model.Docente;
import com.sergiovd.gestiondocentes.repository.DocenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GuardiaService {

    @Autowired
    private DocenteRepository docenteRepo;

    // Método principal para asignar una guardia. Implementa la lógica de negocio requerida:
    // priorizar compañeros del mismo departamento y equilibrar la carga de trabajo.
    public Docente asignarGuardia(Long idDocenteAusente) {
        Docente ausente = docenteRepo.findById(idDocenteAusente).orElse(null);
        if (ausente == null) return null;

        List<Docente> todos = docenteRepo.findAll();
        // Me aseguro de quitar al docente ausente de la lista de candidatos para evitar asignarse a sí mismo
        todos.removeIf(d -> d.getId().equals(idDocenteAusente));

        // CRITERIO 1: Prioridad pedagógica (Mismo Departamento)
        // Busco primero entre los compañeros de departamento porque conocen la materia
        List<Docente> mismoDepto = todos.stream()
                .filter(d -> d.getDepartamento().equals(ausente.getDepartamento()))
                // Ordeno por carga de trabajo (quien menos guardias tenga, va primero)
                .sorted(Comparator.comparingInt(this::getGuardiasRealizadasSafe))
                .collect(Collectors.toList());

        // Si encuentro a alguien del departamento, devuelvo al mejor candidato directamente
        if (!mismoDepto.isEmpty()) return mismoDepto.get(0);

        // CRITERIO 2: Solidaridad de centro (Resto del Claustro)
        // Si no hay nadie del departamento disponible, busco en todo el colegio,
        // ordenando estrictamente por quien ha hecho menos guardias hasta la fecha.
        todos.sort(Comparator.comparingInt(this::getGuardiasRealizadasSafe));

        return todos.isEmpty() ? null : todos.get(0);
    }

    // Helper para evitar NullPointerExceptions si el contador de guardias no se inicializó a 0
    private int getGuardiasRealizadasSafe(Docente d) {
        return d.getGuardiasRealizadas() == null ? 0 : d.getGuardiasRealizadas();
    }

    /**
     * Mantuve este método separado por si necesito una implementación más específica
     * para el algoritmo automático que solo mire el departamento, según requisitos específicos del PDF.
     */
    public Docente buscarSustitutoAutomatico(Long idDocenteAusente) {
        Docente ausente = docenteRepo.findById(idDocenteAusente).orElse(null);

        if (ausente == null || ausente.getDepartamento() == null) {
            return null;
        }

        // Obtengo solo los compañeros de su departamento
        List<Docente> candidatos = docenteRepo.findByDepartamentoNombre(ausente.getDepartamento().getNombre());

        candidatos = candidatos.stream()
                .filter(d -> !d.getId().equals(ausente.getId()))
                .collect(Collectors.toList());

        if (candidatos.isEmpty()) {
            return null;
        }

        // Aplico el algoritmo de ordenación con doble criterio:
        // 1. Menor carga de guardias.
        // 2. Orden alfabético por apellidos como criterio de desempate determinista.
        candidatos.sort(Comparator
                .comparingInt((Docente d) -> d.getGuardiasRealizadas() == null ? 0 : d.getGuardiasRealizadas())
                .thenComparing(Docente::getApellidos));

        return candidatos.get(0);
    }
}