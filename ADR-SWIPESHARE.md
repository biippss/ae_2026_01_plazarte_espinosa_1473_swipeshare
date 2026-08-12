# ADR - Architecture Decision Records
**Platform:** SwipeShare  
**Autores:** Stéfano Espinosa, Israel Plazarte  
**Estado:** Aceptado para implementación  

---

## Control del documento

| Campo | Valor |
| :--- | :--- |
| **Versión** | 1.0 |
| **Estado** | Aceptado para implementación |
| **Alcance inicial** | Backend Microservicios, Cliente Móvil Android y Despliegue en Cloud |
| **Tecnologías base** | Kotlin + Spring Boot; Android Studio (Kotlin + Jetpack Compose); PostgreSQL; Nginx; Docker Compose; AWS Cognito & EC2 |
| **Repositorio objetivo** | GitHub / GitLab |

---

## 1. Propósito

Este documento conserva el fundamento técnico de las decisiones arquitectónicas del sistema SwipeShare. Cada registro sigue la estructura en formato Nygard: estado, contexto, decisión, alternativas consideradas y consecuencias. Los ADR complementan la documentación de arquitectura (SAD) y la especificación de requerimientos (SRS) para garantizar trazabilidad.

---

## 2. Convenciones

| Estado | Significado |
| :--- | :--- |
| **Propuesto** | Decisión en evaluación. |
| **Aceptado** | Decisión aprobada para implementación. |
| **Reemplazado** | Sustituida por otro ADR. |
| **Obsoleto** | Ya no aplica y no tiene reemplazo directo. |

**Numeración:** ADR-001, ADR-002, etc. Una decisión aceptada no se reescribe; si cambia, se crea un nuevo ADR que la reemplaza.

---

## Registros de Decisiones Arquitectónicas

### ADR-001: Adoptar una arquitectura de microservicios con API Gateway Nginx

| Campo | Contenido |
| :--- | :--- |
| **Estado** | Aceptado |
| **Fecha** | 2026-08-10 |
| **Ámbito** | Arquitectura General de Backend |
| **Relación** | SAD y SRS (RF-02, RF-05, RNF-03) |
| **Contexto** | El sistema debe aislar la gestión de usuarios y perfiles respecto a la lógica de negocio pesada (catálogo de ítems, procesamiento de swipes, matches y reseñas). Un monolito entrelazaría dominios que crecen a ritmos distintos. |
| **Decisión** | Implementar microservicios desacoplados en Spring Boot (`users` en puerto 8081 y `swipeshare` en puerto 8080) con bases de datos PostgreSQL independientes, orquestados mediante Docker Compose en AWS EC2 y expuestos a través de un API Gateway centralizado en Nginx escuchando en el puerto 80. |
| **Alternativas consideradas** | Monolito tradicional en Spring Boot; API Gateway con Spring Cloud Gateway. |
| **Consecuencias** | **Positivas:** Aislamiento de fallos, escalabilidad independiente por servicio y punto único de entrada para clientes móviles.<br>**Negativas:** Mayor sobrecarga en la configuración de enrutamiento en `nginx.conf` y redes internas de contenedores. |
| **Criterio de revisión** | Revisar cuando la complejidad de enrutamiento requiera balanceo de carga dinámico o descubrimiento de servicios (Service Discovery). |

---

### ADR-002: Usar Kotlin y Spring Boot para el desarrollo del backend

| Campo | Contenido |
| :--- | :--- |
| **Estado** | Aceptado |
| **Fecha** | 2026-08-10 |
| **Ámbito** | Backend de Microservicios |
| **Relación** | SAD y SRS (RNF-03) |
| **Contexto** | Se requiere un ecosistema empresarial robusto, fuertemente tipado, con soporte nativo para seguridad OAuth2 y fácil integración con contenedores. |
| **Decisión** | Desarrollar los microservicios utilizando Kotlin sobre el framework Spring Boot (versión 3.x), aprovechando la concisión del lenguaje, las funciones de extensión y la inyección de dependencias. |
| **Alternativas consideradas** | Java plano con Spring Boot; Node.js con Express; NestJS. |
| **Consecuencias** | **Positivas:** Código expresivo, reducción de errores de nulos (Null Safety), integración nativa con librerías Java y Spring Security.<br>**Negativas:** Tiempo de compilación inicial ligeramente superior respecto a soluciones Node.js. |
| **Criterio de revisión** | Revisar si los tiempos de arranque en frío impactan negativamente la infraestructura en la nube. |

