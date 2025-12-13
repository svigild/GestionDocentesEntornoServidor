package com.sergiovd.gestiondocentes.config;

import com.sergiovd.gestiondocentes.model.Departamento;
import com.sergiovd.gestiondocentes.model.Docente;
import com.sergiovd.gestiondocentes.model.Rol;
import com.sergiovd.gestiondocentes.repository.DepartamentoRepository;
import com.sergiovd.gestiondocentes.repository.DocenteRepository;
import com.sergiovd.gestiondocentes.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class AdminLoader {

    @Autowired PasswordEncoder encoder;

    @Bean
    CommandLineRunner initAdmin(DocenteRepository docenteRepo,
                                RolRepository rolRepo,
                                DepartamentoRepository deptRepo) {
        return args -> {

            // Compruebo si la base de datos de roles está vacía. Si es así, creo el rol de Dirección
            // para asegurar que la aplicación tenga al menos un nivel de permisos funcional al arrancar.
            if (rolRepo.count() == 0) {
                Rol rDir = new Rol();
                rDir.setNombre("Dirección");
                rDir.setOrden(1);
                rolRepo.save(rDir);
            }

            // Hago lo mismo con los departamentos. Creo uno por defecto (Informática) para evitar
            // errores de claves foráneas al intentar crear el primer usuario si la tabla estuviera vacía.
            if (deptRepo.count() == 0) {
                Departamento dInfo = new Departamento();
                dInfo.setNombre("Informática");
                dInfo.setCodigo("IFC");
                dInfo.setTelefono("000000000");
                deptRepo.save(dInfo);
            }

            // Busco si ya existe el usuario administrador en la base de datos utilizando su email.
            Docente admin = docenteRepo.findDocenteByEmail("admin@educastur.org").orElse(null);

            if (admin == null) {
                // Si no existe, instancio un nuevo objeto Docente con los datos del administrador principal.
                // Le asigno el rol y el departamento que acabo de asegurar que existen (IDs 1).
                admin = new Docente();
                admin.setNombre("Administrador");
                admin.setApellidos("Principal");
                admin.setEmail("admin@educastur.org");
                admin.setSiglas("ADM");
                admin.setRol(rolRepo.findById(1L).orElse(null));
                admin.setDepartamento(deptRepo.findById(1L).orElse(null));
                admin.setTipoFuncionario(1);
                admin.setNotaOposicion(10.0);
                admin.setFechaAntiguedad(LocalDate.of(2000, 1, 1));
                admin.setGuardiasRealizadas(0);

                System.out.println("ADMINLOADER: Creando usuario Admin nuevo...");
            } else {
                System.out.println("ADMINLOADER: Usuario Admin encontrado. Actualizando credenciales...");
            }

            // Esta parte es importante para el entorno de desarrollo: fuerzo la contraseña a '1234'
            // encriptándola de nuevo cada vez que arranca la aplicación.
            // Esto me permite recuperar el acceso fácilmente si olvido la clave o reinicio la base de datos.
            admin.setPassword(encoder.encode("1234"));

            docenteRepo.save(admin);

            System.out.println("CONTRASEÑA DE ADMIN RESTAURADA A '1234'");
        };
    }
}