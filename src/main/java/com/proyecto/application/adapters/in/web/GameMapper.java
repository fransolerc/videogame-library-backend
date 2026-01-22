package com.proyecto.application.adapters.in.web;

import com.proyecto.videogames.generated.model.Game;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GameMapper {

    public Game toApiGame(com.proyecto.domain.model.Game domainGame) {
        if (domainGame == null) {
            return null;
        }

        Game apiGame = new Game();
        apiGame.setId(domainGame.id());
        apiGame.setName(domainGame.name());
        apiGame.setGenres(domainGame.genres());
        apiGame.setReleaseDate(domainGame.releaseDate());
        apiGame.setSummary(domainGame.summary());
        apiGame.setRating(domainGame.rating());
        apiGame.setPlatforms(domainGame.platforms());
        
        if (domainGame.coverImageUrl() != null) {
            apiGame.setCoverImageUrl(URI.create(domainGame.coverImageUrl()));
        }

        if (domainGame.videos() != null) {
            List<URI> videoUris = domainGame.videos().stream()
                    .map(URI::create)
                    .collect(Collectors.toList());
            apiGame.setVideos(videoUris);
        } else {
            apiGame.setVideos(Collections.emptyList());
        }

        if (domainGame.screenshots() != null) {
            List<URI> screenshotUris = domainGame.screenshots().stream()
                    .map(URI::create)
                    .collect(Collectors.toList());
            apiGame.setScreenshots(screenshotUris);
        } else {
            apiGame.setScreenshots(Collections.emptyList());
        }
        
        return apiGame;
    }
}
