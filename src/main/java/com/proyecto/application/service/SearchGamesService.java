package com.proyecto.application.service;

import com.proyecto.application.port.in.FilterGamesUseCase;
import com.proyecto.application.port.in.SearchGamesUseCase;
import com.proyecto.application.port.out.GameProviderPort;
import com.proyecto.domain.model.Game;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SearchGamesService implements SearchGamesUseCase, FilterGamesUseCase {

    private final GameProviderPort gameProviderPort;

    public SearchGamesService(GameProviderPort gameProviderPort) {
        this.gameProviderPort = gameProviderPort;
    }

    @Override
    public List<Game> searchGamesByName(String name) {
        return gameProviderPort.searchByName(name);
    }

    @Override
    public Optional<Game> getGameById(Long id) {
        return gameProviderPort.findByExternalId(id);
    }

    @Override
    public List<Game> filterGames(String filter, String sort, Integer limit, Integer offset) {
        return gameProviderPort.filterGames(filter, sort, limit, offset);
    }
}
