# PROYECTO INTEGRADOR
## Especificación de Requerimientos de Software (SRS) – SwipeShare

**Autores:** Stéfano Espinosa, Israel Plazarte  
**Paralelo:** 2  
**Proyecto:** SwipeShare  
**Período:** 2025-26  

---

## Índice
1. Introducción
   - 1.1 Propósito
   - 1.2 Alcance
   - 1.3 Definiciones y Abreviaturas
2. Descripción General
   - 2.1 Perspectiva del Producto
   - 2.2 Perfiles de Usuario
3. Requerimientos Específicos
   - 3.1 Requerimientos Funcionales (RF)
   - 3.2 Requerimientos No Funcionales (RNF)
4. Tabla de casos de uso (CU)
5. Historias de usuario (HU)

---

## 1. Introducción

### 1.1 Propósito
El propósito de este documento es especificar los requerimientos funcionales, no funcionales, casos de uso e historias de usuario para la plataforma móvil y distribuida SwipeShare, dedicada al trueque e intercambio de bienes entre usuarios. Servirá como marco de referencia para las fases de desarrollo móvil en Android Studio, microservicios en Spring Boot, pruebas unitarias y despliegue en la nube (AWS EC2).

### 1.2 Alcance
El sistema SwipeShare comprende:
* **Backend Distribuido:** Microservicios en Spring Boot (Kotlin), autenticación federada con AWS Cognito, persistencia en PostgreSQL y enrutamiento centralizado mediante Nginx API Gateway.
* **Cliente Móvil Nativo:** Aplicación Android en Kotlin bajo el patrón de arquitectura MVVM (`models`, `services`, `viewmodels`, `ui`), utilizando Retrofit para el consumo de la API REST.

### 1.3 Definiciones y Abreviaturas

| Término | Definición |
| :--- | :--- |
| **API Gateway** | Punto único de entrada (Nginx) que enruta las peticiones HTTP/REST a los microservicios correspondientes. |
| **JWT** | JSON Web Token; estándar para la transmisión segura de identidades y claims entre cliente y servidor. |
| **MVVM** | Model-View-ViewModel; patrón de arquitectura de software utilizado en el cliente Android. |
| **Swipe** | Acción de deslizamiento/interacción (LIKE o DISLIKE) que expresa interés sobre un artículo expuesto. |
| **Match** | Coincidencia mutua de interés entre dos usuarios que habilita la negociación del intercambio. |

---

## 2. Descripción General

### 2.1 Perspectiva del Producto
SwipeShare opera como un ecosistema multicapa en la nube donde la aplicación móvil Android interactúa con la infraestructura de backend contenerizada mediante Docker Compose y desplegada sobre una instancia de AWS EC2 (Ubuntu Linux). El tráfico externo ingresa a través del puerto HTTP estándar (puerto 80) administrado por Nginx como API Gateway, el cual enruta las peticiones de forma segura e interna hacia los microservicios y sus respectivas bases de datos aisladas en PostgreSQL.

                               +-----------------------+
                               | App Móvil Android     |
                               | (Jetpack Compose)     |
                               +-----------+-----------+
                                           |
                                 HTTP / Retrofit + JWT
                                           |
                                           v
                               +-----------------------+
                               | API Gateway Nginx     |
                               | (Puerto 80 en EC2)    |
                               +----+-------------+----+
                                    |             |
                         /api/users |             | /api/swipes, /api/matches,
                                    v             v /api/items, /api/reviews
       +------------------------------+         +------------------------------+
       | Microservicio Users          |         | Microservicio SwipeShare     |
       | (Puerto 8081 - Spring Boot)  |         | (Puerto 8080 - Spring Boot)  |
       +--------------+---------------+         +--------------+---------------+
                      |                                        |
                      v                                        v
               +--------------+                         +--------------+
               | Users DB     |                         | SwipeShare DB|
               | (PostgreSQL) |                         | (PostgreSQL) |
               +--------------+                         +--------------+
