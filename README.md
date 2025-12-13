# Proyecto: Gestión de Docentes

**Asignatura:** Desarrollo Web en Entorno Servidor (DWES)  
**Tecnología:** Spring Boot + Thymeleaf + MySQL + Bootstrap 5

---

## 📋 Descripción del Proyecto
Aplicación web para la gestión integral de un centro educativo, permitiendo la administración del claustro de profesores, la gestión de guardias y ausencias, y la solicitud y validación de días de asuntos propios.

---

## ✅ Cumplimiento de Requisitos (Memoria Técnica)

A continuación se detalla cómo se ha implementado cada punto de la rúbrica de evaluación en el código fuente.

### 1. Arquitectura y Calidad del Código

#### 📂 Estructura de Paquetes (0.5 Puntos)
El proyecto sigue el patrón MVC y la arquitectura en capas estándar de Spring Boot:
- **`config`**: Configuraciones globales (`SecurityConfig`, `CsvDataLoader`).
- **`controller`**: Manejo de peticiones HTTP (`DocenteController`, `AuthController`).
- **`model`**: Entidades JPA (`Docente`, `AsuntoPropio`, `Departamento`, `Rol`).
- **`repository`**: Interfaces de acceso a datos (`JpaRepository`).
- **`service`**: Lógica de negocio (`DocenteService`, `GuardiaService`).
- **`dto`**: Objetos de transferencia de datos (`SolicitudDTO`).

#### 📝 Estilo de Codificación (0.5 Puntos)
Se siguen las convenciones de **Spring Framework** y **Java Standard**:
- Inyección de dependencias mediante `@Autowired`.
- Nomenclatura *CamelCase* para métodos y variables.
- Uso de anotaciones estándar (`@Service`, `@Controller`, `@Entity`).

#### 🔗 Nomenclatura de Rutas (0.5 Puntos)
Jerarquía de URLs uniforme y semántica:
- `/web/docentes`: Listado del claustro.
- `/web/solicitud/nueva`: Formulario de solicitud.
- `/web/admin/validar`: Panel de administración.
- `/web/guardias/panel`: Cuadrante de guardias.

---

### 2. Modelo de Datos y DTOs

#### 🔄 Entidades y @JsonIgnore (2 Puntos)
Para evitar recursividad infinita (bucles) en la serialización de objetos, se ha aplicado `@JsonIgnore` en las relaciones bidireccionales:

```java
// En Docente.java
@OneToMany(mappedBy = "docente", fetch = FetchType.LAZY)
@JsonIgnore 
private List<AsuntoPropio> asuntosPropios;
