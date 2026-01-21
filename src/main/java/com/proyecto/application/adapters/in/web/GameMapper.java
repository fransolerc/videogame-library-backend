package com.proyecto.application.adapters.in.web;

import com.proyecto.videogames.generated.model.Game;
import org.springframework.stereotype.Component;

import java.net.URI;

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
        
        if (domainGame.coverImageUrl() != null) {
            apiGame.setCoverImageUrl(URI.create(domainGame.coverImageUrl()));
        }
        
        return apiGame;
    }
}