### 2.2 Perfiles de Usuario
* **Usuario Registrado:** Persona autenticada que puede publicar artículos, explorar el feed de trueque, realizar swipes, aceptar/rechazar matches y calificar a otros usuarios.
* **Sistema / API REST:** Componente backend encargado de validar la autenticidad de los tokens JWT emitidos por AWS Cognito y de procesar sincrónicamente las reglas de negocio para registrar swipes y generar coincidencias.

---

## 3. Requerimientos Específicos

### 3.1 Requerimientos Funcionales (RF)
* **RF-01 (Autenticación y Seguridad JWT):** Autenticación delegada a AWS Cognito con validación de firma JWT en cada petición protegida para identificar al usuario en el sistema.
* **RF-02 (Gestión de Perfil Personal):** Creación, consulta, actualización y eliminación del perfil de usuario autenticado, registrando datos personales (nombre, correo electrónico, biografía y teléfono) con validación de campos obligatorios.
* **RF-03 (Consulta de Perfil Público):** Consulta de la información pública de otros usuarios mediante su identificador de Cognito, incluyendo su nombre, biografía y saldo de karma.
* **RF-04 (Control y Balance de Karma):** Incremento o decremento seguro del saldo de karma del usuario, aplicando la regla estricta de negocio que impide que el balance acumulado sea inferior a cero.
* **RF-05 (Gestión de Inventario Personal):** Publicación de nuevos artículos, consulta del inventario propio y eliminación de ítems con validación estricta de propiedad.
* **RF-06 (Catálogo General Filtrado):** Consulta del catálogo general de intercambio (feed) excluyendo automáticamente los artículos pertenecientes al usuario autenticado.
* **RF-07 (Procesamiento de Swipes y Ofertas):** Registro de interacciones LIKE y DISLIKE. En interacciones positivas, el sistema valida la existencia de productos propios y asocia un artículo ofrecido como propuesta de trueque.
* **RF-08 (Creación y Estado de Matches):** Generación de coincidencias (matches) tras una interacción positiva o solicitud directa, consulta de trueques en los que participa el usuario y actualización de estado a APPROVED o REJECTED.
* **RF-09 (Reseñas e Integración de Karma):** Registro de evaluaciones de 1 a 5 estrellas hacia otros usuarios, notificando sincrónicamente al microservicio de usuarios para ajustar el saldo de karma (+5, -5 o 0).
* **RF-10 (Métricas y Estadísticas Públicas):** Consulta de indicadores globales del sistema (`totalItems` y `totalMatches`) para paneles de control.

### 3.2 Requerimientos No Funcionales (RNF)
* **RNF-01 (Seguridad y Autorización API):** Todas las rutas protegidas de los microservicios deben exigir un token Bearer JWT válido en el encabezado Authorization, validando la firma e integridad mediante la integración con AWS Cognito.
* **RNF-02 (Arquitectura Frontend MVVM):** La aplicación móvil debe desacoplar strictly la interfaz de usuario, la lógica de presentación y la capa de datos/red utilizando el patrón de arquitectura MVVM (`models`, `services`, `viewmodels`, `ui`) y Retrofit.
* **RNF-03 (Aislamiento por Contenedores):** Los microservicios y bases de datos PostgreSQL deben ejecutarse en contenedores aislados coordinados con Docker Compose, utilizando un proxy inverso Nginx en el puerto 9090 como punto único de entrada.
* **RNF-04 (Rendimiento del Backend):** El tiempo promedio de respuesta de los endpoints de la API no debe superar los 3 segundos bajo condiciones normales de operación.
* **RNF-05 (Seguridad en Almacenamiento Móvil):** El token JWT recibido durante la autenticación debe guardarse de forma segura en el dispositivo (`SharedPreferences` / `EncryptedSharedPreferences`) y destruirse automáticamente al cerrar la sesión del usuario.
* **RNF-06 (Usabilidad y Diseño UX/UI):** La interfaz móvil debe diseñarse bajo las guías de Material Design, estructurada con Jetpack Compose y componentes de Material 3, usando contenedores adaptables (`Box`, `Column`, `Row` y `Modifier`) a diferentes tamaños y resoluciones de pantalla.
* **RNF-07 (Manejo de Concurrencia y Hilos):** Las llamadas a la red mediante Retrofit y las operaciones pesadas en segundo plano deben ejecutarse de forma asíncrona mediante Kotlin Coroutines, garantizando que el hilo principal (UI Thread) no sufra congelamientos.
* **RNF-08 (Compatibilidad de Plataforma):** El cliente Android debe ser totalmente ejecutable y compatible con dispositivos móviles que cuenten con sistema operativo Android 8.0 (API Nivel 26) o superior.

