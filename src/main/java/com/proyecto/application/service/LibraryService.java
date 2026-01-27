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
import java.util.UUID;

@Service
public class LibraryService implements AddGameToLibraryUseCase, ListUserLibraryUseCase {

    private final LibraryRepositoryPort libraryRepositoryPort;
    private final GameProviderPort gameProviderPort;

    public LibraryService(LibraryRepositoryPort libraryRepositoryPort, GameProviderPort gameProviderPort) {
        this.libraryRepositoryPort = libraryRepositoryPort;
        this.gameProviderPort = gameProviderPort;
    }

    @Override
    public UserGame addGameToLibrary(UUID userId, Long gameId, GameStatus status) {
        String userIdString = userId.toString();

        gameProviderPort.findByExternalId(gameId)
                .orElseThrow(() -> new RuntimeException("Game with id " + gameId + " not found"));

        return libraryRepositoryPort.findByUserIdAndGameId(userIdString, gameId)
                .map(existingEntry -> {
                    UserGame updatedEntry = new UserGame(userIdString, gameId, status, existingEntry.addedAt());
                    return libraryRepositoryPort.save(updatedEntry);
                })
                .orElseGet(() -> {
                    UserGame newEntry = new UserGame(userIdString, gameId, status, LocalDateTime.now());
                    return libraryRepositoryPort.save(newEntry);
                });
    }

    @Override
    public List<UserGame> listUserLibrary(UUID userId) {
        String userIdString = userId.toString();
        return libraryRepositoryPort.findByUserId(userIdString);
    }
}
