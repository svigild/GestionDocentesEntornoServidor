package com.sergiovd.gestiondocentes.config;

import com.sergiovd.gestiondocentes.model.Docente;
import com.sergiovd.gestiondocentes.repository.DepartamentoRepository;
import com.sergiovd.gestiondocentes.repository.DocenteRepository;
import com.sergiovd.gestiondocentes.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;

@Configuration
public class CsvDataLoader implements CommandLineRunner {

    @Autowired private DocenteRepository docenteRepo;
    @Autowired private DepartamentoRepository deptRepo;
    @Autowired private RolRepository rolRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // Compruebo si la tabla de docentes está vacía. Si es así, ejecuto la lógica de importación masiva
        // para inicializar el sistema con datos de prueba o reales desde un fichero CSV.
        if (docenteRepo.count() == 0) {
            System.out.println("CARGANDO DATOS DESDE CSV...");
            try {
                ClassPathResource resource = new ClassPathResource("docentes.csv");
                if (resource.exists()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
                    String line;
                    boolean header = true;

                    // Itero línea a línea el archivo
                    while ((line = reader.readLine()) != null) {
                        // Me salto la cabecera para no intentar parsear los títulos de las columnas
                        if (header) { header = false; continue; }

                        // Separo los campos por comas, que es el formato estándar del CSV
                        String[] data = line.split(",");

                        // Valido que la línea tenga todos los campos necesarios para evitar errores de índice
                        if(data.length < 6) continue;

                        Docente d = new Docente();
                        d.setNombre(data[0].trim());
                        d.setApellidos(data[1].trim());
                        d.setEmail(data[2].trim());
                        d.setSiglas(data[3].trim());

                        // Asigno una contraseña temporal encriptada y marco el usuario para que
                        // el sistema le obligue a cambiarla en su primer inicio de sesión.
                        d.setPassword(passwordEncoder.encode("temporal"));
                        d.setPasswordChanged(false);

                        // Parseo los datos numéricos
                        d.setTipoFuncionario(Integer.parseInt(data[4].trim()));
                        d.setNotaOposicion(Double.parseDouble(data[5].trim()));

                        // Inicializo valores por defecto para antigüedad y guardias
                        d.setFechaAntiguedad(LocalDate.now().minusYears(5));
                        d.setGuardiasRealizadas(0);

                        // Para esta carga inicial, asigno el primer departamento y el rol de profesor que encuentre
                        // para asegurar la integridad referencial sin complicar la lógica de importación.
                        d.setDepartamento(deptRepo.findAll().stream().findFirst().orElse(null));
                        d.setRol(rolRepo.findAll().stream().filter(r -> r.getNombre().equals("Profesor")).findFirst().orElse(null));

                        docenteRepo.save(d);
                    }
                    reader.close();
                    System.out.println("CSV CARGADO CORRECTAMENTE");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}