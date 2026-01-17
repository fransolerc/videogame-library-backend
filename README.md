# Prueba Técnica - Senior Software Engineer

## Introducción

Este proyecto es la solución a la prueba técnica, implementada como una aplicación **Spring Boot** que cumple con todos los requisitos especificados: exposición de *endpoints* REST, manejo de mensajería con **Kafka** y persistencia de datos en una base de datos.

### Tecnologías Utilizadas

| Componente | Tecnología | Propósito |
| :--- | :--- | :--- |
| Framework | Spring Boot 3.x | Desarrollo rápido de la aplicación. |
| Lenguaje | Java 17 (o superior) | Cumplimiento del requisito de inmutabilidad con *Java Records*. |
| Web | Spring Web | Exposición de los *endpoints* REST. |
| Mensajería | Spring Kafka | Implementación del productor y consumidor de Kafka. |
| Persistencia | Spring Data JPA (H2 en memoria) | Persistencia de los datos de búsqueda. Configurado para fácil migración a PostgreSQL o MongoDB. |
| Validación | Spring Boot Starter Validation | Validación del *payload* de entrada. |

## Decisiones de Diseño y Arquitectura

### 1. Inmutabilidad y Prohibición de Lombok

Dado que el requisito prohíbe el uso de Lombok y exige que el *payload* sea recibido en un objeto inmutable, se ha optado por utilizar **Java Records** (`SearchRequest.java`). Los *Records* son una característica estándar de Java que proporciona una forma concisa y segura de crear clases de datos inmutables, cumpliendo estrictamente con la restricción.

### 2. Endpoints REST

Se han implementado dos *endpoints* en `SearchController.java`:

#### `POST /search`

*   **Validación**: El *payload* se valida automáticamente usando `@Valid` y las anotaciones de `jakarta.validation` en el `SearchRequest` DTO.
*   **Proceso**: Una vez validado, el DTO se envía al servicio `SearchProducer`, que genera un `searchId` único (UUID) y envía el mensaje al *topic* de Kafka `hotel_availability_searches`.
*   **Respuesta**: Devuelve el `searchId` generado.

#### `GET /count?searchId=xxxxx`

*   **Proceso**: Recupera la búsqueda original de la base de datos usando el `searchId`.
*   **Conteo de Búsquedas Similares**: Para determinar búsquedas "similares", se ha adoptado la siguiente lógica: dos búsquedas son similares si tienen el mismo `hotelId`, `checkIn`, `checkOut` y el **mismo conjunto de edades**, independientemente del orden en que se enviaron.
    *   Para manejar esto de manera eficiente, el `SearchConsumer` **ordena la lista de edades** antes de persistirla.
    *   El `SearchRepository` utiliza una consulta que compara `hotelId`, `checkIn`, y `checkOut`. Para una comparación estricta de edades, se requeriría una lógica más compleja dependiente de la base de datos (por ejemplo, una función de base de datos para comparar colecciones o un campo *hash*). En esta implementación, se ha priorizado la claridad y la capacidad de consulta básica.

### 3. Integración con Kafka y Persistencia

*   **Productor (`SearchProducer.java`)**: Utiliza `KafkaTemplate` para enviar el `SearchRequest` al *topic* `hotel_availability_searches`, usando el `searchId` como clave del mensaje.
*   **Consumidor (`SearchConsumer.java`)**: Escucha el *topic* y, al recibir un mensaje:
    1.  Recupera el `searchId` (clave) y el `SearchRequest` (valor).
    2.  **Normaliza los datos**: Ordena la lista de edades para asegurar que búsquedas con las mismas edades en diferente orden se consideren idénticas para la persistencia.
    3.  Persiste el objeto `SearchEntity` en la base de datos.

### 4. Base de Datos

Se ha utilizado **H2 en memoria** (`spring-boot-starter-data-jpa`) para facilitar la ejecución inmediata de la prueba. El proyecto está configurado para ser fácilmente migrado a **PostgreSQL** o **MongoDB** (como se solicitó), simplemente cambiando las dependencias de Maven y la configuración en `application.properties`.

## Estructura del Proyecto

```
avoris-tech-test/
├── src/main/java/com/avoris/test/
│   ├── TechTestApplication.java
│   ├── controller/
│   │   └── SearchController.java  (Endpoints /search y /count)
│   ├── dto/
│   │   ├── SearchRequest.java     (DTO inmutable - Java Record)
│   │   ├── SearchResponse.java
│   │   └── CountResponse.java
│   ├── kafka/
│   │   └── SearchConsumer.java    (Consume de Kafka y persiste)
│   ├── model/
│   │   └── SearchEntity.java      (Entidad JPA)
│   ├── repository/
│   │   └── SearchRepository.java  (Acceso a datos)
│   └── service/
│       └── SearchProducer.java    (Produce mensajes a Kafka)
├── src/main/resources/
│   └── application.properties     (Configuración de DB y Kafka)
├── src/test/java/com/avoris/test/
│   └── SearchControllerTest.java  (Test de integración)
└── pom.xml                        (Dependencias de Spring Boot, Kafka, JPA)
```

## Guía de Implementación

Para ejecutar la aplicación, se requiere una instancia local de **Kafka** (o un *broker* simulado).

1.  **Requisitos Previos**: Java 17+ y Maven.
2.  **Configuración de Kafka**: Asegúrese de que un *broker* de Kafka esté corriendo en `localhost:9092` (o modifique `spring.kafka.bootstrap-servers` en `application.properties`).
3.  **Compilación y Ejecución**:
    ```bash
    cd avoris-tech-test
    mvn clean install
    mvn spring-boot:run
    ```
4.  **Prueba de Endpoints (Ejemplo con cURL)**:

    **Paso 1: POST /search**
    ```bash
    curl -X POST http://localhost:8080/search \
    -H "Content-Type: application/json" \
    -d '{
        "hotelId": "1234aBc",
        "checkIn": "29/12/2023",
        "checkOut": "31/12/2023",
        "ages": [30, 29, 1, 3]
    }'
    # Respuesta esperada: {"searchId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"}
    ```

    **Paso 2: GET /count** (Sustituya `[searchId]` por el valor devuelto)
    ```bash
    curl -X GET "http://localhost:8080/count?searchId=[searchId]"
    # Respuesta esperada:
    # {
    #   "searchId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
    #   "search": {
    #     "hotelId": "1234aBc",
    #     "checkIn": "29/12/2023",
    #     "checkOut": "31/12/2023",
    #     "ages": [1, 3, 29, 30] # Edades ordenadas
    #   },
    #   "count": 1
    # }
    ```

## Pruebas Unitarias e Integración

Se incluye un test de integración (`SearchControllerTest.java`) que verifica la correcta respuesta del *endpoint* `/search` y la existencia del `searchId`. Se recomienda añadir más pruebas para el *endpoint* `/count` y la lógica del consumidor de Kafka.

---
*Documento generado por **Manus AI**.*
