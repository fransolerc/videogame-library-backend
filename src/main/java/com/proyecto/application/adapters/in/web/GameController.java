package com.proyecto.application.adapters.in.web;

import com.proyecto.application.port.in.SearchGamesUseCase;
import com.proyecto.domain.model.Game;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/games")
public class GameController {

    private final SearchGamesUseCase searchGamesUseCase;

    public GameController(SearchGamesUseCase searchGamesUseCase) {
        this.searchGamesUseCase = searchGamesUseCase;
    }

    @GetMapping("/search")
    public List<Game> searchGames(@RequestParam String name) {
        return searchGamesUseCase.searchGamesByName(name);
    }
}