---

## 4. Tabla de casos de uso (CU)

| Código | Caso de Uso | Actor / Capa | Requerimientos | Descripción Técnica |
| :--- | :--- | :--- | :--- | :--- |
| **CU-01** | Validar Autenticación y Token JWT | Backend - OAuth2 Resource Server | RF-01, RNF-01 | Protege los endpoints verificando la firma e integridad del token JWT emitido por AWS Cognito. |
| **CU-02** | API CRUD de Perfil de Usuario | Backend - Microservicio Users | RF-02 | Procesa la lectura, actualización (PUT) y borrado (DELETE) de la cuenta del usuario. |
| **CU-03** | Control y Balance de Karma | Backend - Microservicio Users | RF-03, RF-04 | Aplica la regla de negocio para aumentar/restar karma asegurando que el saldo acumulado no sea menor a 0. |
| **CU-04** | API de Publicación y Borrado de Ítems | Backend - Microservicio SwipeShare | RF-05 | Registra productos vinculados al `cognitoId` y valida la propiedad del ítem antes de permitir su eliminación. |
| **CU-05** | Filtrado del Catálogo General (Feed) | Backend - Microservicio SwipeShare | RF-06 | Retorna los artículos expuestos ejecutando `findByOwnerIdNot` para excluir las publicaciones del usuario. |
| **CU-06** | Procesar Swipes y Crear Matches | Backend - Microservicio SwipeShare | RF-07, RF-08 | Registra LIKE/DISLIKE, valida inventario propio para ofertas y guarda la entidad Match en estado PENDING. |
| **CU-07** | Actualizar Estado de Match | Backend - Microservicio SwipeShare | RF-08 | Cambia el estado de la negociación a APPROVED o REJECTED verificando la participación del usuario. |
| **CU-08** | Registrar Reseña y Modificar Karma | Backend - Microservicio SwipeShare | RF-04, RF-09 | Guarda la valoración e invoca de forma sincrónica vía `RestTemplate` al servicio de usuarios para ajustar el karma. |
| **CU-09** | Exponer Estadísticas Globales | Backend - Microservicio Public | RF-10 | Consulta y devuelve los contadores de `totalItems` y `totalMatches` registrados en la base de datos. |
| **CU-10** | Iniciar Sesión y Capturar Token | Frontend Móvil - AuthViewModel | RF-01, RNF-01 | Formulario de login en Android; almacena el JWT devuelto e inyecta la cabecera Authorization vía OkHttp. |
| **CU-11** | Gestionar Datos Personales | Frontend Móvil - ProfileViewModel | RF-02, RNF-02 | Despliega la pantalla de perfil propio (`ProfileScreen.kt`) permitiendo editar nombre, teléfono y biografía. Nota: `ApiService.deleteAccount()` y el endpoint DELETE `/api/users/me` ya existen, pero `ProfileViewModel` aún no expone una acción para invocarlos desde la interfaz. |
| **CU-12** | Consultar Perfil Público y Karma | Frontend Móvil - ProfileViewModel | RF-03, RF-04 | Estado actual: `MatchesViewModel` consume GET `/api/users/{cognitoId}` (`ProfileResponse`) únicamente para obtener el teléfono del otro usuario y mostrarlo en `MatchesScreen` tras un match APPROVED. Aún no existe una pantalla de perfil público que muestre biografía o una insignia visual del saldo de karma de terceros. |
| **CU-13** | Formulario de Registro de Producto | Frontend Móvil - ItemViewModel | RF-05, RNF-02 | Valida los campos obligatorios en la pantalla de creación e invoca el endpoint de publicación. |
| **CU-14** | Visualizar Inventario y Borrar Ítems | Frontend Móvil - ItemViewModel | RF-05, RNF-02 | Muestra la lista de productos propios mediante un `LazyColumn` (Jetpack Compose). Nota: el backend expone DELETE `/api/items/{id}` con validación de propiedad y el método existe en `ApiService`, pero la pantalla actual (`MyProductsScreen`) aún no incorpora la acción de eliminar en la interfaz. |
| **CU-15** | Explorar Feed de Tarjetas | Frontend Móvil - FeedViewModel | RF-06, RNF-02 | Muestra las publicaciones disponibles de la comunidad en tarjetas interactivas dentro del feed. |
| **CU-16** | Registrar Swipe y Modal de Oferta | Frontend Móvil - FeedViewModel | RF-07, RNF-02 | Captura el gesto LIKE y envía la propuesta usando automáticamente el primer producto publicado por el usuario (`myFirstItemId`) como ítem ofrecido. Aún no existe un diálogo modal para que el usuario elija cuál de sus artículos ofrecer. |
| **CU-17** | Gestionar Matches Aceptados/Rechazados | Frontend Móvil - MatchViewModel | RF-08, RNF-02 | Muestra la pantalla de coincidencias con botones de acción ("Aceptar" / "Rechazar") sobre cada propuesta. |
| **CU-18** | Formulario de Reseña y RatingBar | Frontend Móvil - ProfileViewModel | RF-09, RNF-02 | No implementado en frontend: el backend expone POST `/api/reviews` (`ReviewController`/`ReviewService`) y calcula el ajuste de karma correctamente, pero `ApiService.kt` no define ningún método hacia `/api/reviews` y no existe una pantalla de calificación (RatingBar) en la app móvil. |
| **CU-19** | Visualizar Dashboard de Estadísticas | Frontend Móvil - AuthViewModel | RF-10 | No implementado en frontend: `ApiService.getGlobalStats()` (GET `/api/public/stats`) está definido, pero ninguna pantalla o ViewModel lo invoca actualmente; no existe una vista de indicadores globales previa al catálogo. |

