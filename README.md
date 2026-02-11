# Video Game Library Backend

Este proyecto es un backend para una aplicación de biblioteca de videojuegos, construido con una **Arquitectura Hexagonal**. Se integra con la API de [IGDB](https://api-docs.igdb.com/) para obtener datos de juegos en tiempo real y utiliza **Apache Kafka** para la publicación de eventos.

El objetivo es servir como un ejemplo práctico de una arquitectura de software moderna, limpia y escalable, incluyendo un sistema de autenticación robusto y comunicación asíncrona.

---

## Tecnologías Principales

- **Java 25**
- **Spring Boot 4**
- **Maven**
- **Spring Data JPA / Hibernate**: Para la persistencia de datos.
- **H2 Database**: Base de datos en memoria para desarrollo y pruebas.
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

- **Docker Desktop** (o Docker Engine) instalado y en ejecución.
- Un cliente de API como Postman, Insomnia, o simplemente tu navegador.

### Configuración Obligatoria

Antes de ejecutar la aplicación, debes proporcionar tus credenciales de la API de IGDB/Twitch y una clave secreta para JWT.

1.  Localiza el fichero `src/main/resources/application.yml`.
2.  Asegúrate de que las siguientes propiedades están configuradas con tus valores:
    ```yaml
    igdb:
      client-id: "TU_CLIENT_ID_TWITCH"
      client-secret: "TU_CLIENT_SECRET_TWITCH"
    jwt:
      secret: "una-clave-secreta-muy-larga-y-segura-que-deberias-cambiar-en-produccion" # ¡Cambia esto en producción!
    ```

### Ejecutar con Docker

1. **Construir la imagen de Docker**:
    -   Abre una terminal en la raíz del proyecto y ejecuta:
        ```sh
        docker build -t videogame-library-backend .
        ```

2. **Iniciar Kafka y la aplicación**:
    -   Si tienes un `docker-compose.yml` para Kafka, asegúrate de iniciarlo primero (`docker-compose up -d`).
    -   Luego, ejecuta la aplicación Docker:
        ```sh
        docker run -p 8080:8080 videogame-library-backend
        ```
    -   La aplicación se iniciará en `http://localhost:8080`.

3. **Prerrequisitos Adicionales**:
    -   **JDK 25** o superior.
    -   **Maven 3.8** o superior.
    -   **Docker Compose** para ejecutar Kafka (o una instancia de Kafka local).

4. **Iniciar Kafka**:
    -   En la raíz del proyecto, ejecuta el siguiente comando para iniciar un broker de Kafka y Zookeeper:
        ```sh
        docker-compose up -d
        ```

5. **Generar código y compilar**:
    ```sh
    mvn clean install
    ```

6. **Ejecutar la aplicación**:
    ```sh
    mvn spring-boot:run
    ```

La aplicación se iniciará en `http://localhost:8080`.

---

## Endpoints de la API

La documentación completa de la API está disponible en **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)** una vez que la aplicación está en marcha.

### Autenticación y Estado

| Método | Endpoint          | Descripción                                                                                                          |
|:-------|:------------------|:---------------------------------------------------------------------------------------------------------------------|
| `POST` | `/users/register` | Registra un nuevo usuario. La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número. |
| `POST` | `/users/login`    | Inicia sesión y devuelve un token JWT, `userId` y `username`.                                                        |
| `GET`  | `/health`         | Comprueba el estado de la aplicación. Devuelve `{"status": "UP"}` si todo está correcto.                             |

### Búsqueda de Juegos

| Método | Endpoint        | Descripción                                                                           |
|:-------|:----------------|:--------------------------------------------------------------------------------------|
| `GET`  | `/games/search` | Busca videojuegos por nombre. (Ej: `?name=Zelda`)                                     |
| `GET`  | `/games/{id}`   | Obtiene los detalles completos de un videojuego por su ID de IGDB.                    |
| `POST` | `/games/filter` | Realiza una búsqueda avanzada con filtros, ordenación y paginación.                   |
| `POST` | `/games/batch`  | Obtiene los detalles completos de múltiples videojuegos a partir de una lista de IDs. |

### Plataformas

| Método | Endpoint     | Descripción                                             |
|:-------|:-------------|:--------------------------------------------------------|
| `GET`  | `/platforms` | Lista todas las plataformas de videojuegos disponibles. |

### Biblioteca de Usuario

| Método   | Endpoint                                   | Descripción                                                                                                                          |
|:---------|:-------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------|
| `GET`    | `/users/{userId}/games`                    | Lista todos los juegos en la biblioteca de un usuario.                                                                               |
| `GET`    | `/users/{userId}/games/{gameId}`           | Obtiene el estado de un juego específico en la biblioteca del usuario.                                                               |
| `PUT`    | `/users/{userId}/games/{gameId}`           | Añade un juego a la biblioteca o actualiza su estado. Si el estado es `NONE` y no es favorito, se elimina.                           |
| `DELETE` | `/users/{userId}/games/{gameId}`           | Elimina un juego de la biblioteca de un usuario (borrado completo).                                                                  |
| `POST`   | `/users/{userId}/games/{gameId}/favorite`  | Marca un juego como favorito. Si no está en la biblioteca, lo añade con estado `NONE`. Devuelve el recurso actualizado.              |
| `DELETE` | `/users/{userId}/games/{gameId}/favorite`  | Quita un juego de favoritos. Si el estado del juego es `NONE`, se elimina por completo de la biblioteca.                             |
| `GET`    | `/users/{userId}/favorites`                | Lista todos los juegos favoritos de un usuario (paginado).                                                                           |

---

## Pruebas

El proyecto tiene una alta cobertura de pruebas, incluyendo:

-   **Pruebas Unitarias**: Para los servicios de aplicación y lógica de dominio.
-   **Pruebas de Integración**: Para los controladores REST y la interacción con la base de datos.

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
