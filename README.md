# Proyecto: Gestion de Docentes
<img width="1919" height="919" alt="image" src="https://github.com/user-attachments/assets/c2f48f6e-f2d7-4b7c-bbfb-bd4a74dbb2a0" />


**Alumno:** Sergio Vigil Díaz

**Asignatura:** Desarrollo Web en Entorno Servidor (DWES)

**Curso:** 2025/2026

**Tecnologia:** Java 17 (o 21) + Spring Boot 3 + MySQL + Bootstrap 5

---

## Descripcion del Proyecto
Aplicacion web para la gestion integral de un centro educativo (CIFP), permitiendo la administracion del claustro de profesores, la gestion de guardias y ausencias, y la solicitud y validacion de dias de asuntos propios.

---

## Memoria Tecnica: Cumplimiento de Requisitos

A continuacion se detalla como se ha implementado cada punto de la rubrica de evaluacion en el codigo fuente.

### 1. Arquitectura y Calidad del Codigo

#### Estructura de Paquetes (0.5 Puntos)
El proyecto sigue el patron MVC y la arquitectura en capas estandar de Spring Boot:
- config: Configuraciones globales (SecurityConfig, CsvDataLoader).
- controller: Manejo de peticiones HTTP (DocenteController, AuthController).
- model: Entidades JPA (Docente, AsuntoPropio, Departamento, Rol).
- repository: Interfaces de acceso a datos (JpaRepository).
- service: Logica de negocio (DocenteService, GuardiaService).
- dto: Objetos de transferencia de datos (SolicitudDTO).

#### Estilo de Codificacion (0.5 Puntos)
Se siguen las convenciones de Spring Framework y Java Standard:
- Inyeccion de dependencias mediante @Autowired (y por constructor).
- Nomenclatura CamelCase para metodos y variables.
- Uso de anotaciones estandar (@Service, @Controller, @Entity, @Repository).

#### Nomenclatura de Rutas (0.5 Puntos)
Jerarquia de URLs uniforme y semantica:
- /web/docentes: Listado del claustro.
- /web/solicitud/nueva: Formulario de solicitud.
- /web/admin/validar: Panel de administracion.
- /web/guardias/panel: Cuadrante de guardias.

---

### 2. Modelo de Datos y DTOs

#### Entidades y @JsonIgnore (2 Puntos)
Para evitar recursividad infinita (bucles) en la serializacion de objetos, se ha aplicado @JsonIgnore en las relaciones bidireccionales:

@OneToMany(mappedBy = "docente", fetch = FetchType.LAZY)
@JsonIgnore
private List<AsuntoPropio> asuntosPropios;

#### Creacion de DTO (1.5 + 2 Puntos)
Se ha implementado SolicitudDTO en el paquete dto. Cumple el doble requisito de ser util para una entidad y combinar datos de varias entidades (ID de Docente + Datos de AsuntoPropio), desacoplando la vista del modelo de base de datos.

---

### 3. Casos de Uso (Logica de Negocio)

#### UC2: Solicitar Dia Propio (1.25 Puntos)
Implementado en DocenteController. Al guardar una solicitud:
1. Estado: Se guarda como null (Pendiente).
2. Validacion Fecha: No permite fechas pasadas.
3. Validacion Cupo: Verifica MAX_PERMISOS_DIARIOS (Configurable en application.properties).
4. Validacion Trimestral: Algoritmo matematico (mes - 1) / 3 + 1 para asegurar un solo dia por trimestre.

#### UC5: Validar Dias (1.25 Puntos)
Panel de administracion donde se aprueban o deniegan solicitudes.
- Notificacion: Envio automatico de email al docente mediante JavaMailSender con la resolucion.

#### UC6: Consultar Dias Propios (1 Punto)
Vista my-list.html. Muestra el historial completo con indicadores visuales de estado (Pendiente, Aprobado, Rechazado).

---

### 4. Consultas Especificas (Repositorios)

