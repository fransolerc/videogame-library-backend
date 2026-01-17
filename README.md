# Video Game Library Backend

Este proyecto es un backend para una aplicación de biblioteca de videojuegos, construido con una **Arquitectura Hexagonal** y comunicación asíncrona mediante **Apache Kafka**.

El objetivo es servir como un ejemplo práctico de una arquitectura de software moderna, limpia y escalable.

---

## Tecnologías Principales

- **Java 21**
- **Spring Boot 3**
- **Maven**
- **Apache Kafka**: Para procesamiento asíncrono de eventos.
- **Spring Data JPA / Hibernate**: Para la persistencia de datos.
- **H2 Database**: Base de datos en memoria para desarrollo y pruebas.
- **OpenAPI 3 / Swagger UI**: Para la documentación de la API.
- **Testcontainers**: Para tests de integración realistas.
- **Arquitectura Hexagonal (Puertos y Adaptadores)**

---

## Cómo Empezar

### Prerrequisitos

- JDK 21 o superior.
- Maven 3.8 o superior.
- Docker y Docker Compose (para levantar Kafka).

### Levantando el Entorno

1.  **Iniciar Kafka**:
    ```sh
    # (Próximamente se añadirá un docker-compose.yml)
    # Por ahora, se asume que Kafka está corriendo en localhost:9092
    ```

2.  **Ejecutar la aplicación**:
    ```sh
    mvn spring-boot:run
    ```

### Documentación de la API

Una vez que la aplicación está corriendo, puedes acceder a la documentación de la API generada por Swagger UI en la siguiente URL:

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## Arquitectura

El proyecto sigue los principios de la **Arquitectura Hexagonal** para separar el dominio de negocio de los detalles de infraestructura.

- **`domain`**: Contiene la lógica y las entidades del negocio (el "corazón" de la aplicación). No depende de nada.
- **`application`**: Orquesta los flujos de trabajo. Contiene los *puertos* (interfaces) y los *casos de uso* (servicios).
- **`infrastructure`**: Contiene las implementaciones concretas de los puertos (los "adaptadores"), como controladores REST, consumidores de Kafka, repositorios JPA, etc.

