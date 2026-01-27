package com.proyecto.infrastructure.adapter.in.web.controller;

import com.proyecto.domain.model.UserGame;
import com.proyecto.infrastructure.adapter.in.web.mapper.UserGameMapper;
import com.proyecto.application.port.in.AddGameToLibraryUseCase;
import com.proyecto.application.port.in.ListUserLibraryUseCase;
import com.proyecto.videogames.generated.api.LibraryApi;
import com.proyecto.videogames.generated.model.AddGameToLibraryRequestDTO;
import com.proyecto.videogames.generated.model.UserGameDTO;
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
    public ResponseEntity<UserGameDTO> addGameToLibrary(
            @NotNull @PathVariable("userId") UUID userId,
            @Valid @RequestBody AddGameToLibraryRequestDTO addGameToLibraryRequest
    ) {
        UserGame domainUserGame = addGameToLibraryUseCase.addGameToLibrary(
                userId,
                addGameToLibraryRequest.getGameId(),
                userGameMapper.toDomainGameStatus(addGameToLibraryRequest.getStatus())
        );

        return ResponseEntity.ok(userGameMapper.toApiUserGame(domainUserGame));
    }

    @Override
    public ResponseEntity<List<UserGameDTO>> listUserLibrary(@NotNull @PathVariable("userId") UUID userId) {
        List<UserGame> domainUserGames = listUserLibraryUseCase.listUserLibrary(userId);
        return ResponseEntity.ok(userGameMapper.toApiUserGameList(domainUserGames));
    }
}
