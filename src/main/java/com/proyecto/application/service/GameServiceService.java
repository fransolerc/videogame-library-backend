package com.proyecto.application.service;

import com.proyecto.application.port.in.GameServiceInterface;
import com.proyecto.application.port.out.provider.GameProviderInterface;
import com.proyecto.domain.model.Game;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GameServiceService implements GameServiceInterface {

    private final GameProviderInterface gameProviderInterface;

    public GameServiceService(GameProviderInterface gameProviderInterface) {
        this.gameProviderInterface = gameProviderInterface;
    }

    @Override
    public List<Game> searchGamesByName(String name) {
        return gameProviderInterface.searchByName(name);
    }

    @Override
    public Optional<Game> getGameById(Long id) {
        return gameProviderInterface.findByExternalId(id);
    }

    @Override
    public List<Game> getGamesByIds(List<Long> ids) {
        return gameProviderInterface.findMultipleByExternalIds(ids);
    }

    @Override
    public Page<Game> filterGames(String filter, String sort, Integer limit, Integer offset) {
        return gameProviderInterface.filterGames(filter, sort, limit, offset);
    }
}
