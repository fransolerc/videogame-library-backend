package com.proyecto.infrastructure.adapter.in.web.controller;

import com.proyecto.application.port.in.FilterGamesUseCase;
import com.proyecto.domain.model.Game;
import com.proyecto.infrastructure.adapter.in.web.mapper.GameMapper;
import com.proyecto.application.port.in.SearchGamesUseCase;
import com.proyecto.videogames.generated.api.GamesApi;
import com.proyecto.videogames.generated.model.GameDTO;
import com.proyecto.videogames.generated.model.GameFilterRequestDTO;
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
    public ResponseEntity<List<GameDTO>> searchGamesByName(String name) {
        List<Game> domainGames = searchGamesUseCase.searchGamesByName(name);
        return ResponseEntity.ok(gameMapper.toApiGameList(domainGames));
    }

    @Override
    public ResponseEntity<GameDTO> getGameById(Long id) {
        return searchGamesUseCase.getGameById(id)
                .map(gameMapper::toApiGame)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<GameDTO>> filterGames(@Valid @RequestBody GameFilterRequestDTO gameFilterRequest) {
        List<Game> domainGames = filterGamesUseCase.filterGames(
                gameFilterRequest.getFilter(),
                gameFilterRequest.getSort(),
                gameFilterRequest.getLimit(),
                gameFilterRequest.getOffset()
        );
        return ResponseEntity.ok(gameMapper.toApiGameList(domainGames));
    }
}
