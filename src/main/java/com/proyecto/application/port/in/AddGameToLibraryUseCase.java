package com.proyecto.application.port.in;

import com.proyecto.domain.model.GameStatus;
import com.proyecto.domain.model.UserGame;

import java.util.UUID;

public interface AddGameToLibraryUseCase {
    UserGame addGameToLibrary(UUID userId, Long gameId, GameStatus status);
}
