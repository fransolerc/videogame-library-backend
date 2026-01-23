# Video Game Library Backend

Este proyecto es un backend para una aplicación de biblioteca de videojuegos, construido con una **Arquitectura Hexagonal**. Se integra con la API de [IGDB](https://api-docs.igdb.com/) para obtener datos de juegos en tiempo real.

El objetivo es servir como un ejemplo práctico de una arquitectura de software moderna, limpia y escalable, incluyendo un sistema de autenticación robusto.

---

## Tecnologías Principales

- **Java 25**
- **Spring Boot 4**
- **Maven**
- **Spring Data JPA / Hibernate**: Para la persistencia de datos (si se implementa).
- **H2 Database**: Base de datos en memoria para desarrollo.
- **OpenAPI 3 / Swagger UI**: Para la documentación y generación de la API.
- **Arquitectura Hexagonal (Puertos y Adaptadores)**
- **Spring Security**: Autenticación y autorización.
- **JSON Web Tokens (JWT)**: Para la autenticación sin estado.
- **MapStruct**: Para el mapeo de objetos entre capas.
- **JJWT**: Librería para la implementación de JWT.

---

## Cómo Empezar

### Prerrequisitos

- **JDK 25** o superior.
- **Maven 3.8** o superior.
- Un cliente de API como Postman, Insomnia, o simplemente tu navegador.

### Ejecutar la Aplicación

1.  **Configurar la API de IGDB y JWT**:
    - Este proyecto requiere credenciales de la API de IGDB/Twitch.
    - Debes crear o actualizar tu archivo `src/main/resources/application.yml` con el siguiente contenido y tus credenciales:
      ```yaml
      igdb:
        api:
          client-id: "TU_CLIENT_ID_TWITCH"
          client-secret: "TU_CLIENT_SECRET_TWITCH"
      jwt:
        secret: "una-clave-secreta-muy-larga-y-segura-que-deberias-cambiar-en-produccion" # ¡Cambia esto en producción!
        expiration.ms: 86400000 # 24 horas
      ```

2.  **Generar código y compilar**:
    ```sh
    mvn clean install
    ```
    Esto generará las clases de la API a partir de `openapi.yaml` y compilará el proyecto.

3.  **Ejecutar la aplicación**:
    ```sh
    mvn spring-boot:run
    ```

La aplicación se iniciará en `http://localhost:8080`.

---

## Endpoints de la API

La documentación completa de la API está disponible en [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) una vez que la aplicación está en marcha.

### Autenticación JWT

Todos los endpoints protegidos requieren un token JWT en la cabecera `Authorization: Bearer <token>`.

1.  **Registrar Usuario**: `POST /users/register`
    ```json
    {
      "username": "testuser",
      "email": "test@example.com",
      "password": "password123"
    }
    ```
2.  **Iniciar Sesión**: `POST /users/login`
    ```json
    {
      "email": "test@example.com",
      "password": "password123"
    }
    ```
    La respuesta incluirá el token JWT. Cópialo y úsalo en el botón "Authorize" de Swagger UI.

### Endpoints de Juegos

-   **`GET /games/search?name={nombre}`**: Busca videojuegos por nombre.
    -   **Ejemplo**: `http://localhost:8080/games/search?name=Zelda`
-   **`GET /games/{id}`**: Obtiene los detalles completos de un videojuego por su ID de IGDB.
    -   **Ejemplo**: `http://localhost:8080/games/1115` (para "The Legend of Zelda: Ocarina of Time")
-   **`POST /games/filter`**: Realiza una búsqueda avanzada de videojuegos con filtros, ordenación y paginación.



### Endpoints de Plataformas

-   **`GET /platforms`**: Lista todas las plataformas de videojuegos disponibles, incluyendo generación y tipo de plataforma (ej. "CONSOLE", "COMPUTER"), ordenadas alfabéticamente.

### Endpoints de Biblioteca de Usuario

-   **`GET /users/{userId}/games`**: Lista todos los juegos en la biblioteca de un usuario.
-   **`POST /users/{userId}/games`**: Añade un juego a la biblioteca de un usuario o actualiza su estado.

---

## Desarrollo con Frontend (Angular, React, etc.)

### Configuración de CORS

El backend está configurado para aceptar peticiones desde cualquier origen (`*`), lo que facilita el desarrollo con frontends locales.

### Acceso desde Dispositivos Móviles

Si estás desarrollando un frontend (ej. con Angular) y quieres probarlo desde tu móvil en la misma red local, recuerda:

1.  **Arrancar el servidor de frontend para que sea visible en la red**:
    ```sh
    # Ejemplo para Angular
    ng serve --host 0.0.0.0
    ```

2.  **Apuntar la URL de la API del frontend a la IP de tu ordenador**, no a `localhost`.
    -   **Ejemplo (environment.ts en Angular)**:
        ```typescript
        export const environment = {
          apiUrl: 'http://192.168.1.126:8080' // Usa la IP de tu PC
        };
        ```

---

## Arquitectura

El proyecto sigue los principios de la **Arquitectura Hexagonal** para separar el dominio de negocio de los detalles de infraestructura.

-   **`domain`**: Contiene la lógica y las entidades del negocio (el "corazón" de la aplicación). No depende de nada.
-   **`application`**: Orquesta los flujos de trabajo. Contiene los *puertos* (interfaces) y los *casos de uso* (servicios).
-   **`infrastructure`**: Contiene las implementaciones concretas de los puertos (los "adaptadores").
    -   **`adapter.in.web`**: Adaptadores de entrada (Driving Adapters), como los controladores REST y mappers de API.
    -   **`adapter.out.persistence`**: Adaptadores de salida (Driven Adapters) para bases de datos (JPA).
    -   **`adapter.out.provider`**: Adaptadores de salida (Driven Adapters) para proveedores externos (IGDB API).
    -   **`security`**: Configuración de seguridad (JWT).
    -   **`config`**: Configuraciones generales de la aplicación.
