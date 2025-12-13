package com.sergiovd.gestiondocentes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() // Recursos estáticos
                        .requestMatchers("/login", "/auth/**").permitAll() // Login y Recuperación públicos
                        // Para permitir las imágenes
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/*.png", "/*.jpg", "/*.jpeg").permitAll()
                        .requestMatchers("/web/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login") // Nuestra página custom
                        .defaultSuccessUrl("/", true) // Redirigir al dashboard al entrar
                        .usernameParameter("email") // Usaremos el email para entrar
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Encriptación estándar
    }
}