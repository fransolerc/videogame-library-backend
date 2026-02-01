package com.proyecto.infrastructure.adapter.in.web.controller;

import com.proyecto.application.port.in.GameUseCase;
import com.proyecto.domain.model.Game;
import com.proyecto.infrastructure.adapter.in.web.mapper.GameMapper;
import com.proyecto.videogames.generated.api.GamesApi;
import com.proyecto.videogames.generated.model.GameDTO;
import com.proyecto.videogames.generated.model.GameFilterRequestDTO;
import com.proyecto.videogames.generated.model.GameSummaryDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GameController implements GamesApi {

    private final GameUseCase gameUseCase;
    private final GameMapper gameMapper;

    public GameController(GameUseCase gameUseCase, GameMapper gameMapper) {
        this.gameUseCase = gameUseCase;
        this.gameMapper = gameMapper;
    }

    @Override
    public ResponseEntity<List<GameSummaryDTO>> searchGamesByName(String name) {
        List<Game> domainGames = gameUseCase.searchGamesByName(name);
        return ResponseEntity.ok(gameMapper.toApiGameSummaryList(domainGames));
    }

    @Override
    public ResponseEntity<GameDTO> getGameById(Long id) {
        return gameUseCase.getGameById(id)
                .map(gameMapper::toApiGame)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<GameSummaryDTO>> filterGames(@Valid @RequestBody GameFilterRequestDTO gameFilterRequest) {
        List<Game> domainGames = gameUseCase.filterGames(
                gameFilterRequest.getFilter(),
                gameFilterRequest.getSort(),
                gameFilterRequest.getLimit(),
                gameFilterRequest.getOffset()
        );
        return ResponseEntity.ok(gameMapper.toApiGameSummaryList(domainGames));
    }
}