---

### ADR-003: Delegar la autenticación a AWS Cognito y Spring Security OAuth2

| Campo | Contenido |
| :--- | :--- |
| **Estado** | Aceptado |
| **Fecha** | 2026-08-10 |
| **Ámbito** | Seguridad e Identidad |
| **Relación** | SAD y SRS (RF-01, RNF-01) |
| **Contexto** | El almacenamiento de contraseñas y la gestión manual de JWT en base de datos local incrementa los riesgos de seguridad y la complejidad de cifrado. |
| **Decisión** | Integrar AWS Cognito como Proveedor de Identidades (IdP). Los microservicios actúan como OAuth2 Resource Servers validando la firma de los tokens JWT emitidos por Cognito en cada petición protegida. |
| **Alternativas consideradas** | Autenticación JWT propia con Spring Security y HMAC-SHA256; Auth0; Keycloak autogestionado. |
| **Consecuencias** | **Positivas:** Delegación de la seguridad a infraestructura administrada por AWS, cumplimiento de estándares de cifrado y desacoplamiento de credenciales en DB.<br>**Negativas:** Dependencia de conectividad externa con endpoints JWK de AWS para validar tokens. |
| **Criterio de revisión** | Revisar si las cuotas gratuitas de AWS Cognito son superadas por el volumen de usuarios. |

---

### ADR-004: Usar PostgreSQL como base de datos por microservicio

| Campo | Contenido |
| :--- | :--- |
| **Estado** | Aceptado |
| **Fecha** | 2026-08-10 |
| **Ámbito** | Persistencia de Datos |
| **Relación** | SAD y SRS (RF-02, RF-05, RNF-03) |
| **Contexto** | Se requiere consistencia transaccional ACID para garantizar que las publicaciones, las ofertas de intercambio y los matches no generen inconsistencias. |
| **Decisión** | Asignar una instancia de PostgreSQL independiente para cada microservicio (`users_db` y `swipeshare_db`), aplicando el patrón Database per Service. |
| **Alternativas consideradas** | Base de datos única compartida; MongoDB; MySQL. |
| **Consecuencias** | **Positivas:** Aislamiento estricto de datos entre dominios y garantía de integridad referencial ACID en cada servicio.<br>**Negativas:** Imposibilidad de realizar JOINs directos entre tablas de usuarios e ítems a nivel de base de datos. |
| **Criterio de revisión** | Revisar si la latencia entre servicios para combinar información afecta el rendimiento. |

---

### ADR-005: Desarrollar la app móvil nativa en Android con Kotlin y MVVM

| Campo | Contenido |
| :--- | :--- |
| **Estado** | Aceptado |
| **Fecha** | 2026-08-10 |
| **Ámbito** | Cliente Móvil |
| **Relación** | SAD y SRS (RNF-02, RNF-07, RNF-08) |
| **Contexto** | Se requiere una aplicación móvil nativa con excelente rendimiento, fluidez en la navegación y desacoplamiento entre la interfaz visual y la lógica de negocio. |
| **Decisión** | Construir la app en Android Studio usando Kotlin nativo bajo el patrón de arquitectura MVVM (`models`, `services`, `viewmodels`, `ui`), utilizando Kotlin Coroutines para operaciones asíncronas en segundo plano. |
| **Alternativas consideradas** | Flutter; React Native; Arquitectura MVC tradicional en Android. |
| **Consecuencias** | **Positivas:** Acceso directo a las APIs nativas de Android, manejo transparente del estado visual y prevención de bloqueos en el hilo principal.<br>**Negativas:** Desarrollo exclusivo para la plataforma Android sin exportación directa a iOS. |
| **Criterio de revisión** | Revisar si se requiere dar soporte multiplataforma en el futuro. |

---

### ADR-006: Diseñar interfaces móviles con Jetpack Compose y Material 3