---

## 5. Historias de usuario (HU)

### Épica 1: Autenticación y Estadísticas

#### HU-01: Validar Autenticación JWT en Servidor de Recursos (Backend)
* **ID:** HU-01
* **Épica:** Autenticación y Seguridad
* **Título:** Configuración de Servidor de Recursos OAuth2
* **Historia:** Como microservicio de backend, quiero validar la firma e integridad de los tokens JWT emitidos por AWS Cognito en cada petición para proteger los endpoints.
* **Prioridad:** Alta
* **RF / RNF:** RF-01, RNF-01
* **Casos de Uso:** CU-01
* **Rama Git:** `feature/auth-backend-security`
* **Criterios de Aceptación:**
  1. Configuración de Spring Security OAuth2 Resource Server.
  2. Bloqueo automático de peticiones no autorizadas con código HTTP 401/403.

#### HU-02: Pantalla de Login e Interceptor de Tokens (Frontend)
* **ID:** HU-02
* **Épica:** Autenticación y Seguridad
* **Título:** Interfaz de Autenticación e Inyección de JWT
* **Historia:** Como usuario móvil, quiero ingresar mi correo y contraseña para iniciar sesión y que el sistema adjunte mi token a todas las solicitudes.
* **Prioridad:** Alta
* **RF / RNF:** RF-01, RNF-02
* **Casos de Uso:** CU-10
* **Rama Git:** `feature/auth-mobile-login`
* **Criterios de Aceptación:**
  1. Pantalla `LoginScreen.kt` (Jetpack Compose) captura correo y contraseña; actualmente no aplica validación de formato en el cliente antes de invocar `AuthViewModel.login()` (queda como mejora pendiente).
  2. `AuthViewModel` almacena el JWT y `AuthInterceptor` lo adjunta automáticamente a las peticiones de red.

---

### Épica 2: Gestión de Perfiles y Reputación

