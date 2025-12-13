package com.sergiovd.gestiondocentes.controller;

import com.sergiovd.gestiondocentes.model.Docente;
import com.sergiovd.gestiondocentes.repository.DocenteRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Implementé este controlador global para gestionar datos transversales a toda la aplicación.
 * Al usar @ControllerAdvice, me aseguro de que estos atributos estén disponibles en todas las vistas Thymeleaf
 * sin tener que añadirlos manualmente en cada método de cada controlador.
 */
@ControllerAdvice
public class GlobalController {

    @Autowired private DocenteRepository docenteRepo;
    @Autowired private HttpServletRequest request;

    // Inyecto la URI actual en todas las vistas. Esto lo necesito principalmente para la barra de navegación,
    // para poder marcar visualmente en qué sección se encuentra el usuario (clase 'active' de Bootstrap).
    @ModelAttribute("currentUri")
    public String getCurrentUri() {
        return request.getRequestURI();
    }

    // Inyecto el objeto del usuario logueado en todas las peticiones.
    // De esta forma, en cualquier HTML puedo acceder a 'usuarioLogueado.nombre' o 'usuarioLogueado.rol'
    // sin tener que consultar la base de datos repetidamente en cada controlador.
    @ModelAttribute("usuarioLogueado")
    public Docente agregarUsuarioGlobal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Compruebo que haya una sesión iniciada y que no sea un usuario anónimo
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            // Busco al docente por el email que Spring Security guarda en el contexto
            return docenteRepo.findDocenteByEmail(auth.getName()).orElse(null);
        }
        return null;
    }
}