| Campo | Contenido |
| :--- | :--- |
| **Estado** | Aceptado |
| **Fecha** | 2026-08-10 |
| **Ámbito** | Interfaz de Usuario Móvil |
| **Relación** | SAD y SRS (RNF-02, RNF-06) |
| **Contexto** | La interfaz debe ofrecer pantallas fluidas, con recomposición eficiente ante cambios de estado y una integración directa con los ViewModel de la capa MVVM, evitando la fragmentación entre archivos de layout XML y controladores en Kotlin. |
| **Decisión** | Definir el diseño de las pantallas utilizando Jetpack Compose con componentes de Material 3, garantizando interfaces responsivas, reactivas al estado y adaptables. |
| **Alternativas consideradas** | XML Layouts tradicionales con ConstraintLayout; Views dinámicas creadas por código Kotlin imperativo. |
| **Consecuencias** | **Positivas:** Código más conciso al eliminar el árbol de vistas XML, recomposición automática de la interfaz ante cambios de estado y observación directa de los LiveData del ViewModel mediante `observeAsState()`.<br>**Negativas:** Curva de aprendizaje del modelo declarativo (estado, recomposición) y necesidad de mantener `AndroidView` como puente si en el futuro se requiere integrar algún componente nativo basado en XML. |
| **Criterio de revisión** | Revisar si se requiere compatibilidad con vistas XML heredadas (por ejemplo, mediante `AndroidView`) para integrar componentes visuales de terceros. |

---

### ADR-007: Usar Retrofit y OkHttp Interceptor para el consumo de la API REST

| Campo | Contenido |
| :--- | :--- |
| **Estado** | Aceptado |
| **Fecha** | 2026-08-10 |
| **Ámbito** | Capa de Red Móvil |
| **Relación** | SAD y SRS (RNF-01, RNF-02, RNF-05) |
| **Contexto** | La app móvil necesita consumir los endpoints JSON expuestos por el API Gateway (Nginx) adjuntando de forma transparente el token JWT en cada solicitud. |
| **Decisión** | Implementar Retrofit 2 con convertidor Gson/Moshi para la capa de red y configurar un Interceptor en OkHttpClient que inyecte automáticamente el encabezado `Authorization: Bearer <token>` desde `SharedPreferences`. |
| **Alternativas consideradas** | Volley; Ktor Client; HttpURLConnection manual. |
| **Consecuencias** | **Positivas:** Mapeo automático de JSON a Data Classes de Kotlin, inyección limpia de cabeceras de seguridad y manejo estructurado de errores HTTP.<br>**Negativas:** Dependencia de librerías de terceros dentro del módulo móvil. |
| **Criterio de revisión** | Revisar si cambios en el contrato de la API exigen actualizar los DTOs de Retrofit. |

---

### ADR-008: Integración HTTP síncrona con RestTemplate para actualización de Karma

| Campo | Contenido |
| :--- | :--- |
| **Estado** | Aceptado |
| **Fecha** | 2026-08-10 |
| **Ámbito** | Comunicación entre Microservicios |
| **Relación** | SAD y SRS (RF-04, RF-09) |
| **Contexto** | Al registrar una reseña en SwipeShare, el sistema debe notificar al microservicio Users para incrementar o decrementar el karma del usuario objetivo. |
| **Decisión** | Utilizar `RestTemplate` para realizar una llamada HTTP PUT directa e in-process hacia el endpoint `/api/internal/users/{id}/karma`. Se agrega un bloque try-catch para registrar errores sin abortar la creación de la reseña si el servicio de usuarios no responde. |
| **Alternativas consideradas** | Comunicación asíncrona mediante un bróker de mensajes (RabbitMQ / Apache Kafka); Feign Client. |
| **Consecuencias** | **Positivas:** Implementación directa sin añadir middleware de mensajería ni sobrecarga de infraestructura.<br>**Negativas:** Acoplamiento temporal síncrono entre ambos microservicios durante el envío de la reseña. Además, la implementación actual apunta a `http://localhost:8081`, en lugar del nombre de servicio de Docker Compose (`users`), por lo que solo resuelve correctamente si ambos contenedores comparten red de host; se debe parametrizar mediante variable de entorno antes de un despliegue real. |
| **Criterio de revisión** | Revisar si la indisponibilidad del servicio de usuarios requiere migrar a mensajería asíncrona. |

---

### ADR-009: Despliegue de infraestructura en AWS EC2 con Docker Compose

