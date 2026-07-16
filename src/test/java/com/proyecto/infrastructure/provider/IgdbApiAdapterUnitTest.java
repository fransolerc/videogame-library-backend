package com.proyecto.infrastructure.provider;

import com.proyecto.domain.model.Game;
import com.proyecto.domain.model.Platform;
import com.proyecto.domain.model.PlatformType;
import com.proyecto.infrastructure.config.IgdbApiConfig;
import io.github.bucket4j.BlockingBucket;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * Cobertura de IgdbApiAdapter usando MockRestServiceServer en lugar de mockear RestTemplate
 * directamente. Los DTOs internos (IgdbGameResponse, etc.) son records privados anidados,
 * inaccesibles desde fuera de la clase, por lo que verificar el contrato HTTP real (URL,
 * cabeceras, cuerpo) contra respuestas JSON crudas es el enfoque más robusto y evita
 * depender de la implementación interna del adapter.
 */
@ExtendWith(MockitoExtension.class)
class IgdbApiAdapterUnitTest {

    private static final String BASE_URL = "https://api.igdb.com/v4";
    private static final String AUTH_URL = "https://id.twitch.tv/oauth2/token";
    private static final String CLIENT_ID = "test-client-id";
    private static final String CLIENT_SECRET = "test-client-secret";

    @Mock
    private IgdbApiConfig apiConfig;

    @Mock
    private Bucket rateLimiter;

    @Mock
    private BlockingBucket blockingBucket;

    private MockRestServiceServer mockServer;
    private IgdbApiAdapter adapter;

    @BeforeEach
    void setUp() throws InterruptedException {
        lenient().when(apiConfig.getApiBaseUrl()).thenReturn(BASE_URL);
        lenient().when(apiConfig.getAuthUrl()).thenReturn(AUTH_URL);
        lenient().when(apiConfig.getClientId()).thenReturn(CLIENT_ID);
        lenient().when(apiConfig.getClientSecret()).thenReturn(CLIENT_SECRET);

        lenient().when(rateLimiter.asBlocking()).thenReturn(blockingBucket);
        lenient().doNothing().when(blockingBucket).consume(1);

        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        adapter = new IgdbApiAdapter(apiConfig, restTemplate, rateLimiter);
    }

    @AfterEach
    void clearInterruptFlag() {
        // Algunos tests fuerzan Thread.currentThread().interrupt(); lo limpiamos para no
        // contaminar otros tests que corran en el mismo hilo.
        Thread.interrupted();
    }

    private void expectSuccessfulAuth(String token) {
        mockServer.expect(requestTo(AUTH_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"access_token\":\"" + token + "\",\"expires_in\":" + (long) 3600 + ",\"token_type\":\"bearer\"}",
                        MediaType.APPLICATION_JSON));
    }

    private void expectFailedAuth() {
        mockServer.expect(requestTo(AUTH_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"status\":403,\"message\":\"invalid client secret\"}"));
    }

    @Nested
    class Authentication {

