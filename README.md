# SwipeShare - Backend Microservices Platform

[![Build & Test](https://img.shields.io/badge/Coverage-100%25-brightgreen.svg)](https://github.com/pucetec/swipe-share)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3%2B-6DB33F.svg)](https://spring.io/projects/spring-boot)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0%2B-7F52FF.svg)](https://kotlinlang.org/)
[![Docker Compose](https://img.shields.io/badge/Docker%20Compose-2.x-2496ED.svg)](https://www.docker.com/)
[![AWS EC2](https://img.shields.io/badge/AWS-EC2%20Ubuntu-232F3E.svg)](https://aws.amazon.com/ec2/)
[![AWS Cognito](https://img.shields.io/badge/Security-AWS%20Cognito%20JWT-FF9900.svg)](https://aws.amazon.com/cognito/)

Plataforma de economía colaborativa e intercambio universitario (*"Less buying, more sharing"*) desarrollada para la **Pontificia Universidad Católica del Ecuador (PUCE)**. Permite a los estudiantes intercambiar libros, herramientas, equipos y materiales mediante una dinámica de *Swipe* (Like/Dislike), gestión de Matches, sistema de Karma/Reputación y autenticación restringida al dominio institucional `@puce.edu.ec`.

---

## Arquitectura del Sistema

El backend sigue una **Arquitectura de Microservicios Desacoplados**, orquestados en contenedores con **Docker Compose** sobre una instancia **AWS EC2 (Ubuntu Linux)** y expuestos mediante **Nginx** como API Gateway y Reverse Proxy en el puerto HTTP estándar `80`.

```text
                                +---------------------------------------+
                                |          Cliente Móvil Android        |
                                |       (Jetpack Compose + Retrofit)    |
                                +-------------------+-------------------+
                                                    |
                                         HTTP / REST (Puerto 80)
                                                    |
                                                    v
                               +--------------------+-------------------+
                               |         Nginx API Gateway / Proxy      |
                               |         (Puerto 80 en AWS EC2)         |
                               +---------+--------------------+---------+
                                         |                    |
                         /api/users/*    |                    | /api/swipes, /api/matches,
                                         v                    v /api/items, /api/public/*
                           +-------------+----+          +----+---------------+
                           |  Users Service   |          | SwipeShare Service |
                           |  (Puerto 8081)   |          |  (Puerto 8080)     |
                           |   Spring Boot    |          |   Spring Boot      |
                           +--------+---------+          +--------+-----------+
                                    |                             |
                                    v                             v
                           +--------+---------+          +--------+-----------+
                           | Users DB (PGSQL) |          |SwipeShare DB(PGSQL)|
                           +------------------+          +--------------------+
```

---

## 📦 Componentes y Microservicios

### 1. `users-service` (Puerto `8081`)
* **Propósito:** Gestión de perfiles de usuario, reputación, acumulación/ajuste de puntos **Karma** e integración interna.
* **Base de Datos:** PostgreSQL (`users-db`).
* **Endpoints Principales:**
  * `GET /api/users/me` - Obtener perfil del usuario autenticado.
  * `POST /api/users/me` - Registrar/sincronizar perfil inicial con validación institucional `@puce.edu.ec`.
  * `PUT /api/users/me` - Actualizar información básica (bio, teléfono).
  * `DELETE /api/users/me` - Eliminar perfil.
  * `GET /api/users/{cognitoId}` - Obtener perfil público de otro estudiante.
  * `PATCH /api/users/me/karma` - Consultar o ajustar saldo Karma.
  * `POST /api/users/internal/{cognitoId}/karma` - Endpoint interno para recompensa de Karma (+5 al aprobar match o al recibir reseña positiva).

### 2. `swipeshare-service` (Puerto `8080`)
* **Propósito:** Núcleo de la aplicación. Administración del catálogo de ítems, procesamiento de gestos de *Swipe* (Like/Dislike), solicitudes de Match, confirmación de trueques y sistema de reseñas.
* **Base de Datos:** PostgreSQL (`swipeshare-db`).
* **Endpoints Principales:**
  * `POST /api/items` - Publicar un nuevo artículo en el catálogo.
  * `GET /api/items/feed` - Obtener artículos disponibles para el usuario (excluye propios y ya intercambiados).
  * `GET /api/items/me` - Listar publicaciones propias.
  * `POST /api/swipes` - Procesar me gusta / rechazo sobre un artículo.
  * `POST /api/matches` - Crear solicitud directa de intercambio.
  * `GET /api/matches/me` - Listar coincidencias y solicitudes activas.
  * `PATCH /api/matches/{id}/status` - Aprobar/Rechazar match (al aprobar por primera vez se otorgan +5 puntos Karma a ambos usuarios mediante llamada REST interna).
  * `POST /api/reviews` - Enviar calificación de 1 a 5 estrellas a otro usuario post-intercambio.
  * `GET /api/public/stats` - Estadísticas globales de impacto de la comunidad (total de matches e ítems activos).

### 3. `nginx-gateway` (Puerto `80`)
* **Propósito:** API Gateway y Reverse Proxy. Centraliza el tráfico de clientes externos (Android/Postman) e implementa enrutamiento interno a la red de contenedores Docker.

---

## Autenticación y Seguridad Institucional

1. **Delegación de Identidad:** La autenticación es administrada por **AWS Cognito**.
2. **Validación Institucional en 2 Niveles:**
   * **Frontend (Android):** `AuthViewModel` aplica filtro que exige dominio `@puce.edu.ec`.
   * **Backend (`UserService`):** Valida la firma del JWT otorgado por Cognito, verificando que la clave `email` pertenezca al dominio `@puce.edu.ec` y `email_verified == true`.
3. **Control de Acceso (Spring Security):** Todos los endpoints privados verifican la presencia del token `Bearer <JWT>` y las autoridades correspondientes. Intentos no autorizados retornan códigos de respuesta `401 Unauthorized` o `403 Forbidden`.

---

## Registro de Trazabilidad y Auditoría (MDC Logging)

El sistema utiliza un filtro de interceptación HTTP (`HttpLoggingFilter`) configurado con **MDC (Mapped Diagnostic Context)** para asegurar trazabilidad punta a punta (*End-to-End Tracing*):

```text
users-db       | 2026-08-12 18:44:13.408 UTC [15289] LOG: execute select ... where u1_0.cognito_id=$1
users          | 2026-08-12T18:44:13.891Z | INFO | users | sub=94a8b448-6031-7054-cb6e-ceb53f53c330 | c.pucetec.users.config.HttpLoggingFilter | event=http.response | msg=200 GET /api/users/me
nginx-gateway  | 157.100.138.74 - - [12/Aug/2026:18:44:13 +0000] "GET /api/users/me HTTP/1.1" 200 195 "-" "PostmanRuntime/7.56.0"
```

* **Campos Auditados:** Timestamp ISO-8601, Microservicio, `sub` (ID único de usuario Cognito) y Evento HTTP (`event=http.request` / `event=http.response`).

---

## Architecture Decision Record (ADR-001)

### ADR-001: Adoptar una arquitectura de microservicios con API Gateway Nginx

| Campo | Contenido |
| :--- | :--- |
| **Estado** | Aceptado |
| **Fecha** | 2026-08-10 |
| **Ámbito** | Arquitectura General de Backend |
| **Relación** | SAD y SRS (RF-02, RF-05, RNF-03) |
| **Contexto** | El sistema debe aislar la gestión de usuarios, perfiles y Karma respecto a la lógica de negocio pesada (catálogo de ítems, procesamiento de swipes, matches y reseñas). Un monolito entrelazaría dominios con cargas de trabajo y persistencia distintas. |
| **Decisión** | Implementar microservicios desacoplados en **Spring Boot** (`users` en puerto `8081` y `swipeshare` en puerto `8080`) con bases de datos **PostgreSQL** independientes, orquestados mediante **Docker Compose** en **AWS EC2** y expuestos a través de un API Gateway centralizado en **Nginx escuchando en el puerto 80**. |
| **Alternativas** | Monolito tradicional en Spring Boot; API Gateway con Spring Cloud Gateway. |
| **Consecuencias** | **Positivas:** Aislamiento total de fallos, persistencia desacoplada, escalabilidad independiente por contenedor y punto único de entrada HTTP estándar (puerto 80).<br><br>**Negativas:** Configuración de `nginx.conf` y gestión de redes internas de contenedores. |
| **Revisión** | Revisar cuando la complejidad requiera balanceo de carga dinámico o Service Discovery. |

---

## Pruebas Unitarias y Cobertura (100% Coverage)

El backend cuenta con una suite completa de pruebas unitarias e integración en las capas de `Service` y `Controller` utilizando **JUnit 5**, **Mockito** y **MockMvc**:

```bash
# Ejecutar todas las pruebas unitarias
./gradlew test
```

* **Casos Coberturados:** 
  * Lógica de gestos de *Swipe* (Like / Dislike / Validación de propiedad del ítem).
  * Creación y actualización de estados de *Matches* con otorgamiento de Karma.
  * Creación de reseñas y cálculo del ajuste de reputación (+5 / -5).
  * Filtro de exclusión de artículos propios y ya intercambiados en el Feed.
  * Validaciones de acceso no autorizado (`401`/`403`) y recursos no encontrados (`404`).

---

## Guía de Despliegue y Comandos

### 1. Clonar el repositorio
```bash
git clone https://github.com/pucetec/swipe-share.git
cd swipe-share
```

### 2. Levantar la infraestructura completa con Docker Compose
```bash
docker compose up -d --build
```

### 3. Monitorear logs en tiempo real
```bash
# Logs de todos los contenedores
docker compose logs -f

# Logs del microservicio de SwipeShare
docker compose logs -f swipeshare

# Logs del microservicio de Users
docker compose logs -f users
```

### 4. Escalamiento Horizontal de Microservicios
Para escalar horizontalmente las réplicas del contenedor de SwipeShare detrás del API Gateway de Nginx:
```bash
docker compose up -d --scale swipeshare=3
```

---

##  Autores
**Israel Plazarte**
**Stefano Espinosa**

*Tecnología Superior en Desarrollo de Software - PUCE Quito*
