package com.proyecto.application.service;

import com.proyecto.application.port.in.AddGameToLibraryUseCase;
import com.proyecto.application.port.in.ListUserLibraryUseCase;
import com.proyecto.application.port.out.GameProviderPort;
import com.proyecto.application.port.out.LibraryRepositoryPort;
import com.proyecto.domain.model.GameStatus;
import com.proyecto.domain.model.UserGame;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LibraryService implements AddGameToLibraryUseCase, ListUserLibraryUseCase {

    private final LibraryRepositoryPort libraryRepositoryPort;
    private final GameProviderPort gameProviderPort;

    public LibraryService(LibraryRepositoryPort libraryRepositoryPort, GameProviderPort gameProviderPort) {
        this.libraryRepositoryPort = libraryRepositoryPort;
        this.gameProviderPort = gameProviderPort;
    }

    @Override
    public UserGame addGameToLibrary(String userId, Long gameId, GameStatus status) {
        // 1. Validar que el juego existe
        gameProviderPort.findByExternalId(gameId)
                .orElseThrow(() -> new RuntimeException("Game with id " + gameId + " not found"));

        // 2. Comprobar si ya existe en la biblioteca
        return libraryRepositoryPort.findByUserIdAndGameId(userId, gameId)
                .map(existingEntry -> {
                    // Si existe, actualiza el estado
                    UserGame updatedEntry = new UserGame(userId, gameId, status, existingEntry.addedAt());
                    return libraryRepositoryPort.save(updatedEntry);
                })
                .orElseGet(() -> {
                    // Si no existe, crea una nueva entrada
                    UserGame newEntry = new UserGame(userId, gameId, status, LocalDateTime.now());
                    return libraryRepositoryPort.save(newEntry);
                });
    }

    @Override
    public List<UserGame> listUserLibrary(String userId) {
        return libraryRepositoryPort.findByUserId(userId);
    }

}