#### HU-03: API CRUD de Usuarios y Regla de Karma (Backend)
* **ID:** HU-03
* **Épica:** Gestión de Perfiles
* **Título:** Servicio de Perfil de Usuario y Lógica de Karma
* **Historia:** Como API de usuarios, quiero administrar los perfiles personales y aplicar la regla de negocio que impide que el karma descienda de cero.
* **Prioridad:** Alta
* **RF / RNF:** RF-02, RF-03, RF-04
* **Casos de Uso:** CU-02, CU-03
* **Rama Git:** `feature/users-backend-service`
* **Criterios de Aceptación:**
  1. Endpoints para obtener, actualizar (PUT) y eliminar (DELETE) el perfil.
  2. Método `addKarma` ajusta el saldo garantizando que el mínimo acumulado sea 0.

#### HU-04: Pantallas de Perfil Personal, Público y Karma (Frontend)
* **ID:** HU-04
* **Épica:** Gestión de Perfiles
* **Título:** Vistas de Perfil Personal, Edición y Reputación
* **Historia:** Como usuario móvil, quiero visualizar mi perfil, editar mis datos y consultar la reputación (Karma) de otros usuarios.
* **Prioridad:** Alta
* **RF / RNF:** RF-02, RF-03, RNF-02
* **Casos de Uso:** CU-11, CU-12
* **Rama Git:** `feature/users-mobile-profile`
* **Criterios de Aceptación:**
  1. `ProfileViewModel` gestiona los estados visuales para la edición del perfil (nombre, teléfono, biografía); la eliminación de cuenta desde la UI queda pendiente de conectar al endpoint ya existente.
  2. Muestra el propio saldo de karma en `ProfileScreen` (`Karma: ${profile?.karma}`); no existe todavía una vista de perfil público con el karma de terceros — solo se expone el teléfono tras un match aprobado. Nota: existe un desajuste de contrato entre el DTO móvil (campo `"karma"`) y la respuesta real del backend (campo `"karmaBalance"`), por lo que el valor mostrado actualmente no refleja el saldo real y debe corregirse.

---

### Épica 3: Gestión de Inventario y Artículos

#### HU-05: API de Creación, Consulta e Inventario (Backend)
* **ID:** HU-05
* **Épica:** Gestión de Inventario
* **Título:** Endpoints de Publicación, Lectura y Borrado de Ítems
* **Historia:** Como API de inventario, quiero permitir publicar productos vinculados al usuario logueado y borrarlos con validación de propiedad.
* **Prioridad:** Alta
* **RF / RNF:** RF-05
* **Casos de Uso:** CU-04
* **Rama Git:** `feature/items-backend-service`
* **Criterios de Aceptación:**
  1. `createItem` asocia el producto al `cognitoId`.
  2. `deleteItem` verifica que el `ownerId` coincida con el usuario autenticado antes de removerlo.

#### HU-06: Formulario de Publicación y Mis Ítems (Frontend)
* **ID:** HU-06
* **Épica:** Gestión de Inventario
* **Título:** Interfaz de Creación y Listado de Inventario Personal
* **Historia:** Como usuario móvil, quiero llenar un formulario para publicar un artículo y ver mis productos en una lista con opción de eliminación.
* **Prioridad:** Alta
* **RF / RNF:** RF-05, RNF-02
* **Casos de Uso:** CU-13, CU-14
* **Rama Git:** `feature/items-mobile-management`
* **Criterios de Aceptación:**
  1. La pantalla `CreateItemScreen.kt` (Jetpack Compose), respaldada por `CreateItemViewModel`, valida que título y categoría no estén vacíos.
  2. `MyProductsScreen.kt` (Jetpack Compose) despliega tus productos en un `LazyColumn`. El botón de eliminar aún no está conectado en esta pantalla, aunque el endpoint y el método `deleteItem()` del `ApiService` ya existen.

---

### Épica 4: Exploración y Catálogo (Feed)

#### HU-07: API de Feed Filtrado por Propietario (Backend)
* **ID:** HU-07
* **Épica:** Exploración y Catálogo
* **Título:** Consulta de Catálogo General con Exclusión
* **Historia:** Como API de catálogo, quiero filtrar la lista general para omitir los artículos creados por el usuario autenticado.
* **Prioridad:** Alta
* **RF / RNF:** RF-06
* **Casos de Uso:** CU-05
* **Rama Git:** `feature/feed-backend-filter`
* **Criterios de Aceptación:**
  1. Ejecución de `getAllItemsExceptUser` llamando a `findByOwnerIdNot(cognitoId)`.

