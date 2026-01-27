package com.proyecto.application.port.in;

import com.proyecto.domain.model.GameStatus;
import com.proyecto.domain.model.UserGame;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LibraryUseCase {

    UserGame addGameToLibrary(UUID userId, Long gameId, GameStatus status);

    List<UserGame> listUserLibrary(UUID userId);

    Optional<UserGame> getUserGameStatus(UUID userId, Long gameId);
}
