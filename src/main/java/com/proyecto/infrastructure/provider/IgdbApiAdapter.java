package com.proyecto.infrastructure.provider;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.proyecto.application.port.out.provider.GameProviderInterface;
import com.proyecto.application.port.out.provider.PlatformProviderInterface;
import com.proyecto.domain.model.Artwork;
import com.proyecto.domain.model.Game;
import com.proyecto.domain.model.Platform;
import com.proyecto.domain.model.PlatformType;
import com.proyecto.infrastructure.config.IgdbApiConfig;
import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class IgdbApiAdapter implements GameProviderInterface, PlatformProviderInterface {

    private static final Logger logger = LoggerFactory.getLogger(IgdbApiAdapter.class);
    private static final String GAMES_URL = "/games";
    private static final String GAMES_COUNT_URL = "/games/count";
    private static final String PLACEHOLDER_IMAGE_URL = "https://placehold.co/600x400";
    private static final String HEADER_CLIENT_ID = "Client-ID";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String FIELDS_GAME_BASE = "fields name, genres.name, first_release_date, cover.image_id, artworks.*, summary, storyline, videos.video_id, screenshots.image_id, platforms.name, rating;";
    private static final String RATE_LIMITER_INTERRUPTION_MESSAGE = "Thread interrupted while waiting for rate limiter token";

    private final IgdbApiConfig apiConfig;
    private final RestTemplate restTemplate;
    private final Bucket rateLimiter;

    private String accessToken;
    private long tokenExpirationTime;

    private record AuthResponse(@JsonProperty("access_token") String accessToken, @JsonProperty("expires_in") long expiresIn) {}
    private record IgdbGameResponse(
            long id,
            String name,
            List<IgdbGenreResponse> genres,
            @JsonProperty("first_release_date") Long releaseDate,
            IgdbCoverResponse cover,
            List<Artwork> artworks,
            String summary,
            String storyline,
            List<IgdbVideoResponse> videos,
            List<IgdbScreenshotResponse> screenshots,
            List<IgdbPlatformResponse> platforms,
            Double rating
    ) {}
    private record IgdbGenreResponse(String name) {}
    private record IgdbCoverResponse(@JsonProperty("image_id") String imageId) {}
    private record IgdbVideoResponse(@JsonProperty("video_id") String videoId) {}
    private record IgdbScreenshotResponse(@JsonProperty("image_id") String imageId) {}
    private record IgdbPlatformResponse(
            long id,
            String name,
            Integer generation,
            @JsonProperty("platform_type") Integer platformType
    ) {}
    private record IgdbCountResponse(long count) {}


    public IgdbApiAdapter(IgdbApiConfig apiConfig, RestTemplate restTemplate, Bucket rateLimiter) {
        this.apiConfig = apiConfig;
        this.restTemplate = restTemplate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    @Cacheable("igdb-game-by-id")
    public Optional<Game> findByExternalId(Long externalId) {
        if (rateLimiterInterrupted()) return Optional.empty();
        ensureAuthentication();

        HttpHeaders headers = createHeaders();
        String requestBody = String.format("%s where id = %s;", FIELDS_GAME_BASE, externalId);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<IgdbGameResponse[]> response = restTemplate.postForEntity(apiConfig.getApiBaseUrl() + GAMES_URL, entity, IgdbGameResponse[].class);
            IgdbGameResponse[] responseBody = response.getBody();

            if (response.getStatusCode() == HttpStatus.OK && responseBody != null && responseBody.length > 0) {
                return Optional.of(mapToDomain(responseBody[0]));
            }
        } catch (Exception e) {
            logger.error("Error fetching game with id {} from IGDB", externalId, e);
        }

        return Optional.empty();
    }

    @Override
    @Cacheable("igdb-games-by-ids")
    public List<Game> findMultipleByExternalIds(List<Long> externalIds) {
        if (externalIds == null || externalIds.isEmpty()) {
            return Collections.emptyList();
        }

        if (rateLimiterInterrupted()) return Collections.emptyList();
        ensureAuthentication();

        HttpHeaders headers = createHeaders();
        String ids = externalIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        String requestBody = String.format("%s where id = (%s); limit %d;", FIELDS_GAME_BASE, ids, externalIds.size());
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<IgdbGameResponse[]> response = restTemplate.postForEntity(apiConfig.getApiBaseUrl() + GAMES_URL, entity, IgdbGameResponse[].class);
            IgdbGameResponse[] responseBody = response.getBody();

            if (response.getStatusCode() == HttpStatus.OK && responseBody != null) {
                return Arrays.stream(responseBody)
                        .map(this::mapToDomain)
                        .toList();
            }
        } catch (Exception e) {
            logger.error("Error fetching games with ids {} from IGDB", ids, e);
        }

        return Collections.emptyList();
    }

    @Override
    @Cacheable("igdb-games-by-name")
    public List<Game> searchByName(String name) {
        if (rateLimiterInterrupted()) return Collections.emptyList();
        ensureAuthentication();

        HttpHeaders headers = createHeaders();
        String requestBody = String.format("search \"%s\"; %s limit 50;", name, FIELDS_GAME_BASE);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<IgdbGameResponse[]> response = restTemplate.postForEntity(apiConfig.getApiBaseUrl() + GAMES_URL, entity, IgdbGameResponse[].class);
            IgdbGameResponse[] responseBody = response.getBody();

            if (response.getStatusCode() == HttpStatus.OK && responseBody != null) {
                return Arrays.stream(responseBody)
                        .map(this::mapToDomain)
                        .toList();
            }
        } catch (Exception e) {
            logger.error("Error searching for games with name '{}' from IGDB", name, e);
        }

        return Collections.emptyList();
    }

    @Override
    @Cacheable("igdb-games-by-filter")
    public Page<Game> filterGames(String filter, String sort, Integer limit, Integer offset) {
        if (rateLimiterInterrupted()) return Page.empty();
        ensureAuthentication();

        HttpHeaders headers = createHeaders();
        
        long totalElements = fetchTotalCount(filter, headers);
        if (totalElements == 0) {
            return Page.empty();
        }

        if (rateLimiterInterrupted()) return Page.empty();

        List<Game> games = fetchGamesPage(filter, sort, limit, offset, headers);
        
        int pageSize = limit != null ? limit : 50;
        int pageOffset = offset != null ? offset : 0;
        int pageNumber = pageOffset / pageSize;

        return new PageImpl<>(games, PageRequest.of(pageNumber, pageSize), totalElements);
    }

    private long fetchTotalCount(String filter, HttpHeaders headers) {
        String countRequestBody = (filter != null && !filter.isEmpty()) ? "where " + filter + ";" : "";
        HttpEntity<String> countEntity = new HttpEntity<>(countRequestBody, headers);

        try {
            ResponseEntity<IgdbCountResponse> countResponse = restTemplate.postForEntity(apiConfig.getApiBaseUrl() + GAMES_COUNT_URL, countEntity, IgdbCountResponse.class);
            IgdbCountResponse body = countResponse.getBody();
            if (countResponse.getStatusCode() == HttpStatus.OK && body != null) {
                return body.count();
            }
        } catch (Exception e) {
            logger.error("Error fetching game count with filter '{}' from IGDB", filter, e);
        }
        return 0;
    }

    private List<Game> fetchGamesPage(String filter, String sort, Integer limit, Integer offset, HttpHeaders headers) {
        String requestBody = buildFilterQuery(filter, sort, limit, offset);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<IgdbGameResponse[]> response = restTemplate.postForEntity(apiConfig.getApiBaseUrl() + GAMES_URL, entity, IgdbGameResponse[].class);
            IgdbGameResponse[] responseBody = response.getBody();

            if (response.getStatusCode() == HttpStatus.OK && responseBody != null) {
                return Arrays.stream(responseBody)
                        .map(this::mapToDomain)
                        .toList();
            }
        } catch (Exception e) {
            logger.error("Error filtering games with filter '{}' from IGDB", filter, e);
        }
        return Collections.emptyList();
    }

    private String buildFilterQuery(String filter, String sort, Integer limit, Integer offset) {
        StringBuilder requestBodyBuilder = new StringBuilder();
        requestBodyBuilder.append(FIELDS_GAME_BASE);

        if (filter != null && !filter.isEmpty()) {
            requestBodyBuilder.append(" where ").append(filter).append(";");
        }

        if (sort != null && !sort.isEmpty()) {
            requestBodyBuilder.append(" sort ").append(sort).append(";");
        }

        int pageSize = limit != null ? limit : 50;
        int pageOffset = offset != null ? offset : 0;

        requestBodyBuilder.append(" limit ").append(pageSize).append(";");
        requestBodyBuilder.append(" offset ").append(pageOffset).append(";");
        
        return requestBodyBuilder.toString();
    }

    @Override
    @Cacheable("igdb-platforms")
    public List<Platform> listPlatforms() {
        if (rateLimiterInterrupted()) return Collections.emptyList();
        ensureAuthentication();

        HttpHeaders headers = createHeaders();
        String requestBody = "fields name, generation, platform_type; sort name asc; limit 500;";
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<IgdbPlatformResponse[]> response = restTemplate.postForEntity(apiConfig.getApiBaseUrl() + "/platforms", entity, IgdbPlatformResponse[].class);
            IgdbPlatformResponse[] responseBody = response.getBody();

            if (response.getStatusCode() == HttpStatus.OK && responseBody != null) {
                return Arrays.stream(responseBody)
                        .map(this::mapToDomain)
                        .toList();
            }
        } catch (Exception e) {
            logger.error("Error fetching platforms from IGDB", e);
        }

        return Collections.emptyList();
    }

    private boolean rateLimiterInterrupted() {
        try {
            rateLimiter.asBlocking().consume(1);
            return false; // Not interrupted, token consumed
        } catch (InterruptedException e) {
            logger.error(RATE_LIMITER_INTERRUPTION_MESSAGE, e);
            Thread.currentThread().interrupt();
            return true; // Interrupted
        }
    }

    private void ensureAuthentication() {
        if (isTokenInvalid()) {
            authenticate();
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_CLIENT_ID, apiConfig.getClientId());
        headers.set(HEADER_AUTHORIZATION, BEARER_PREFIX + accessToken);
        headers.setContentType(MediaType.TEXT_PLAIN);
        return headers;
    }

    private void authenticate() {
        if (rateLimiterInterrupted()) return;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", apiConfig.getClientId());
        map.add("client_secret", apiConfig.getClientSecret());
        map.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        try {
            AuthResponse response = restTemplate.postForObject(apiConfig.getAuthUrl(), entity, AuthResponse.class);
            if (response != null && response.accessToken() != null) {
                this.accessToken = response.accessToken();
                this.tokenExpirationTime = System.currentTimeMillis() + (response.expiresIn() * 1000);
                logger.info("Successfully authenticated with IGDB/Twitch API.");
            }
        } catch (Exception e) {
            logger.error("Error during authentication with IGDB/Twitch API", e);
        }
    }

    private boolean isTokenInvalid() {
        return accessToken == null || System.currentTimeMillis() >= tokenExpirationTime;
    }

    private Game mapToDomain(IgdbGameResponse igdbGame) {
        List<String> genreNames = igdbGame.genres() != null ? igdbGame.genres().stream().map(IgdbGenreResponse::name).toList() : Collections.emptyList();
        LocalDate releaseDate = igdbGame.releaseDate() != null ? Instant.ofEpochSecond(igdbGame.releaseDate()).atZone(ZoneId.systemDefault()).toLocalDate() : null;
        
        String coverUrl = (igdbGame.cover() != null && igdbGame.cover().imageId() != null)
                ? "https://images.igdb.com/igdb/image/upload/t_cover_big/" + igdbGame.cover().imageId() + ".jpg"
                : PLACEHOLDER_IMAGE_URL;

        List<String> videoUrls = igdbGame.videos() != null
                ? igdbGame.videos().stream()
                    .map(v -> "https://www.youtube.com/watch?v=" + v.videoId())
                    .toList()
                : Collections.emptyList();

        List<String> screenshotUrls = igdbGame.screenshots() != null
                ? igdbGame.screenshots().stream()
                    .map(s -> "https://images.igdb.com/igdb/image/upload/t_screenshot_big/" + s.imageId() + ".jpg")
                    .toList()
                : Collections.emptyList();

        List<String> platformNames = igdbGame.platforms() != null
                ? igdbGame.platforms().stream().map(IgdbPlatformResponse::name).toList()
                : Collections.emptyList();

        return new Game(
                igdbGame.id(),
                igdbGame.name(),
                igdbGame.summary(),
                igdbGame.storyline(),
                releaseDate,
                igdbGame.rating(),
                coverUrl,
                platformNames,
                genreNames,
                videoUrls,
                screenshotUrls,
                igdbGame.artworks()
        );
    }

    private Platform mapToDomain(IgdbPlatformResponse igdbPlatform) {
        return new Platform(
                igdbPlatform.id(),
                igdbPlatform.name(),
                igdbPlatform.generation(),
                PlatformType.fromValue(igdbPlatform.platformType())
        );
    }
}
