package com.proyecto.infrastructure.adapter.in.web.controller;

import com.proyecto.application.port.in.LibraryUseCase;
import com.proyecto.infrastructure.adapter.in.web.mapper.UserGameMapper;
import com.proyecto.videogames.generated.api.LibraryApi;
import com.proyecto.videogames.generated.model.UpdateGameStatusRequestDTO;
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

    private final LibraryUseCase libraryUseCase;
    private final UserGameMapper userGameMapper;

    public LibraryController(LibraryUseCase libraryUseCase, UserGameMapper userGameMapper) {
        this.libraryUseCase = libraryUseCase;
        this.userGameMapper = userGameMapper;
    }

    @Override
    public ResponseEntity<List<UserGameDTO>> listUserLibrary(@NotNull @PathVariable("userId") UUID userId) {
        return ResponseEntity.ok(userGameMapper.toApiUserGameList(libraryUseCase.listUserLibrary(userId)));
    }

    @Override
    public ResponseEntity<UserGameDTO> getUserGameStatus(
            @PathVariable("userId") UUID userId,
            @PathVariable("gameId") Long gameId
    ) {
        return libraryUseCase.getUserGameStatus(userId, gameId)
                .map(userGameMapper::toApiUserGame)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<UserGameDTO> upsertGameInLibrary(
            @PathVariable("userId") UUID userId,
            @PathVariable("gameId") Long gameId,
            @Valid @RequestBody UpdateGameStatusRequestDTO updateGameStatusRequestDTO
    ) {
        var domainUserGame = libraryUseCase.upsertGameInLibrary(
                userId,
                gameId,
                userGameMapper.toDomainGameStatus(updateGameStatusRequestDTO.getStatus())
        );
        return ResponseEntity.ok(userGameMapper.toApiUserGame(domainUserGame));
    }
}
