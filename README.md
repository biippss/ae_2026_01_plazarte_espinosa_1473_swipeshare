## System Architecture

```mermaid
graph TD
    Client[Client / Postman] -->|HTTP Port 9090| NGINX[Nginx Gateway]
    
    subgraph Isolated Docker Network
        NGINX -->|/users| USERS[Users Microservice :8081]
        NGINX -->|/swipeshare| SWIPESHARE[SwipeShare Microservice :8080]
        
        USERS -->|JDBC| USERS_DB[(Users Postgres DB :5432)]
        SWIPESHARE -->|JDBC| SWIPESHARE_DB[(SwipeShare Postgres DB :5432)]
    end

    Cognito[AWS Cognito IDP] <-->|JWT Issuer Validation| USERS
    Cognito <-->|JWT Issuer Validation| SWIPESHARE
## Estándar de Logging

El sistema utiliza un estándar unificado de logging en consola (`stdout`) estructurado en una sola línea por evento, permitiendo trazabilidad completa mediante el identificador de Cognito (`sub`).

### Formato de Salida
`<timestamp> | <LEVEL> | <servicio> | sub=<cognito-sub|anonimo> | <logger> | event=<evento> | msg=<mensaje> | <clave=valor ...>`

### Niveles de Log
* **ERROR:** Fallas que impiden completar una operación (incluye stacktrace).
* **WARN:** Situaciones anómalas o validaciones de negocio rechazadas.
* **INFO:** Eventos relevantes de negocio y ciclo de vida de peticiones HTTP (`http.request`, `http.response`).
* **DEBUG:** Sentencias SQL ejecutadas y binding de parámetros por la aplicación.

### Formato de Eventos
Los eventos de negocio utilizan la nomenclatura `<recurso>.<acción>` en minúsculas (ej: `item.created`, `swipe.like`, `match.status_updated`).