        @Test
        void shouldAuthenticateAndAttachTokenBeforeFirstCall() {
            expectSuccessfulAuth("tok123");
            mockServer.expect(requestTo(BASE_URL + "/games"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(header("Authorization", "Bearer tok123"))
                    .andExpect(header("Client-ID", CLIENT_ID))
                    .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

            Optional<Game> result = adapter.findByExternalId(1L);

            assertTrue(result.isEmpty());
            mockServer.verify();
        }

        @Test
        void shouldReuseTokenAcrossCallsWithinExpirationWindow() {
            // Solo se registra UNA expectativa de autenticación: si el adapter reautentica
            // en la segunda llamada, MockRestServiceServer fallará por petición no esperada.
            expectSuccessfulAuth("tok123");
            mockServer.expect(ExpectedCount.times(2), requestTo(BASE_URL + "/games"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(header("Authorization", "Bearer tok123"))
                    .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

            adapter.findByExternalId(1L);
            adapter.findByExternalId(2L);

            mockServer.verify();
        }

        @Test
        void shouldReauthenticateOnceTokenHasExpired() {
            // expires_in negativo fuerza que el token quede caducado inmediatamente.
            mockServer.expect(requestTo(AUTH_URL))
                    .andRespond(withSuccess("{\"access_token\":\"tok1\",\"expires_in\":-1,\"token_type\":\"bearer\"}", MediaType.APPLICATION_JSON));
            mockServer.expect(requestTo(BASE_URL + "/games"))
                    .andExpect(header("Authorization", "Bearer tok1"))
                    .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

            mockServer.expect(requestTo(AUTH_URL))
                    .andRespond(withSuccess("{\"access_token\":\"tok2\",\"expires_in\":3600,\"token_type\":\"bearer\"}", MediaType.APPLICATION_JSON));
            mockServer.expect(requestTo(BASE_URL + "/games"))
                    .andExpect(header("Authorization", "Bearer tok2"))
                    .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

            adapter.findByExternalId(1L);
            adapter.findByExternalId(2L);

            mockServer.verify();
        }

        /**
         * Test de regresión directo del bug corregido: si la autenticación falla, el
         * adapter NO debe intentar ninguna petición a IGDB (antes se enviaba igualmente
         * con "Authorization: Bearer null"). Como no se registra ninguna expectativa para
         * /games ni /platforms, cualquier intento de llamada haría fallar el test.
         */
        @Test
        void shouldNotCallIgdbWhenAuthenticationFails() {
            expectFailedAuth();

            Optional<Game> gameResult = adapter.findByExternalId(1L);

            assertTrue(gameResult.isEmpty());
            mockServer.verify();
        }

        @Test
        void shouldNotSendBearerNullToPlatformsEndpointWhenAuthFails() {
            expectFailedAuth();

            List<Platform> result = adapter.listPlatforms();

            assertTrue(result.isEmpty());
            mockServer.verify();
        }

        @Test
        void shouldInvalidateStaleTokenAfterFailedReauthentication() {
            // Primera autenticación exitosa pero con caducidad inmediata.
            mockServer.expect(requestTo(AUTH_URL))
                    .andRespond(withSuccess("{\"access_token\":\"tok1\",\"expires_in\":-1,\"token_type\":\"bearer\"}", MediaType.APPLICATION_JSON));
            mockServer.expect(requestTo(BASE_URL + "/games"))
                    .andExpect(header("Authorization", "Bearer tok1"))
                    .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

            // El reintento de autenticación falla.
            mockServer.expect(requestTo(AUTH_URL))
                    .andRespond(withStatus(HttpStatus.FORBIDDEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"status\":403,\"message\":\"invalid client secret\"}"));

            adapter.findByExternalId(1L);
            Optional<Game> secondResult = adapter.findByExternalId(2L);

            assertTrue(secondResult.isEmpty());
            // Ninguna segunda petición a /games registrada: si el token viejo "tok1" se
            // hubiera reutilizado indebidamente, MockRestServiceServer habría fallado.
            mockServer.verify();
        }
    }

    @Nested
    class RateLimiter {

        @Test
        void findByExternalId_shouldReturnEmptyAndNotCallIgdbWhenRateLimiterInterrupted() throws InterruptedException {
            doThrow(new InterruptedException()).when(blockingBucket).consume(1);

            Optional<Game> result = adapter.findByExternalId(1L);

            assertTrue(result.isEmpty());
            assertTrue(Thread.currentThread().isInterrupted());
            mockServer.verify(); // ninguna expectativa registrada: cero peticiones HTTP realizadas
        }

        @Test
        void listPlatforms_shouldReturnEmptyAndNotCallIgdbWhenRateLimiterInterrupted() throws InterruptedException {
            doThrow(new InterruptedException()).when(blockingBucket).consume(1);

            List<Platform> result = adapter.listPlatforms();

            assertTrue(result.isEmpty());
            assertTrue(Thread.currentThread().isInterrupted());
            mockServer.verify();
        }

        @Test
        void filterGames_shouldReturnEmptyPageAndNotCallIgdbWhenRateLimiterInterrupted() throws InterruptedException {
            doThrow(new InterruptedException()).when(blockingBucket).consume(1);

            Page<Game> result = adapter.filterGames("rating > 80", "rating desc", 10, 0);

            assertTrue(result.isEmpty());
            mockServer.verify();
        }
    }

    @Nested
    class FindByExternalId {

        @Test
        void shouldMapFullGameResponseCorrectly() {
            expectSuccessfulAuth("tok");
            String json = """
                [{
                  "id": 1942,
                  "name": "The Witcher 3: Wild Hunt",
                  "genres": [{"name": "RPG"}, {"name": "Adventure"}],
                  "first_release_date": 1431993600,
                  "cover": {"image_id": "co1wyy"},
                  "artworks": [],
                  "summary": "An epic RPG.",
                  "storyline": "Geralt hunts the Wild Hunt.",
                  "videos": [{"video_id": "c0i88t"}],
                  "screenshots": [{"image_id": "sc1abc"}],
                  "platforms": [{"id": 6, "name": "PC", "generation": null, "platform_type": null}],
                  "rating": 93.5
                }]
                """;
            mockServer.expect(requestTo(BASE_URL + "/games"))
                    .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

            Optional<Game> result = adapter.findByExternalId(1942L);

            assertTrue(result.isPresent());
            Game game = result.get();
            assertEquals(1942L, game.id());
            assertEquals("The Witcher 3: Wild Hunt", game.name());
            assertEquals(List.of("RPG", "Adventure"), game.genres());
            assertEquals(LocalDate.of(2015, Month.MAY, 19), game.releaseDate());
            assertEquals("https://images.igdb.com/igdb/image/upload/t_cover_big/co1wyy.jpg", game.coverImageUrl());
            assertEquals(List.of("https://www.youtube.com/watch?v=c0i88t"), game.videos());
            assertEquals(List.of("https://images.igdb.com/igdb/image/upload/t_screenshot_big/sc1abc.jpg"), game.screenshots());
            assertEquals(List.of("PC"), game.platforms());
            assertEquals(93.5, game.rating());
            mockServer.verify();
        }

        @Test
        void shouldUsePlaceholderImageWhenCoverIsMissing() {
            expectSuccessfulAuth("tok");
            mockServer.expect(requestTo(BASE_URL + "/games"))
                    .andRespond(withSuccess("[{\"id\":1,\"name\":\"Untitled\"}]", MediaType.APPLICATION_JSON));

            Optional<Game> result = adapter.findByExternalId(1L);

            assertTrue(result.isPresent());
            assertEquals("https://placehold.co/600x400", result.get().coverImageUrl());
            assertEquals(Collections.emptyList(), result.get().genres());
            assertEquals(Collections.emptyList(), result.get().videos());
            assertEquals(Collections.emptyList(), result.get().screenshots());
            assertEquals(Collections.emptyList(), result.get().platforms());
        }

        @Test
        void shouldReturnEmptyWhenIgdbReturnsNoResults() {
            expectSuccessfulAuth("tok");
            mockServer.expect(requestTo(BASE_URL + "/games"))
                    .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

            assertTrue(adapter.findByExternalId(999L).isEmpty());
        }

        @Test
        void shouldReturnEmptyWhenIgdbRespondsWithServerError() {
            expectSuccessfulAuth("tok");
            mockServer.expect(requestTo(BASE_URL + "/games"))
                    .andRespond(withServerError());

            Optional<Game> result = adapter.findByExternalId(1L);

            assertTrue(result.isEmpty());
            mockServer.verify();
        }
    }

    @Nested
    class FindMultipleByExternalIds {

        @Test
        void shouldReturnEmptyImmediatelyForNullList_withoutAnyHttpCall() {
            List<Game> result = adapter.findMultipleByExternalIds(null);

            assertTrue(result.isEmpty());
            mockServer.verify(); // ninguna expectativa registrada: confirma que no se intentó autenticar ni llamar a IGDB
        }

        @Test
        void shouldReturnEmptyImmediatelyForEmptyList_withoutAnyHttpCall() {
            List<Game> result = adapter.findMultipleByExternalIds(Collections.emptyList());

            assertTrue(result.isEmpty());
            mockServer.verify();
        }

        @Test
        void shouldFetchAndMapMultipleGames() {
            expectSuccessfulAuth("tok");
            mockServer.expect(requestTo(BASE_URL + "/games"))
                    .andExpect(content().string(containsString("where id = (1,2);")))
                    .andRespond(withSuccess(
                            "[{\"id\":1,\"name\":\"Game One\"},{\"id\":2,\"name\":\"Game Two\"}]",
                            MediaType.APPLICATION_JSON));

            List<Game> result = adapter.findMultipleByExternalIds(List.of(1L, 2L));

            assertEquals(2, result.size());
            assertEquals("Game One", result.get(0).name());
            assertEquals("Game Two", result.get(1).name());
        }
    }

    @Nested
    class SearchByName {

        @Test
        void shouldSendSearchQueryAndMapResults() {
            expectSuccessfulAuth("tok");
            mockServer.expect(requestTo(BASE_URL + "/games"))
                    .andExpect(content().string(containsString("search \"zelda\";")))
                    .andRespond(withSuccess("[{\"id\":1,\"name\":\"Zelda\"}]", MediaType.APPLICATION_JSON));

            List<Game> result = adapter.searchByName("zelda");

            assertEquals(1, result.size());
            assertEquals("Zelda", result.getFirst().name());
        }

        @Test
        void shouldReturnEmptyListOnError() {
            expectSuccessfulAuth("tok");
            mockServer.expect(requestTo(BASE_URL + "/games"))
                    .andRespond(withServerError());

            assertTrue(adapter.searchByName("anything").isEmpty());
        }
    }

    @Nested
    class FilterGames {

        @Test
        void shouldReturnEmptyPageWithoutFetchingGamesWhenCountIsZero() {
            expectSuccessfulAuth("tok");
            mockServer.expect(requestTo(BASE_URL + "/games/count"))
                    .andRespond(withSuccess("{\"count\":0}", MediaType.APPLICATION_JSON));
            // Sin expectativa para /games: si el adapter la llamara igualmente, el test fallaría.

            Page<Game> result = adapter.filterGames("genres = (12)", null, 10, 0);

            assertTrue(result.isEmpty());
            mockServer.verify();
        }

        @Test
        void shouldBuildQueryAndReturnPagedResults() {
            expectSuccessfulAuth("tok");
            mockServer.expect(requestTo(BASE_URL + "/games/count"))
                    .andExpect(content().string("where rating > 80;"))
                    .andRespond(withSuccess("{\"count\":25}", MediaType.APPLICATION_JSON));
            mockServer.expect(requestTo(BASE_URL + "/games"))
                    .andExpect(content().string(containsString("where rating > 80; sort rating desc; limit 10; offset 10;")))
                    .andRespond(withSuccess(
                            "[{\"id\":1,\"name\":\"Game A\"},{\"id\":2,\"name\":\"Game B\"}]",
                            MediaType.APPLICATION_JSON));

            Page<Game> result = adapter.filterGames("rating > 80", "rating desc", 10, 10);

            assertEquals(2, result.getContent().size());
            assertEquals(25, result.getTotalElements());
            assertEquals(1, result.getNumber()); // offset 10 / pageSize 10 = página 1 (0-indexada)
            assertEquals(10, result.getSize());
        }

        @Test
        void shouldReturnEmptyPageWhenCountRequestFails() {
            expectSuccessfulAuth("tok");
            mockServer.expect(requestTo(BASE_URL + "/games/count"))
                    .andRespond(withServerError());

            Page<Game> result = adapter.filterGames("rating > 80", null, 10, 0);

            assertTrue(result.isEmpty());
            mockServer.verify();
        }
    }

    @Nested
    class ListPlatforms {

        @Test
        void shouldMapPlatformsIncludingKnownAndUnknownTypes() {
            expectSuccessfulAuth("tok");
            String json = """
                [
                  {"id": 6, "name": "PC", "generation": 4, "platform_type": 6},
                  {"id": 99, "name": "Misterioso", "generation": null, "platform_type": null}
                ]
                """;
            mockServer.expect(requestTo(BASE_URL + "/platforms"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

            List<Platform> result = adapter.listPlatforms();

            assertEquals(2, result.size());
            assertEquals(new Platform(6L, "PC", 4, PlatformType.COMPUTER), result.get(0));
            assertEquals(PlatformType.UNKNOWN, result.get(1).platformType());
        }

        @Test
        void shouldReturnEmptyListOnError() {
            expectSuccessfulAuth("tok");
            mockServer.expect(requestTo(BASE_URL + "/platforms"))
                    .andRespond(withServerError());

            assertTrue(adapter.listPlatforms().isEmpty());
        }
    }
}
