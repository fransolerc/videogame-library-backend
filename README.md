# Video Game Library Backend

REST API for a video game library application, built with **Hexagonal Architecture**. It fetches game data in real time from the [IGDB API](https://api-docs.igdb.com/) and stores users and their libraries in a local database.

The frontend lives in a separate repository (Angular).

---

## Core Technologies

- **Java 25** and **Maven**
- **Spring Boot 4** (Web, Data JPA, Security, Validation); exact versions are in `pom.xml`
- **H2**, file-based by default (`./data/videogamedb`), in-memory for tests
- **Spring Security + JWT (JJWT)**: stateless authentication
- **OpenAPI 3 + openapi-generator**: the API interfaces and DTOs are generated from `src/main/resources/api/openapi.yaml` at build time
- **springdoc / Swagger UI**: interactive documentation
- **MapStruct** and **Lombok**
- **Bucket4j**: rate limiting for outgoing IGDB calls
- **JUnit 5, Mockito, MockRestServiceServer, JaCoCo**

---

## Getting Started

### Prerequisites

- **JDK 25**
- **Maven 3.9+**
- **Docker** (optional, for running in a container)
- IGDB credentials from <https://dev.twitch.tv/console/apps>

### Configuration

All secrets are read from environment variables. `application.yml` defines **no defaults** for the first three, so the application (and the tests) will not start without them.

| Variable | Required | Description |
|:--|:--|:--|
| `IGDB_CLIENT_ID` | yes | Twitch application client ID |
| `IGDB_CLIENT_SECRET` | yes | Twitch application client secret |
| `JWT_SECRET` | yes | Base64 string that decodes to at least 32 bytes |
| `CORS_ALLOWED_ORIGINS` | no | Comma-separated origins. Default: `http://localhost:4200` |

Copy the example file and fill in your values:

```sh
cp .env.example .env
```

Generate a valid `JWT_SECRET` as **a single line, without quotes**:

```sh
openssl rand -base64 64 | tr -d '\n'
```

> `openssl rand -base64 64` wraps its output across two lines. If you paste both lines, the secret is truncated. An invalid secret makes the login fail after the credentials are checked.

### Running with Docker Compose

```sh
docker compose up --build
```

Compose reads `.env` from the project root. The H2 database is stored in the named volume `videogame-data` (mounted at `/app/data`), so users survive rebuilds. `docker compose down -v` deletes it.

### Running with Maven

Maven does not read `.env`. Export the variables first (or define them in your IDE run configuration):

```sh
set -a && source .env && set +a
mvn spring-boot:run
```

The application starts at `http://localhost:8080`.

---

## API

Interactive documentation: **<http://localhost:8080/swagger-ui.html>**. The contract is `src/main/resources/api/openapi.yaml`.

### Public endpoints

| Method | Endpoint | Description |
|:--|:--|:--|
| `GET` | `/health` | Returns `{"status": "UP"}` |
| `POST` | `/users/register` | Registers a user. Password: at least 8 characters, one uppercase, one lowercase and one digit |
| `POST` | `/users/login` | Returns a JWT, `userId` and `username` (login is by **email**) |
| `GET` | `/games/search?name=` | Searches games by name |
| `GET` | `/games/{id}` | Full details of a game (IGDB ID) |
| `POST` | `/games/filter` | Advanced search with IGDB filters, sorting and pagination |
| `POST` | `/games/batch` | Details of several games from a list of IDs |
| `GET` | `/platforms` | Lists all platforms |

### Authenticated endpoints (`Authorization: Bearer <token>`)

A user can only access **their own** library. Requesting another user's `userId` returns `403`.

| Method | Endpoint | Description |
|:--|:--|:--|
| `GET` | `/users/{userId}/games` | Lists the user's library |
| `GET` | `/users/{userId}/games/{gameId}` | Status of one game (`404` if not in the library) |
| `PUT` | `/users/{userId}/games/{gameId}` | Adds a game or updates its status. With status `NONE` and not a favorite, the entry is removed (`204`) |
| `DELETE` | `/users/{userId}/games/{gameId}` | Removes a game from the library |
| `POST` | `/users/{userId}/games/{gameId}/favorite` | Marks as favorite. If not in the library, it is added with status `NONE` |
| `DELETE` | `/users/{userId}/games/{gameId}/favorite` | Removes from favorites. If the status is `NONE`, the entry is removed |
| `GET` | `/users/{userId}/favorites` | Paginated list of favorites (`page`, `size`) |

Game statuses: `NONE`, `WANT_TO_PLAY`, `PLAYING`, `COMPLETED`.

---

## Runtime notes

- **Cache:** IGDB responses are cached in memory (`ConcurrentMapCacheManager`). Entries **never expire** until the application restarts.
- **Rate limit:** outgoing IGDB calls go through a token bucket of 10 requests per second.
- **H2 console:** enabled at `/h2-console` and publicly accessible. It is intended for **development only**; disable it before exposing the service.
- **IGDB errors:** failures are logged and surface as empty results, not as errors.

---

## Testing

```sh
mvn clean verify
```

- **Unit tests:** services and the IGDB adapter (HTTP contract checked with `MockRestServiceServer`).
- **Integration tests:** controllers with `MockMvc` and an in-memory H2 database.

The environment variables listed above must be defined, otherwise the Spring context fails to start. A JaCoCo report is generated in `target/site/jacoco`.

---

## Architecture

```
com.proyecto
├── domain                  Models and domain exceptions
├── application
│   ├── port.in             Use-case interfaces (*ServiceInterface)
│   ├── port.out            Output ports (persistence, provider)
│   └── service             Use-case implementations
└── infrastructure
    ├── adapter.in.web      REST controllers, mappers, error handling
    ├── adapter.out.persistence   JPA entities, repositories, adapters
    ├── provider            IGDB adapter
    ├── security            JWT filter and token provider
    └── config              Security, CORS, cache, rate limiter, IGDB client
```

The dependency direction is `infrastructure → application → domain`. There are pragmatic deviations:

- Some domain records carry Jackson annotations (`Game`, `Artwork`, `PlatformType`).
- `UserService` depends directly on `JwtTokenProvider` and Spring Security's `AuthenticationManager`.