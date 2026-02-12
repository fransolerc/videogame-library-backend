package com.proyecto.infrastructure.adapter.in.web.controller;

import com.proyecto.application.port.in.GameInterface;
import com.proyecto.domain.model.Game;
import com.proyecto.infrastructure.adapter.in.web.mapper.GameMapper;
import com.proyecto.videogames.generated.api.GamesApi;
import com.proyecto.videogames.generated.model.GameDTO;
import com.proyecto.videogames.generated.model.GameFilterRequestDTO;
import com.proyecto.videogames.generated.model.GamePageDTO;
import com.proyecto.videogames.generated.model.GameSummaryDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GameController implements GamesApi {

    private final GameInterface gameInterface;
    private final GameMapper gameMapper;

    public GameController(GameInterface gameInterface, GameMapper gameMapper) {
        this.gameInterface = gameInterface;
        this.gameMapper = gameMapper;
    }

    @Override
    public ResponseEntity<List<GameDTO>> searchGamesByName(String name) {
        List<Game> domainGames = gameInterface.searchGamesByName(name);
        return ResponseEntity.ok(gameMapper.toApiGameList(domainGames));
    }

    @Override
    public ResponseEntity<GameSummaryDTO> getGameById(Long id) {
        return gameInterface.getGameById(id)
                .map(gameMapper::toApiGameSummary)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<GameDTO>> getGamesByIds(@Valid @RequestBody List<Long> ids) {
        List<Game> domainGames = gameInterface.getGamesByIds(ids);
        return ResponseEntity.ok(gameMapper.toApiGameList(domainGames));
    }

    @Override
    public ResponseEntity<GamePageDTO> filterGames(@Valid @RequestBody GameFilterRequestDTO gameFilterRequest) {
        Page<Game> domainPage = gameInterface.filterGames(
                gameFilterRequest.getFilter(),
                gameFilterRequest.getSort(),
                gameFilterRequest.getLimit(),
                gameFilterRequest.getOffset()
        );
        return ResponseEntity.ok(gameMapper.toApiGamePage(domainPage));
    }
}
