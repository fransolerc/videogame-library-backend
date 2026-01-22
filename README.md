# Video Game Library Backend

Este proyecto es un backend para una aplicación de biblioteca de videojuegos, construido con una **Arquitectura Hexagonal**. Se integra con la API de [IGDB](https://api-docs.igdb.com/) para obtener datos de juegos en tiempo real.

El objetivo es servir como un ejemplo práctico de una arquitectura de software moderna, limpia y escalable.

---

## Tecnologías Principales

- **Java 25**: Utilizando características modernas como **Hilos Virtuales** para mejorar el rendimiento de las operaciones de red.
- **Spring Boot 3**
- **Maven**
- **Spring Data JPA / Hibernate**: Para la persistencia de datos (si se implementa).
- **H2 Database**: Base de datos en memoria para desarrollo.
- **OpenAPI 3 / Swagger UI**: Para la documentación y generación de la API.
- **Arquitectura Hexagonal (Puertos y Adaptadores)**

---

## Cómo Empezar

### Prerrequisitos

- **JDK 25** o superior.
- **Maven 3.8** o superior.
- Un cliente de API como Postman, Insomnia, o simplemente tu navegador.

### Ejecutar la Aplicación

1.  **Configurar la API de IGDB**:
    - Este proyecto requiere credenciales de la API de IGDB/Twitch.
    - Debes crear un archivo `src/main/resources/application.yml` con el siguiente contenido y tus credenciales:
      ```yaml
      igdb:
        api:
          client-id: "TU_CLIENT_ID"
          client-secret: "TU_CLIENT_SECRET"
      ```

2.  **Ejecutar la aplicación**:
    ```sh
    mvn spring-boot:run
    ```

La aplicación se iniciará en `http://localhost:8080`.

---

## Endpoints de la API

La documentación completa de la API está disponible en [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) una vez que la aplicación está en marcha.

### Principales Endpoints

- **`GET /games/search?name={nombre}`**: Busca videojuegos por nombre.
  - **Ejemplo**: `http://localhost:8080/games/search?name=Zelda`

- **`GET /games/{id}`**: Obtiene los detalles completos de un videojuego por su ID de IGDB.
  - **Ejemplo**: `http://localhost:8080/games/1115` (para "The Legend of Zelda: Ocarina of Time")

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
    - **Ejemplo (environment.ts en Angular)**:
      ```typescript
      export const environment = {
        apiUrl: 'http://192.168.1.126:8080' // Usa la IP de tu PC
      };
      ```

---

## Arquitectura

El proyecto sigue los principios de la **Arquitectura Hexagonal** para separar el dominio de negocio de los detalles de infraestructura.

- **`domain`**: Contiene la lógica y las entidades del negocio (el "corazón" de la aplicación). No depende de nada.
- **`application`**: Orquesta los flujos de trabajo. Contiene los *puertos* (interfaces) y los *casos de uso* (servicios).
- **`infrastructure`**: Contiene las implementaciones concretas de los puertos (los "adaptadores"), como controladores REST, clientes de API externas (IGDB), etc.

