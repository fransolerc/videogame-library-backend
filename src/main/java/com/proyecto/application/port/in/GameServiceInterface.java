package com.proyecto.application.port.in;

import com.proyecto.domain.model.Game;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface GameServiceInterface {

    List<Game> searchGamesByName(String name);

    Optional<Game> getGameById(Long id);

    List<Game> getGamesByIds(List<Long> ids);

    Page<Game> filterGames(String filter, String sort, Integer limit, Integer offset);
}
