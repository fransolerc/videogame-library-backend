package com.proyecto.infrastructure.provider;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.proyecto.application.port.out.GameProviderPort;
import com.proyecto.domain.model.Game;
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
import java.util.stream.Collectors;

@Component
public class IgdbApiAdapter implements GameProviderPort {

    private static final Logger logger = LoggerFactory.getLogger(IgdbApiAdapter.class);
    private static final String PLACEHOLDER_IMAGE_URL = "https://placehold.co/600x400";

    private final IgdbApiConfig apiConfig;
    private final RestTemplate restTemplate;
    private final ExecutorService imageValidationExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private String accessToken;
    private long tokenExpirationTime;

    // DTOs para la API de IGDB
    private record AuthResponse(@JsonProperty("access_token") String accessToken, @JsonProperty("expires_in") long expiresIn) {}
    private record IgdbGameResponse(long id, String name, List<IgdbGenreResponse> genres, @JsonProperty("first_release_date") Long releaseDate, IgdbCoverResponse cover) {}
    private record IgdbGenreResponse(String name) {}
    private record IgdbCoverResponse(@JsonProperty("image_id") String imageId) {}


    public IgdbApiAdapter(IgdbApiConfig apiConfig, RestTemplate restTemplate) {
        this.apiConfig = apiConfig;
        this.restTemplate = restTemplate;
    }

    @Override
    public Optional<Game> findByExternalId(String externalId) {
        if (isTokenInvalid()) {
            authenticate();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Client-ID", apiConfig.getClientId());
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.TEXT_PLAIN);

        String requestBody = String.format("fields name, genres.name, first_release_date, cover.image_id; where id = %s;", externalId);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<IgdbGameResponse[]> response = restTemplate.postForEntity(apiConfig.getApiBaseUrl() + "/games", entity, IgdbGameResponse[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().length > 0) {
                return Optional.of(mapToDomain(response.getBody()[0]));
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

        HttpHeaders headers = new HttpHeaders();
        headers.set("Client-ID", apiConfig.getClientId());
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.TEXT_PLAIN);

        String requestBody = String.format("search \"%s\"; fields name, genres.name, first_release_date, cover.image_id; limit 20;", name);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<IgdbGameResponse[]> response = restTemplate.postForEntity(apiConfig.getApiBaseUrl() + "/games", entity, IgdbGameResponse[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<IgdbGameResponse> igdbGames = Arrays.asList(response.getBody());

                List<CompletableFuture<Game>> futures = igdbGames.stream()
                        .map(igdbGame -> CompletableFuture.supplyAsync(() -> mapToDomain(igdbGame), imageValidationExecutor))
                        .toList();

                return futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Error searching for games with name '{}' from IGDB", name, e);
        }

        return Collections.emptyList();
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

        return new Game(
                String.valueOf(igdbGame.id()),
                igdbGame.name(),
                genreNames,
                releaseDate,
                coverUrl
        );
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
            connection.setConnectTimeout(2000); // 2 segundos de timeout
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
