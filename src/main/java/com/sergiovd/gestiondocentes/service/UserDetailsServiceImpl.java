package com.sergiovd.gestiondocentes.service;

import com.sergiovd.gestiondocentes.model.Docente;
import com.sergiovd.gestiondocentes.repository.DocenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private DocenteRepository docenteRepo;

    // Este método es el puente entre Spring Security y mi base de datos.
    // Cuando alguien intenta loguearse, Spring me llama aquí para que busque al usuario.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Busco al docente por email, que actúa como nombre de usuario en mi sistema
        Docente d = docenteRepo.findDocenteByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        // Construyo y devuelvo el objeto User estándar de Spring con los datos de mi Docente.
        // Aquí es donde asigno el rol y la contraseña encriptada para que el framework haga el cotejo.
        return User.builder()
                .username(d.getEmail())
                .password(d.getPassword())
                .roles("USER") // Asigno un rol genérico, la gestión fina de permisos la hago en los controladores
                .build();
    }
}