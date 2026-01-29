package com.proyecto.application.port.in;

import com.proyecto.domain.model.GameStatus;
import com.proyecto.domain.model.UserGame;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LibraryUseCase {

    Optional<UserGame> upsertGameInLibrary(UUID userId, Long gameId, GameStatus status);

    List<UserGame> listUserLibrary(UUID userId);

    Optional<UserGame> getUserGameStatus(UUID userId, Long gameId);

    void removeGameFromLibrary(UUID userId, Long gameId);

    void addGameToFavorites(UUID userId, Long gameId);

    void removeGameFromFavorites(UUID userId, Long gameId);

    Page<UserGame> listFavoriteGames(UUID userId, Pageable pageable);
}
