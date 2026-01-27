package com.proyecto.application.port.in;

import com.proyecto.domain.model.Game;
import java.util.List;
import java.util.Optional;

public interface GameUseCase {

    List<Game> searchGamesByName(String name);

    Optional<Game> getGameById(Long id);

    List<Game> filterGames(String filter, String sort, Integer limit, Integer offset);
}