- Docentes por Apellido: Sort.by("apellidos") en Service. (Vista: /web/docentes)
- Docentes por Depto: docenteRepo.findByDepartamentoNombre(). (Vista: Filtro en list.html)
- N Profesores por Depto: JPQL SELECT COUNT(d)... (Vista: Panel de Estadisticas)
- Dias Pendientes Disfrute: Filtro Java aprobado=true && fecha >= hoy. (Vista: Mis Solicitudes)
- Docente mas dias disfrutados: JPQL con ORDER BY COUNT DESC LIMIT 1. (Vista: Panel de Estadisticas)

---

### 5. Funcionalidades Extra (MVP)

- Carga de CSV: Clase CsvDataLoader que carga docentes al inicio si la BBDD esta vacia.
- Seguridad: Contraseñas encriptadas con BCrypt.
- Gestion de Password: Generacion de contraseña temporal y cambio forzoso al primer inicio de sesion.
- Cuadrante de Guardias: Visualizacion dinamica generada desde el controlador (MVP sin persistencia masiva de horarios).

---

## Instrucciones de Ejecucion

### 1. Base de Datos
1. Crear una base de datos MySQL llamada `gestion_docentes`.
2. **Importante:** Verificar el archivo `src/main/resources/application.properties` y ajustar las lineas `spring.datasource.username` y `spring.datasource.password` segun la configuracion de su MySQL local.
3. La aplicacion creara las tablas automaticamente al arrancar (`hibernate.ddl-auto=update`).

### 2. Carga Inicial de Datos
El sistema detectara si la base de datos esta vacia y ejecutara CsvDataLoader para importar los datos de src/main/resources/docentes.csv.

### 3. Credenciales de Acceso
El sistema genera las siguientes credenciales iniciales (Contraseña temporal: 1234):

* Administrador: admin@educastur.org
* Profesor: sergio@educastur.org

Nota: Al acceder por primera vez, el sistema obligara a cambiar la contraseña por motivos de seguridad.

---

## Galeria Visual de la Aplicacion

A continuacion se muestran capturas reales del funcionamiento del sistema.

### 1. Seguridad y Acceso
Pantalla de inicio de sesion con diseño corporativo y sistema de cambio de contraseña forzoso al primer acceso.
<img width="1919" height="923" alt="image" src="https://github.com/user-attachments/assets/c5d88b6c-735e-42f4-adc2-d1cf32036f87" />
<img width="1919" height="921" alt="image" src="https://github.com/user-attachments/assets/704b1383-30b0-454a-b78e-e460c1c11659" />


### 2. Gestion del Claustro y Filtros
Listado de docentes cargado via CSV, con filtro funcional por Departamento y ordenacion por apellidos.
<img width="1904" height="918" alt="image" src="https://github.com/user-attachments/assets/44b663d8-42e2-4101-87d7-753b0eacf934" />


### 3. Solicitudes y Estadisticas Personales
Panel del profesor donde puede ver sus solicitudes y, destacado en azul, el calculo en tiempo real de los dias pendientes de disfrutar.
<img width="1918" height="924" alt="image" src="https://github.com/user-attachments/assets/d62ccb62-1052-4966-b122-3749a36890c5" />


### 4. Administracion y Algoritmo
Panel de validacion donde el Admin recibe las peticiones ordenadas por el algoritmo de prioridad (Tipo Funcionario > Antiguedad > Nota).
<img width="1919" height="918" alt="image" src="https://github.com/user-attachments/assets/247bfbfe-8744-4818-ae7d-25aab89f8593" />


### 5. Cuadrante de Guardias
Visualizacion del horario generada dinamicamente desde Java. El sistema detecta automaticamente las faltas y las marca en rojo para solicitar sustituto.
<img width="1896" height="916" alt="image" src="https://github.com/user-attachments/assets/04ebd6a0-f77c-4655-a985-022ec5a1f8c9" />
<img width="1918" height="717" alt="image" src="https://github.com/user-attachments/assets/3dd06653-937a-4802-bcfe-77656a311337" />
<img width="1904" height="915" alt="image" src="https://github.com/user-attachments/assets/414c7553-e72c-472c-b056-9f6b498df2da" />
<img width="1306" height="859" alt="image" src="https://github.com/user-attachments/assets/d7859ec8-1334-425b-a76e-a3f7e0754d60" />

# Cursos Completados

## 1. Desarrollo de una aplicación web con Spring Boot
Enlace al diploma https://openwebinars.net/cert/N5He




