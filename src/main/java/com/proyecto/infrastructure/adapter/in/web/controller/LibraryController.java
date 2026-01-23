package com.proyecto.infrastructure.adapter.in.web.controller;

import com.proyecto.infrastructure.adapter.in.web.mapper.UserGameMapper;
import com.proyecto.application.port.in.AddGameToLibraryUseCase;
import com.proyecto.application.port.in.ListUserLibraryUseCase;
import com.proyecto.videogames.generated.api.LibraryApi;
import com.proyecto.videogames.generated.model.AddGameToLibraryRequest;
import com.proyecto.videogames.generated.model.UserGame;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class LibraryController implements LibraryApi {

    private final AddGameToLibraryUseCase addGameToLibraryUseCase;
    private final ListUserLibraryUseCase listUserLibraryUseCase;
    private final UserGameMapper userGameMapper;

    public LibraryController(AddGameToLibraryUseCase addGameToLibraryUseCase, ListUserLibraryUseCase listUserLibraryUseCase, UserGameMapper userGameMapper) {
        this.addGameToLibraryUseCase = addGameToLibraryUseCase;
        this.listUserLibraryUseCase = listUserLibraryUseCase;
        this.userGameMapper = userGameMapper;
    }

    @Override
    public ResponseEntity<UserGame> addGameToLibrary(
            @NotNull @PathVariable("userId") UUID userId,
            @Valid @RequestBody AddGameToLibraryRequest addGameToLibraryRequest
    ) {
        // Usar String.format para construir la cadena del userId
        String userIdString = String.format("%s", userId);

        com.proyecto.domain.model.UserGame domainUserGame = addGameToLibraryUseCase.addGameToLibrary(
                userIdString,
                addGameToLibraryRequest.getGameId(),
                userGameMapper.toDomainGameStatus(addGameToLibraryRequest.getStatus())
        );

        UserGame apiUserGame = userGameMapper.toApiUserGame(domainUserGame);
        return ResponseEntity.ok(apiUserGame);
    }

    @Override
    public ResponseEntity<List<UserGame>> listUserLibrary(@NotNull @PathVariable("userId") UUID userId) {
        String userIdString = String.format("%s", userId);

        List<com.proyecto.domain.model.UserGame> domainUserGames = listUserLibraryUseCase.listUserLibrary(userIdString);
        List<UserGame> apiUserGames = domainUserGames.stream()
                .map(userGameMapper::toApiUserGame)
                .toList();
        return ResponseEntity.ok(apiUserGames);
    }
}