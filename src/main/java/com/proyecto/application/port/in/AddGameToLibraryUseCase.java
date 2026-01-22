package com.proyecto.application.port.in;

import com.proyecto.domain.model.GameStatus;
import com.proyecto.domain.model.UserGame;

public interface AddGameToLibraryUseCase {
    UserGame addGameToLibrary(String userId, String gameId, GameStatus status);
}