#### HU-08: Vista del Feed General y Tarjetas de Trueque (Frontend)
* **ID:** HU-08
* **Épica:** Exploración y Catálogo
* **Título:** Catálogo Principal de Publicaciones
* **Historia:** Como usuario móvil, quiero ver las publicaciones de otros usuarios en el feed para encontrar artículos de mi interés.
* **Prioridad:** Alta
* **RF / RNF:** RF-06, RNF-02
* **Casos de Uso:** CU-15
* **Rama Git:** `feature/feed-mobile-view`
* **Criterios de Aceptación:**
  1. `FeedViewModel` solicita el catálogo y renderiza las tarjetas en pantalla.
  2. Comprueba que las publicaciones propias no aparezcan visibles en la lista.

---

### Épica 5: Interacciones y Coincidencias (Swipes & Matches)

#### HU-09: API de Swipes, Ofertas y Estado de Matches (Backend)
* **ID:** HU-09
* **Épica:** Interacciones y Coincidencias
* **Título:** Procesamiento de Swipes y Cambio de Estado de Matches
* **Historia:** Como API de trueques, quiero procesar interacciones, verificar la existencia de ofertas de productos e ingresar/actualizar matches.
* **Prioridad:** Alta
* **RF / RNF:** RF-07, RF-08
* **Casos de Uso:** CU-06, CU-07
* **Rama Git:** `feature/matches-backend-logic`
* **Criterios de Aceptación:**
  1. `processSwipe` obliga a tener al menos un producto propio para enviar un LIKE.
  2. `updateMatchStatus` permite cambiar la negociación a APPROVED o REJECTED.

#### HU-10: Interfaz de Swipes, Modal de Oferta y Matches (Frontend)
* **ID:** HU-10
* **Épica:** Interacciones y Coincidencias
* **Título:** Interfaz de Interacciones y Pantalla de Matches
* **Historia:** Como usuario móvil, quiero presionar me gusta u ofrecer un artículo, ver mis coincidencias y poder aceptarlas o rechazarlas.
* **Prioridad:** Alta
* **RF / RNF:** RF-07, RF-08, RNF-02
* **Casos de Uso:** CU-16, CU-17
* **Rama Git:** `feature/matches-mobile-flow`
* **Criterios de Aceptación:**
  1. Al dar LIKE, `FeedViewModel.swipe()` envía automáticamente el primer artículo del inventario del usuario (`myFirstItemId`) como oferta; la selección manual mediante un cuadro modal queda pendiente como mejora de UX.
  2. `MatchViewModel` lista tus coincidencias y cambia su estado con botones de acción ("Aceptar"/"Rechazar").

---

### Épica 6: Reputación y Reseñas

#### HU-11: API de Reseñas e Integración Sincrónica de Karma (Backend)
* **ID:** HU-11
* **Épica:** Reputación y Reseñas
* **Título:** Creación de Reseñas y Notificación HTTP de Karma
* **Historia:** Como API de reseñas, quiero guardar evaluaciones e invocar sincrónicamente vía `RestTemplate` al microservicio de usuarios para actualizar el karma.
* **Prioridad:** Media
* **RF / RNF:** RF-04, RF-09
* **Casos de Uso:** CU-08
* **Rama Git:** `feature/reviews-backend-integration`
* **Criterios de Aceptación:**
  1. `createReview` calcula +5 (4-5 estrellas) o -5 (1-2 estrellas).
  2. Realiza la llamada HTTP PUT al puerto del servicio de usuarios sin fallar si este no responde.

