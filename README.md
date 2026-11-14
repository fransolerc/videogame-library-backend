# Video Game Library Backend

This project is a backend for a video game library application, built with a **Hexagonal Architecture**. It integrates with the [IGDB API](https://api-docs.igdb.com/) to fetch real-time game data.

The goal is to serve as a practical example of a modern, clean, and scalable software architecture, including a robust authentication system.

---

## Core Technologies

- **Java 25**
- **Spring Boot 4**
- **Maven**
- **Spring Data JPA / Hibernate**: For data persistence.
- **H2 Database**: In-memory database for development and testing.
- **OpenAPI 3 / Swagger UI**: For API documentation and generation.
- **Hexagonal Architecture (Ports and Adapters)**
- **Spring Security**: Authentication and authorization (JWT).
- **JSON Web Tokens (JWT)**: For stateless authentication.
- **JJWT**: Library for JWT implementation.
- **JUnit 5 / Mockito**: For unit and integration testing.

---

## Getting Started

### Prerequisites

- **Docker Desktop** (or Docker Engine) installed and running.
- An API client like Postman, Insomnia, or just your browser.
- **JDK 25** or higher.
- **Maven 3.8** or higher.

### Mandatory Configuration

Credentials are read from environment variables. Copy the example file and fill in your values:

```sh
cp .env.example .env
```

- `IGDB_CLIENT_ID` and `IGDB_CLIENT_SECRET`: from https://dev.twitch.tv/console/apps
- `JWT_SECRET`: a Base64 string, generate one with `openssl rand -base64 64`

### Running with Docker

1.  **Build the Docker image**:
    -   Open a terminal in the project root and run:
        ```sh
        docker build -t videogame-library-backend .
        ```

### Running the Application (Traditional Method)

1.  **Generate code and compile**:
    ```sh
    mvn clean install
    ```

2.  **Run the application**:
    ```sh
    mvn spring-boot:run
    ```

The application will start at `http://localhost:8080`.

---

## API Endpoints

Full API documentation is available at **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)** once the application is running.

### Authentication & Status

| Method | Endpoint          | Description                                                                                                       |
|:-------|:------------------|:------------------------------------------------------------------------------------------------------------------|
| `POST` | `/users/register` | Registers a new user. The password must have at least 8 characters, one uppercase, one lowercase, and one number. |
| `POST` | `/users/login`    | Logs in and returns a JWT, `userId`, and `username`.                                                              |
| `GET`  | `/health`         | Checks the application's health status. Returns `{"status": "UP"}` if everything is correct.                      |

### Game Discovery

| Method | Endpoint        | Description                                                                    |
|:-------|:----------------|:-------------------------------------------------------------------------------|
| `GET`  | `/games/search` | Searches for video games by name. (e.g., `?name=Zelda`)                        |
| `GET`  | `/games/{id}`   | Gets the full details of a video game by its IGDB ID.                          |
| `POST` | `/games/filter` | Performs an advanced search with filters, sorting, and pagination.             |
| `POST` | `/games/batch`  | Gets the full details of multiple video games from a list of IDs.              |

### Platforms

| Method | Endpoint     | Description                                      |
|:-------|:-------------|:-------------------------------------------------|
| `GET`  | `/platforms` | Lists all available video game platforms.        |

### User Library

| Method   | Endpoint                                  | Description                                                                                                              |
|:---------|:------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------|
| `GET`    | `/users/{userId}/games`                   | Lists all games in a user's library.                                                                                     |
| `GET`    | `/users/{userId}/games/{gameId}`          | Gets the status of a specific game in the user's library.                                                                |
| `PUT`    | `/users/{userId}/games/{gameId}`          | Adds a game to the library or updates its status. If the status is `NONE` and it's not a favorite, the entry is removed. |
| `DELETE` | `/users/{userId}/games/{gameId}`          | Removes a game from a user's library (complete deletion).                                                                |
| `POST`   | `/users/{userId}/games/{gameId}/favorite` | Marks a game as a favorite. If not in the library, it's added with `NONE` status. Returns the updated resource.          |
| `DELETE` | `/users/{userId}/games/{gameId}/favorite` | Removes a game from favorites. If the game's status is `NONE`, it's completely removed from the library.                 |
| `GET`    | `/users/{userId}/favorites`               | Lists all of a user's favorite games (paginated).                                                                        |

---

## Testing

The project has high test coverage, including:

-   **Unit Tests**: For application services and domain logic.
-   **Integration Tests**: For REST controllers and database interaction.

To run all tests, use the following Maven command:

```sh
mvn clean verify
```

---

## Architecture

The project follows the principles of **Hexagonal Architecture** to separate the business domain from infrastructure details.

-   **`domain`**: Contains the business logic and entities (the "core" of the application). It has no dependencies on other layers.
-   **`application`**: Orchestrates workflows. It contains the *ports* (`UseCase` interfaces) and the *use cases* (services that implement those `UseCase` interfaces).
-   **`infrastructure`**: Contains the concrete implementations of the ports (the "adapters").
    -   **`adapter.in.web`**: Input adapters (Driving Adapters), such as REST controllers.
    -   **`adapter.out.persistence`**: Output adapters (Driven Adapters) for databases (JPA).
    -   **`adapter.out.provider`**: Output adapters for external providers (IGDB API).
    -   **`security`**: Security configuration (JWT).
    -   **`config`**: General application configurations.
