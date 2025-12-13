package com.sergiovd.gestiondocentes.controller;

import com.sergiovd.gestiondocentes.dto.SolicitudDTO;
import com.sergiovd.gestiondocentes.model.AsuntoPropio;
import com.sergiovd.gestiondocentes.model.Docente;
import com.sergiovd.gestiondocentes.repository.*;
import com.sergiovd.gestiondocentes.service.DocenteService;
import com.sergiovd.gestiondocentes.service.GuardiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DocenteController {

    @Autowired private DocenteService docenteService;
    @Autowired private DocenteRepository docenteRepo;
    @Autowired private AsuntoPropioRepository asuntoRepo;
    @Autowired private GuardiaService guardiaService;
    @Autowired(required = false) private JavaMailSender mailSender;
    @Autowired private DepartamentoRepository deptRepo;
    @Autowired private RolRepository rolRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    // Leo el número máximo de permisos diarios desde el archivo de configuración para no tener números mágicos en el código
    @Value("${app.config.max-permisos-diarios:3}")
    private int MAX_PERMISOS_DIARIOS;

    // --- HOME & LISTADO ---
    @GetMapping("/")
    public String home(Model model, Principal principal) {
        // Implementé esta validación para detectar si el usuario acaba de entrar con la contraseña temporal '1234'
        // Si es así, le redirijo forzosamente a la pantalla de cambio de contraseña por seguridad
        Docente d = docenteRepo.findDocenteByEmail(principal.getName()).orElse(null);
        if (d != null && Boolean.FALSE.equals(d.getPasswordChanged())) {
            return "redirect:/auth/forgot-password?forceChange=true";
        }

        long totalDocentes = docenteRepo.count();
        // Filtro las solicitudes que tienen 'aprobado' a null para saber cuántas hay pendientes de revisar
        long pendientes = asuntoRepo.findAll().stream().filter(a -> a.getAprobado() == null).count();

        // Obtengo todas las solicitudes para mostrarlas en el widget de actividad reciente
        List<AsuntoPropio> actividad = asuntoRepo.findAll();
        // Invierto la lista para mostrar primero las más nuevas (LIFO)
        Collections.reverse(actividad);
        // Me quedo solo con las 5 primeras para no saturar la interfaz del dashboard
        List<AsuntoPropio> ultimas5 = actividad.stream().limit(5).collect(Collectors.toList());

        model.addAttribute("totalDocentes", totalDocentes);
        model.addAttribute("solicitudesPendientes", pendientes);
        model.addAttribute("actividadReciente", ultimas5);

        return "index";
    }

    @GetMapping("/web/admin/estadisticas")
    public String verEstadisticas(Model model) {
        // Uso consultas JPQL personalizadas en el repositorio para obtener estas estadísticas de forma eficiente
        Docente masDisfruton = asuntoRepo.encontrarDocenteConMasDias();
        long numProfesInfo = docenteRepo.contarDocentesPorDepartamento("IFC");

        model.addAttribute("masDisfruton", masDisfruton);
        model.addAttribute("numProfesInfo", numProfesInfo);

        return "admin/stats";
    }

    @GetMapping("/web/docentes")
    public String listarDocentes(@RequestParam(required = false) String departamento, Model model) {
        List<Docente> docentes;

        // Implementé este if para el filtro por departamento. Si viene un parámetro en la URL, filtro la lista.
        // Si no, cargo la lista completa ordenada alfabéticamente para facilitar la búsqueda visual.
        if (departamento != null && !departamento.isEmpty() && !departamento.equals("Todos")) {
            docentes = docenteRepo.findByDepartamentoNombre(departamento);
        } else {
            docentes = docenteService.listarOrdenadosPorApellido();
        }

        model.addAttribute("listaDocentes", docentes);
        model.addAttribute("departamentos", deptRepo.findAll());
        model.addAttribute("deptSeleccionado", departamento);

        return "docentes/list";
    }

    // --- SOLICITUDES ---
    @GetMapping("/web/solicitud/nueva")
    public String formSolicitud(Model model) {
        model.addAttribute("solicitud", new SolicitudDTO());
        // Paso la lista de docentes por si el admin quiere registrar una solicitud en nombre de otro
        model.addAttribute("docentes", docenteService.listarTodos());
        return "solicitudes/form";
    }

    @PostMapping("/web/solicitud/guardar")
    public String guardarSolicitud(@ModelAttribute SolicitudDTO solicitudDto) {

        // Validaciones de negocio:

        // 1. No permitir fechas pasadas
        if (solicitudDto.getDiaSolicitado().isBefore(LocalDate.now())) {
            return "redirect:/web/solicitud/nueva?error=fecha";
        }

        // 2. Control de cupo diario (Requisito del centro)
        // Cuento cuántas personas faltan ese día concreto
        long genteEseDia = asuntoRepo.findAll().stream()
                .filter(a -> a.getDiaSolicitado().equals(solicitudDto.getDiaSolicitado())
                        && Boolean.TRUE.equals(a.getAprobado()))
                .count();

        if (genteEseDia >= MAX_PERMISOS_DIARIOS) {
            return "redirect:/web/solicitud/nueva?error=cupo";
        }

        Docente d = docenteRepo.findById(solicitudDto.getIdDocente()).orElse(null);
        if (d == null) return "redirect:/web/solicitud/nueva?error=usuario";

        // 3. Control de trimestres (Solo 1 día por trimestre)
        // Calculo el trimestre matemático del mes solicitado
        int mesSolicitado = solicitudDto.getDiaSolicitado().getMonthValue();
        int trimestreSolicitado = (mesSolicitado - 1) / 3 + 1;

        // Compruebo si el docente ya ha gastado un día en ese mismo trimestre y año
        boolean yaGastado = d.getAsuntosPropios().stream()
                .filter(a -> Boolean.TRUE.equals(a.getAprobado()))
                .anyMatch(a -> {
                    int m = a.getDiaSolicitado().getMonthValue();
                    int t = (m - 1) / 3 + 1;
                    return t == trimestreSolicitado &&
                            a.getDiaSolicitado().getYear() == solicitudDto.getDiaSolicitado().getYear();
                });

        if (yaGastado) {
            return "redirect:/web/solicitud/nueva?error=trimestre_agotado";
        }

        AsuntoPropio asunto = new AsuntoPropio();
        asunto.setDiaSolicitado(solicitudDto.getDiaSolicitado());
        asunto.setDescripcion(solicitudDto.getDescripcion());
        asunto.setDocente(d);
        asunto.setFechaTramitacion(LocalDateTime.now());
        asunto.setAprobado(null); // Inicialmente pendiente

        asuntoRepo.save(asunto);

        return "redirect:/web/solicitudes/mis-solicitudes/" + d.getId();
    }

    @PostMapping("/web/solicitud/subir-material")
    public String subirMaterial(@RequestParam("idSolicitud") Long idSolicitud,
                                @RequestParam("archivo") MultipartFile archivo) {
        try {
            if (!archivo.isEmpty()) {
                // Guardo el archivo físicamente en una carpeta del servidor
                String carpeta = "uploads/";
                byte[] bytes = archivo.getBytes();
                Path path = Paths.get(carpeta + archivo.getOriginalFilename());
                Files.createDirectories(path.getParent());
                Files.write(path, bytes);
                System.out.println("MATERIAL SUBIDO: " + archivo.getOriginalFilename());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        AsuntoPropio ap = asuntoRepo.findById(idSolicitud).get();
        return "redirect:/web/solicitudes/mis-solicitudes/" + ap.getDocente().getId() + "?uploadSuccess";
    }

    @GetMapping("/web/solicitudes/mis-solicitudes/{idDocente}")
    public String misSolicitudes(@PathVariable Long idDocente, Model model) {
        Docente d = docenteRepo.findById(idDocente).orElse(null);

        if (d != null) {
            model.addAttribute("docente", d);

            // Decidí usar el repositorio directamente en vez de d.getAsuntosPropios() para evitar problemas de caché de Hibernate.
            // Así me aseguro de que siempre veo la lista actualizada en tiempo real.
            List<AsuntoPropio> listaReal = asuntoRepo.findByDocenteIdOrderByDiaSolicitadoDesc(idDocente);
            model.addAttribute("misAsuntos", listaReal);

            // Calculo estadísticas al vuelo para mostrar los contadores de colores en la vista
            long diasPendientes = listaReal.stream()
                    .filter(a -> Boolean.TRUE.equals(a.getAprobado()) && !a.getDiaSolicitado().isBefore(LocalDate.now()))
                    .count();

            long diasGastados = listaReal.stream()
                    .filter(a -> Boolean.TRUE.equals(a.getAprobado()) && a.getDiaSolicitado().isBefore(LocalDate.now()))
                    .count();

            model.addAttribute("statsPendientes", diasPendientes);
            model.addAttribute("statsGastados", diasGastados);
        }
        return "solicitudes/my-list";
    }

    // --- ADMIN ---
    @GetMapping("/web/admin/validar")
    public String panelValidacion(Model model, Principal principal) {
        // Verificación de seguridad adicional: solo dejo pasar si tiene rol de Administrador (ID 1)
        Docente usuario = docenteRepo.findDocenteByEmail(principal.getName()).orElse(null);
        if (usuario == null || usuario.getRol().getId() != 1) {
            return "redirect:/";
        }

        List<AsuntoPropio> todas = asuntoRepo.findAll();
        List<AsuntoPropio> pendientes = todas.stream().filter(a -> a.getAprobado() == null).collect(Collectors.toList());

        // Implementé un comparador complejo para ordenar las solicitudes pendientes según baremo
        // (tipo de funcionario -> antigüedad -> nota oposición), útil para desempatar si faltan plazas
        pendientes.sort(
                Comparator.comparing((AsuntoPropio a) -> a.getDocente().getTipoFuncionario(), Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(a -> a.getDocente().getFechaAntiguedad(), Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(a -> a.getDocente().getNotaOposicion(), Comparator.nullsLast(Comparator.reverseOrder()))
        );

        List<AsuntoPropio> resueltas = todas.stream().filter(a -> a.getAprobado() != null).toList();
        pendientes.addAll(resueltas);
        model.addAttribute("todasSolicitudes", pendientes);
        return "solicitudes/admin-list";
    }

    @GetMapping("/web/admin/accion/{id}/{estado}")
    public String validarSolicitud(@PathVariable Long id, @PathVariable Boolean estado) {
        AsuntoPropio ap = asuntoRepo.findById(id).orElse(null);
        if (ap != null) {
            ap.setAprobado(estado);
            asuntoRepo.save(ap);

            // Notificación por email al docente con el resultado de la solicitud
            if (mailSender != null) {
                try {
                    SimpleMailMessage msg = new SimpleMailMessage();
                    msg.setTo(ap.getDocente().getEmail());
                    msg.setSubject("Resolución Solicitud Asuntos Propios");
                    msg.setText(estado ? "Su solicitud ha sido APROBADA." : "Su solicitud ha sido DENEGADA.");
                    mailSender.send(msg);
                } catch (Exception e) {
                    System.err.println("Fallo al enviar email: " + e.getMessage());
                }
            }
        }
        return "redirect:/web/admin/validar";
    }

    // --- GUARDIAS (Visualizacion del Cuadrante Mensual) ---
    @GetMapping("/web/guardias/panel")
    public String panelGuardias(Model model) {
        // Calculo los datos del mes actual para poder dibujar el calendario dinámicamente
        LocalDate hoy = LocalDate.now();
        int diasEnElMes = hoy.lengthOfMonth();

        // Calculo el día de la semana que empieza el mes para pintar los huecos vacíos antes del día 1
        int diaSemanaInicio = hoy.withDayOfMonth(1).getDayOfWeek().getValue() - 1;

        // Recupero todas las ausencias aprobadas de este mes para mostrarlas en el calendario
        List<AsuntoPropio> ausenciasMes = asuntoRepo.findAll().stream()
                .filter(a -> Boolean.TRUE.equals(a.getAprobado()))
                .filter(a -> a.getDiaSolicitado().getMonth() == hoy.getMonth() && a.getDiaSolicitado().getYear() == hoy.getYear())
                .toList();

        // Agrupo las ausencias por día (Key: Día del mes, Value: Lista de ausencias)
        // Esto facilita mucho pintar la vista en Thymeleaf iterando por días
        Map<Integer, List<AsuntoPropio>> mapaAusencias = ausenciasMes.stream()
                .collect(Collectors.groupingBy(a -> a.getDiaSolicitado().getDayOfMonth()));

        // Filtro la lista de docentes para el desplegable: solo muestro los que realmente faltan este mes
        // para facilitar la tarea al Jefe de Estudios
        Set<Long> idsAusentes = ausenciasMes.stream()
                .map(a -> a.getDocente().getId())
                .collect(Collectors.toSet());

        List<Docente> docentesConFaltas = docenteService.listarTodos().stream()
                .filter(d -> idsAusentes.contains(d.getId()))
                .toList();

        // Si no hay faltas, cargo todos los docentes como fallback para que la interfaz no se rompa
        if (docentesConFaltas.isEmpty()) docentesConFaltas = docenteService.listarTodos();

        model.addAttribute("mesActual", hoy.getMonth().getDisplayName(java.time.format.TextStyle.FULL, new Locale("es", "ES")).toUpperCase());
        model.addAttribute("anioActual", hoy.getYear());
        model.addAttribute("diasEnElMes", diasEnElMes);
        model.addAttribute("diaSemanaInicio", diaSemanaInicio);
        model.addAttribute("mapaAusencias", mapaAusencias);
        model.addAttribute("docentes", docentesConFaltas);
        model.addAttribute("hoy", hoy.getDayOfMonth());

        return "guardias/panel";
    }

    @GetMapping("/web/docentes/crear")
    public String formNuevoDocente(Model model) {
        model.addAttribute("docente", new Docente());
        model.addAttribute("departamentos", deptRepo.findAll());
        model.addAttribute("roles", rolRepo.findAll());
        return "docentes/form";
    }

    @PostMapping("/web/docentes/guardar-nuevo")
    public String guardarNuevoDocente(@ModelAttribute Docente docente) {
        // Genero una contraseña temporal y fuerzo al usuario a cambiarla en el primer login
        String passTemporal = "1234";
        docente.setPassword(passwordEncoder.encode(passTemporal));
        docente.setPasswordChanged(false);

        // Inicializo valores por defecto para evitar NullPointerExceptions más adelante
        if (docente.getGuardiasRealizadas() == null) docente.setGuardiasRealizadas(0);
        if (docente.getFechaAntiguedad() == null) docente.setFechaAntiguedad(LocalDate.now());

        docenteRepo.save(docente);

        // Envío las credenciales por correo
        if (mailSender != null) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setTo(docente.getEmail());
                msg.setSubject("Credenciales de Acceso");
                msg.setText("Usuario: " + docente.getEmail() + "\nContraseña: " + passTemporal);
                mailSender.send(msg);
            } catch (Exception e) {
                System.err.println("Error enviando email: " + e.getMessage());
            }
        }

        return "redirect:/web/docentes?creado=true";
    }

    // --- ALGORITMO DE ASIGNACION ---
    @PostMapping("/web/guardias/asignar")
    public String asignarGuardia(@RequestParam Long idDocenteAusente, Model model) {
        Docente ausente = docenteRepo.findById(idDocenteAusente).orElse(null);
        if (ausente == null) return "redirect:/web/guardias/panel";

        // Ejecuto el algoritmo de asignación en dos fases:
        // 1. Busco compañeros del mismo departamento (prioridad pedagógica).
        // 2. Ordeno por carga de trabajo (quien ha hecho menos guardias va primero).
        List<Docente> candidatos = docenteRepo.findAll().stream()
                .filter(d -> !d.getId().equals(ausente.getId())) // Excluyo al propio ausente
                .filter(d -> d.getDepartamento().getId().equals(ausente.getDepartamento().getId()))
                .sorted(Comparator.comparing(d -> d.getGuardiasRealizadas() == null ? 0 : d.getGuardiasRealizadas()))
                .toList();

        // Si no encuentro a nadie del mismo departamento, amplio la búsqueda a todo el claustro
        if (candidatos.isEmpty()) {
            candidatos = docenteRepo.findAll().stream()
                    .filter(d -> !d.getId().equals(ausente.getId()))
                    .sorted(Comparator.comparing(d -> d.getGuardiasRealizadas() == null ? 0 : d.getGuardiasRealizadas()))
                    .toList();
        }

        // Selecciono al mejor candidato (el primero de la lista ordenada)
        Docente sustituto = candidatos.isEmpty() ? null : candidatos.get(0);

        if (sustituto != null) {
            // Actualizo la carga de trabajo del sustituto incrementando su contador
            int actuales = sustituto.getGuardiasRealizadas() == null ? 0 : sustituto.getGuardiasRealizadas();
            sustituto.setGuardiasRealizadas(actuales + 1);
            docenteRepo.save(sustituto);

            // Busco todas las ausencias pendientes de cubrir de este profesor (aprobadas y futuras)
            List<AsuntoPropio> ausenciasSinCubrir = asuntoRepo.findAll().stream()
                    .filter(a -> a.getDocente().getId().equals(ausente.getId()))
                    .filter(a -> Boolean.TRUE.equals(a.getAprobado()))
                    .filter(a -> a.getSustituto() == null) // Importante: solo las que no tienen sustituto aún
                    .filter(a -> !a.getDiaSolicitado().isBefore(LocalDate.now()))
                    .toList();

            // Vinculo el sustituto encontrado a esas solicitudes y guardo los cambios.
            // Esto es lo que permite que el calendario muestre el nombre del sustituto en verde.
            for (AsuntoPropio ausencia : ausenciasSinCubrir) {
                ausencia.setSustituto(sustituto);
                asuntoRepo.save(ausencia);
            }
        }

        model.addAttribute("ausente", ausente);
        model.addAttribute("sustituto", sustituto);
        return "guardias/resultado";
    }
}