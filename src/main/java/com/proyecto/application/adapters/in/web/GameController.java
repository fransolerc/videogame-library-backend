package com.proyecto.application.adapters.in.web;

import com.proyecto.application.port.in.SearchGamesUseCase;
import com.proyecto.videogames.generated.api.GamesApi;
import com.proyecto.videogames.generated.model.Game;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class GameController implements GamesApi {

    private final SearchGamesUseCase searchGamesUseCase;
    private final GameMapper gameMapper;

    public GameController(SearchGamesUseCase searchGamesUseCase, GameMapper gameMapper) {
        this.searchGamesUseCase = searchGamesUseCase;
        this.gameMapper = gameMapper;
    }

    @Override
    public ResponseEntity<List<Game>> searchGamesByName(String name) {
        List<Game> games = searchGamesUseCase.searchGamesByName(name).stream()
                .map(gameMapper::toApiGame)
                .collect(Collectors.toList());
        return ResponseEntity.ok(games);
    }

    @Override
    public ResponseEntity<Game> getGameById(Long id) {
        return searchGamesUseCase.getGameById(id)
                .map(gameMapper::toApiGame)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
