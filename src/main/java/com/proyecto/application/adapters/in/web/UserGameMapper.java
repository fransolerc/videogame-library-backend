package com.proyecto.application.adapters.in.web;

import com.proyecto.domain.model.GameStatus;
import com.proyecto.videogames.generated.model.UserGame;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class UserGameMapper {

    public UserGame toApiUserGame(com.proyecto.domain.model.UserGame domainUserGame) {
        if (domainUserGame == null) {
            return null;
        }

        UserGame apiUserGame = new UserGame();
        apiUserGame.setUserId(UUID.fromString(domainUserGame.userId()));
        apiUserGame.setGameId(domainUserGame.gameId());
        apiUserGame.setStatus(com.proyecto.videogames.generated.model.GameStatus.fromValue(domainUserGame.status().name()));
        apiUserGame.setAddedAt(OffsetDateTime.of(domainUserGame.addedAt(), ZoneOffset.UTC)); // Asumiendo UTC para la API

        return apiUserGame;
    }

    public GameStatus toDomainGameStatus(com.proyecto.videogames.generated.model.GameStatus apiGameStatus) {
        if (apiGameStatus == null) {
            return null;
        }
        return GameStatus.valueOf(apiGameStatus.name());
    }
}
