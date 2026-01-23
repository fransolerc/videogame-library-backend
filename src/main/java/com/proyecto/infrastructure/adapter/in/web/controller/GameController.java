package com.proyecto.infrastructure.adapter.in.web.controller;

import com.proyecto.application.port.in.FilterGamesUseCase;
import com.proyecto.infrastructure.adapter.in.web.mapper.GameMapper;
import com.proyecto.application.port.in.SearchGamesUseCase;
import com.proyecto.videogames.generated.api.GamesApi;
import com.proyecto.videogames.generated.model.Game;
import com.proyecto.videogames.generated.model.GameFilterRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GameController implements GamesApi {

    private final SearchGamesUseCase searchGamesUseCase;
    private final FilterGamesUseCase filterGamesUseCase;
    private final GameMapper gameMapper;

    public GameController(SearchGamesUseCase searchGamesUseCase, FilterGamesUseCase filterGamesUseCase, GameMapper gameMapper) {
        this.searchGamesUseCase = searchGamesUseCase;
        this.filterGamesUseCase = filterGamesUseCase;
        this.gameMapper = gameMapper;
    }

    @Override
    public ResponseEntity<List<Game>> searchGamesByName(String name) {
        List<Game> games = searchGamesUseCase.searchGamesByName(name).stream()
                .map(gameMapper::toApiGame)
                .toList();
        return ResponseEntity.ok(games);
    }

    @Override
    public ResponseEntity<Game> getGameById(Long id) {
        return searchGamesUseCase.getGameById(id)
                .map(gameMapper::toApiGame)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<Game>> filterGames(@Valid @RequestBody GameFilterRequest gameFilterRequest) {
        List<com.proyecto.domain.model.Game> domainGames = filterGamesUseCase.filterGames(
                gameFilterRequest.getFilter(),
                gameFilterRequest.getSort(),
                gameFilterRequest.getLimit(),
                gameFilterRequest.getOffset()
        );

        List<Game> apiGames = domainGames.stream()
                .map(gameMapper::toApiGame)
                .toList();

        return ResponseEntity.ok(apiGames);
    }
}
