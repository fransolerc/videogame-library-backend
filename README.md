# Video Game Library Backend

Este proyecto es un backend para una aplicación de biblioteca de videojuegos, construido con una **Arquitectura Hexagonal**. Se integra con la API de [IGDB](https://api-docs.igdb.com/) para obtener datos de juegos en tiempo real y utiliza **Apache Kafka** para la publicación de eventos.

El objetivo es servir como un ejemplo práctico de una arquitectura de software moderna, limpia y escalable, incluyendo un sistema de autenticación robusto y comunicación asíncrona.

---

## Tecnologías Principales

- **Java 25**
- **Spring Boot 4**
- **Maven**
- **Spring Data JPA / Hibernate**: Para la persistencia de datos.
- **H2 Database**: Base de datos en memoria para desarrollo.
- **OpenAPI 3 / Swagger UI**: Para la documentación y generación de la API.
- **Arquitectura Hexagonal (Puertos y Adaptadores)**
- **Spring Security**: Autenticación y autorización (JWT).
- **JSON Web Tokens (JWT)**: Para la autenticación sin estado.
- **Apache Kafka**: Para la mensajería asíncrona de eventos.
- **MapStruct**: Para el mapeo de objetos entre capas.
- **JJWT**: Librería para la implementación de JWT.
- **JUnit 5 / Mockito / WireMock**: Para pruebas unitarias y de integración.

---

## Cómo Empezar

### Prerrequisitos

- **JDK 25** o superior.
- **Maven 3.8** o superior.
- **Docker** y **Docker Compose** para ejecutar Kafka (o una instancia de Kafka local).
- Un cliente de API como Postman, Insomnia, o simplemente tu navegador.

### Ejecutar la Aplicación

1.  **Iniciar Kafka**:
    -   En la raíz del proyecto, ejecuta el siguiente comando para iniciar un broker de Kafka y Zookeeper:
        ```sh
        docker-compose up -d
        ```

2.  **Configurar la API de IGDB y JWT**:
    -   Este proyecto requiere credenciales de la API de IGDB/Twitch.
    -   Debes crear o actualizar tu archivo `src/main/resources/application.yml` con el siguiente contenido y tus credenciales:
      ```yaml
      igdb:
        client-id: "TU_CLIENT_ID_TWITCH"
        client-secret: "TU_CLIENT_SECRET_TWITCH"
      jwt:
        secret: "una-clave-secreta-muy-larga-y-segura-que-deberias-cambiar-en-produccion" # ¡Cambia esto en producción!
      ```

3.  **Generar código y compilar**:
    ```sh
    mvn clean install
    ```
    Esto generará las clases de la API a partir de `openapi.yaml` y compilará el proyecto.

4.  **Ejecutar la aplicación**:
    ```sh
    mvn spring-boot:run
    ```

La aplicación se iniciará en `http://localhost:8080`.

---

## Endpoints de la API

La documentación completa de la API está disponible en **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)** una vez que la aplicación está en marcha.

**Importante**: Todos los endpoints están prefijados con `/api/v1`.

### Autenticación

| Método | Endpoint                 | Descripción                                                                                                          |
|:-------|:-------------------------|:---------------------------------------------------------------------------------------------------------------------|
| `POST` | `/api/v1/users/register` | Registra un nuevo usuario. La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número. |
| `POST` | `/api/v1/users/login`    | Inicia sesión y devuelve un token JWT, `userId` y `username`.                                                        |

### Búsqueda de Juegos

| Método | Endpoint               | Descripción                                                         |
|:-------|:-----------------------|:--------------------------------------------------------------------|
| `GET`  | `/api/v1/games/search` | Busca videojuegos por nombre. (Ej: `?name=Zelda`)                   |
| `GET`  | `/api/v1/games/{id}`   | Obtiene los detalles completos de un videojuego por su ID de IGDB.  |
| `POST` | `/api/v1/games/filter` | Realiza una búsqueda avanzada con filtros, ordenación y paginación. |
| `GET`  | `/api/v1/platforms`    | Lista todas las plataformas de videojuegos disponibles.             |

### Biblioteca de Usuario

| Método   | Endpoint                                         | Descripción                                                                                                                          |
|:---------|:-------------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------|
| `GET`    | `/api/v1/users/{userId}/games`                   | Lista todos los juegos en la biblioteca de un usuario.                                                                               |
| `GET`    | `/api/v1/users/{userId}/games/{gameId}`          | Obtiene el estado de un juego específico en la biblioteca del usuario.                                                               |
| `PUT`    | `/api/v1/users/{userId}/games/{gameId}`          | Añade un juego a la biblioteca o actualiza su estado. Si el estado es `NONE` y no es favorito, se elimina.                           |
| `DELETE` | `/api/v1/users/{userId}/games/{gameId}`          | Elimina un juego de la biblioteca de un usuario (borrado completo).                                                                  |
| `POST`   | `/api/v1/users/{userId}/games/{gameId}/favorite` | Marca un juego como favorito. Si no está en la biblioteca, lo añade con estado `NONE`. Devuelve el recurso actualizado.              |
| `DELETE` | `/api/v1/users/{userId}/games/{gameId}/favorite` | Quita un juego de favoritos. Si el estado del juego es `NONE`, se elimina por completo de la biblioteca.                             |
| `GET`    | `/api/v1/users/{userId}/favorites`               | Lista todos los juegos favoritos de un usuario (paginado).                                                                           |
| `GET`    | `/api/v1/users/{userId}/favorites/analysis`      | **(Experimental)** Obtiene un análisis de los juegos favoritos de un usuario, generado por un servicio de IA (actualmente simulado). |

---

## Pruebas

El proyecto tiene una alta cobertura de pruebas, incluyendo:

-   **Pruebas Unitarias**: Para los servicios de aplicación y lógica de dominio.
-   **Pruebas de Integración**: Para los controladores REST, utilizando **WireMock** para simular la API externa de IGDB.

Para ejecutar todas las pruebas, usa el siguiente comando de Maven:

```sh
mvn clean verify
```

---

## Arquitectura

El proyecto sigue los principios de la **Arquitectura Hexagonal** para separar el dominio de negocio de los detalles de infraestructura.

-   **`domain`**: Contiene la lógica y las entidades del negocio (el "corazón" de la aplicación). No depende de nada.
-   **`application`**: Orquesta los flujos de trabajo. Contiene los *puertos* (interfaces `UseCase`) y los *casos de uso* (servicios que implementan esos `UseCase`).
-   **`infrastructure`**: Contiene las implementaciones concretas de los puertos (los "adaptadores").
    -   **`adapter.in.web`**: Adaptadores de entrada (Driving Adapters), como los controladores REST.
    -   **`adapter.out.persistence`**: Adaptadores de salida (Driven Adapters) para bases de datos (JPA).
    -   **`adapter.out.provider`**: Adaptadores de salida para proveedores externos (IGDB API).
    -   **`adapter.out.kafka`**: Adaptadores de salida para publicar eventos en Kafka.
    -   **`security`**: Configuración de seguridad (JWT).
    -   **`config`**: Configuraciones generales de la aplicación.
