package com.sergiovd.gestiondocentes.service;

import com.sergiovd.gestiondocentes.model.Docente;

import java.util.List;

public interface DocenteService {
    List<Docente> listarTodos();
    List<Docente> listarOrdenadosPorApellido();
}
