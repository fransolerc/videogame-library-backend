package com.proyecto.infrastructure.provider;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.proyecto.application.port.out.GameProviderPort;
import com.proyecto.application.port.out.PlatformProviderPort;
import com.proyecto.domain.model.Game;
import com.proyecto.domain.model.Platform;
import com.proyecto.domain.model.PlatformType;
import com.proyecto.infrastructure.config.IgdbApiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class IgdbApiAdapter implements GameProviderPort, PlatformProviderPort {

    private static final Logger logger = LoggerFactory.getLogger(IgdbApiAdapter.class);
    private static final String GAMES_URL = "/games";
    private static final String PLACEHOLDER_IMAGE_URL = "https://placehold.co/600x400";
    private static final String HEADER_CLIENT_ID = "Client-ID";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String FIELDS_GAME_BASE = "fields name, genres.name, first_release_date, cover.image_id, summary, videos.video_id, screenshots.image_id, platforms.name, rating;";

    private final IgdbApiConfig apiConfig;
    private final RestTemplate restTemplate;
    private final ExecutorService imageValidationExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private String accessToken;
    private long tokenExpirationTime;
    
    private record AuthResponse(@JsonProperty("access_token") String accessToken, @JsonProperty("expires_in") long expiresIn) {}
    private record IgdbGameResponse(
            long id,
            String name,
            List<IgdbGenreResponse> genres,
            @JsonProperty("first_release_date") Long releaseDate,
            IgdbCoverResponse cover,
            String summary,
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


    public IgdbApiAdapter(IgdbApiConfig apiConfig, RestTemplate restTemplate) {
        this.apiConfig = apiConfig;
        this.restTemplate = restTemplate;
    }

    @Override
    public Optional<Game> findByExternalId(Long externalId) {
        if (isTokenInvalid()) {
            authenticate();
        }

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
    public List<Game> searchByName(String name) {
        if (isTokenInvalid()) {
            authenticate();
        }

        HttpHeaders headers = createHeaders();
        String requestBody = String.format("search \"%s\"; %s limit 20;", name, FIELDS_GAME_BASE);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<IgdbGameResponse[]> response = restTemplate.postForEntity(apiConfig.getApiBaseUrl() + GAMES_URL, entity, IgdbGameResponse[].class);
            IgdbGameResponse[] responseBody = response.getBody();

            if (response.getStatusCode() == HttpStatus.OK && responseBody != null) {
                List<IgdbGameResponse> igdbGames = Arrays.asList(responseBody);
                return processGamesAsync(igdbGames);
            }
        } catch (Exception e) {
            logger.error("Error searching for games with name '{}' from IGDB", name, e);
        }

        return Collections.emptyList();
    }

    @Override
    public List<Game> filterGames(String filter, String sort, Integer limit, Integer offset) {
        if (isTokenInvalid()) {
            authenticate();
        }

        HttpHeaders headers = createHeaders();
        StringBuilder requestBodyBuilder = new StringBuilder();
        requestBodyBuilder.append(FIELDS_GAME_BASE);

        if (filter != null && !filter.isEmpty()) {
            requestBodyBuilder.append(" where ").append(filter).append(";");
        }

        if (sort != null && !sort.isEmpty()) {
            requestBodyBuilder.append(" sort ").append(sort).append(";");
        }

        requestBodyBuilder.append(" limit ").append(limit != null ? limit : 10).append(";");
        requestBodyBuilder.append(" offset ").append(offset != null ? offset : 0).append(";");

        HttpEntity<String> entity = new HttpEntity<>(requestBodyBuilder.toString(), headers);

        try {
            ResponseEntity<IgdbGameResponse[]> response = restTemplate.postForEntity(apiConfig.getApiBaseUrl() + GAMES_URL, entity, IgdbGameResponse[].class);
            IgdbGameResponse[] responseBody = response.getBody();

            if (response.getStatusCode() == HttpStatus.OK && responseBody != null) {
                List<IgdbGameResponse> igdbGames = Arrays.asList(responseBody);
                return processGamesAsync(igdbGames);
            }
        } catch (Exception e) {
            logger.error("Error filtering games with filter '{}' from IGDB", filter, e);
        }

        return Collections.emptyList();
    }

    @Override
    public List<Platform> listPlatforms() {
        if (isTokenInvalid()) {
            authenticate();
        }

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

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_CLIENT_ID, apiConfig.getClientId());
        headers.set(HEADER_AUTHORIZATION, BEARER_PREFIX + accessToken);
        headers.setContentType(MediaType.TEXT_PLAIN);
        return headers;
    }

    private List<Game> processGamesAsync(List<IgdbGameResponse> igdbGames) {
        List<CompletableFuture<Game>> futures = igdbGames.stream()
                .map(igdbGame -> CompletableFuture.supplyAsync(() -> mapToDomain(igdbGame), imageValidationExecutor))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    private void authenticate() {
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
        String coverUrl = validateAndGetCoverUrl(igdbGame.cover());

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
                String.valueOf(igdbGame.id()),
                igdbGame.name(),
                genreNames,
                releaseDate,
                coverUrl,
                igdbGame.summary(),
                videoUrls,
                screenshotUrls,
                platformNames,
                igdbGame.rating()
        );
    }

    private Platform mapToDomain(IgdbPlatformResponse igdbPlatform) {
        return new Platform(igdbPlatform.id(), igdbPlatform.name(), igdbPlatform.generation(), PlatformType.fromValue(igdbPlatform.platformType()));
    }

    private String validateAndGetCoverUrl(IgdbCoverResponse cover) {
        if (cover == null || cover.imageId() == null) {
            return PLACEHOLDER_IMAGE_URL;
        }

        String imageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/" + cover.imageId() + ".jpg";

        try {
            URI uri = new URI(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                logger.warn("Cover image not found (HTTP {}): {}. Using placeholder.", responseCode, imageUrl);
                return PLACEHOLDER_IMAGE_URL;
            }
        } catch (Exception e) {
            logger.error("Error validating cover image URL: {}. Using placeholder.", imageUrl, e);
            return PLACEHOLDER_IMAGE_URL;
        }

        return imageUrl;
    }
}