| Campo | Contenido |
| :--- | :--- |
| **Estado** | Aceptado |
| **Fecha** | 2026-08-10 |
| **Ámbito** | Infraestructura y Computación en la Nube |
| **Relación** | SAD y SRS (RNF-03, RNF-04) |
| **Contexto** | Se requiere publicar el backend en un entorno IaaS en la nube garantizando reproducibilidad y facilidad de gestión para el consumo desde la app móvil. |
| **Decisión** | Aprovisionar una instancia AWS EC2 (Ubuntu Linux), configurar Security Groups abriendo los puertos 80 y 9090, y desplegar la pila completa (Nginx, Microservicios y bases de datos PostgreSQL) mediante Docker Compose. |
| **Alternativas consideradas** | Render / Heroku; AWS ECS con Fargate; Despliegue manual sobre máquina virtual sin contenedores. |
| **Consecuencias** | **Positivas:** Reproducibilidad idéntica entre el ambiente local y la nube, aislamiento total de servicios y costo controlado dentro de la capa gratuita de AWS.<br>**Negativas:** Responsabilidad manual sobre el mantenimiento de la VM de EC2 y actualizaciones del motor de Docker. |
| **Criterio de revisión** | Revisar si la demanda requiere migrar a orquestadores autogestionados (Kubernetes / ECS). |

---

### ADR-010: Estrategia de Monorepo estructurado por carpetas

| Campo | Contenido |
| :--- | :--- |
| **Estado** | Aceptado |
| **Fecha** | 2026-08-10 |
| **Ámbito** | Gestión de Código Fuente |
| **Relación** | SAD y SRS (Mantenibilidad y Trazabilidad GitFlow) |
| **Contexto** | Se requiere mantener organizados los proyectos de backend, cliente móvil y configuraciones de infraestructura en el repositorio sin dispersar el código. |
| **Decisión** | Adoptar la estructura de Monorepo separando el proyecto en las carpetas `/backend` (microservicios y Docker Compose) y `/mobile` (proyecto Android Studio). |
| **Alternativas consideradas** | Repositorios totalmente independientes en GitHub; Mezcla de archivos en la raíz. |
| **Consecuencias** | **Positivas:** Cambios atómicos, trazabilidad unificada de commits y facilidad de revisión académica.<br>**Negativas:** Requiere disciplina para evitar cruce accidental de scripts entre módulos. |
| **Criterio de revisión** | Revisar si los permisos de acceso o integración continua exigen dividir los repositorios. |

---

## Anexo A. Índice de decisiones

| ID | Decisión | Estado |
| :--- | :--- | :--- |
| **ADR-001** | Adoptar una arquitectura de microservicios con API Gateway Nginx | Aceptado |
| **ADR-002** | Usar Kotlin y Spring Boot para el desarrollo del backend | Aceptado |
| **ADR-003** | Delegar la autenticación a AWS Cognito y Spring Security OAuth2 | Aceptado |
| **ADR-004** | Usar PostgreSQL como base de datos por microservicio | Aceptado |
| **ADR-005** | Desarrollar la app móvil nativa en Android con Kotlin y MVVM | Aceptado |
| **ADR-006** | Diseñar interfaces móviles con Jetpack Compose y Material 3 | Aceptado |
| **ADR-007** | Usar Retrofit y OkHttp Interceptor para el consumo de la API REST | Aceptado |
| **ADR-008** | Integración HTTP síncrona con RestTemplate para actualización de Karma | Aceptado |
| **ADR-009** | Despliegue de infraestructura en AWS EC2 con Docker Compose | Aceptado |
| **ADR-010** | Estrategia de Monorepo estructurado por carpetas | Aceptado |

---

## Anexo B. Relación SRS → SAD → ADR

| Necesidad / Atributo SRS | Respuesta en SAD | ADR Principal |
| :--- | :--- | :--- |
| **Aislamiento de Dominios y Escalabilidad** | Arquitectura de Microservicios con Nginx Gateway | ADR-001, ADR-002, ADR-010 |
| **Seguridad e Identidad** | Autenticación Federada con AWS Cognito y JWT | ADR-003, ADR-007 |
| **Integridad y Persistencia** | PostgreSQL por Microservicio | ADR-004 |
| **Arquitectura Móvil y Fluidez** | App Nativa Android con MVVM y Corrutinas | ADR-005, ADR-006 |
| **Interoperabilidad Móvil-Backend** | API REST consumida mediante Retrofit e Interceptor | ADR-007 |
| **Comunicación Inter-servicio** | Cliente HTTP sincrónico RestTemplate para Karma | ADR-008 |
| **Despliegue e Infraestructura Cloud** | Contenerización Docker Compose sobre AWS EC2 (IaaS) | ADR-009 |