#### HU-12: Formulario de Calificación (RatingBar) y Comentarios (Frontend)
* **ID:** HU-12
* **Épica:** Reputación y Reseñas
* **Título:** Componente de Calificación e Historial de Reseñas
* **Historia:** Como usuario móvil, quiero seleccionar estrellas y escribir un comentario sobre un usuario tras completar un trueque.
* **Prioridad:** Media
* **RF / RNF:** RF-09, RNF-02
* **Casos de Uso:** CU-18
* **Rama Git:** `feature/reviews-mobile-ui`
* **Criterios de Aceptación:**
  1. Pendiente: aún no existe una pantalla emergente con RatingBar (1 a 5 estrellas) ni caja de comentario en la app móvil.
  2. Pendiente: falta agregar en `ApiService.kt` un método hacia POST `/api/reviews` y una vista de historial de reseñas en el perfil del usuario evaluado (el backend ya soporta ambas cosas mediante `ReviewController` y `ReviewService`).

---

### Épica 7: Métricas e Indicadores

#### HU-13: API de Estadísticas Públicas (Backend)
* **ID:** HU-13
* **Épica:** Métricas e Indicadores
* **Título:** Endpoint de Métricas Globales del Sistema
* **Historia:** Como API pública, quiero exponer un endpoint que devuelva el total de productos e intercambios para mostrar indicadores en el sistema.
* **Prioridad:** Baja
* **RF / RNF:** RF-10
* **Casos de Uso:** CU-09
* **Rama Git:** `feature/stats-backend-public`
* **Criterios de Aceptación:**
  1. `PublicService` calcula el conteo directo mediante `count()` de repositorios y retorna el DTO `StatsResponse`.

#### HU-14: Dashboard de Métricas Iniciales (Frontend)
* **ID:** HU-14
* **Épica:** Métricas e Indicadores
* **Título:** Vista de Contadores e Indicadores Globales
* **Historia:** Como usuario móvil, quiero ver el total de productos y trueques en la pantalla inicial para conocer la actividad de la comunidad.
* **Prioridad:** Baja
* **RF / RNF:** RF-10, RNF-02
* **Casos de Uso:** CU-19
* **Rama Git:** `feature/stats-mobile-dashboard`
* **Criterios de Aceptación:**
  1. Pendiente: `AuthViewModel` aún no consume GET `/api/public/stats`; el método `getGlobalStats()` existe en `ApiService` pero no está conectado a ningún ViewModel.
  2. Pendiente: no existen todavía tarjetas informativas que desplieguen `totalItems` y `totalMatches` en ninguna pantalla de la app.

---

### Épica 8: Detalle y Eliminación de Ítems

#### HU-15: API y Pantalla de Detalle de Producto (Backend + Frontend)
* **ID:** HU-15
* **Épica:** Inventario y Detalle
* **Título:** Consulta Detallada de un Artículo Específico
* **Historia:** Como usuario, quiero seleccionar un producto específico del catálogo para consultar sus características completas en pantalla.
* **Prioridad:** Media
* **RF / RNF:** RF-05, RNF-02
* **Casos de Uso:** CU-04, CU-14
* **Rama Git:** `feature/item-detail-view`
* **Criterios de Aceptación:**
  1. Backend expone GET `/api/items/{id}` mapeando `ItemResponse`.
  2. Pendiente en frontend: el endpoint GET `/api/items/{id}` existe en el backend, pero `ApiService.kt` aún no define el método correspondiente y no hay una pantalla de detalle de producto implementada en la app móvil; actualmente el usuario solo ve la información del ítem resumida en la tarjeta del feed o de "Mis Productos".

#### HU-16: Borrado de Ítems con Validación de Seguridad (Backend + Frontend)
* **ID:** HU-16
* **Épica:** Inventario y Detalle
* **Título:** Eliminación Segura de Artículos de Inventario
* **Historia:** Como usuario, quiero presionar eliminar sobre uno de mis artículos para que se retire del sistema verificando que me pertenezca.
* **Prioridad:** Alta
* **RF / RNF:** RF-05, RNF-01
* **Casos de Uso:** CU-04, CU-14
* **Rama Git:** `feature/item-deletion-security`
* **Criterios de Aceptación:**
  1. Backend retorna un error de autorización si el `ownerId` no coincide con el token.
  2. Pendiente en frontend: la app móvil aún no dispara la llamada a `deleteItem()` desde `MyProductsScreen.kt`, por lo que el ítem no se remueve todavía del `LazyColumn` tras el borrado; queda como tarea de conexión de UI.
