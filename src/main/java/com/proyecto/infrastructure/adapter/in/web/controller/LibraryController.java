package com.proyecto.infrastructure.adapter.in.web.controller;

import com.proyecto.application.port.in.LibraryUseCase;
import com.proyecto.infrastructure.adapter.in.web.mapper.UserGameMapper;
import com.proyecto.videogames.generated.api.LibraryApi;
import com.proyecto.videogames.generated.model.UpdateGameStatusRequestDTO;
import com.proyecto.videogames.generated.model.UserGameDTO;
import com.proyecto.videogames.generated.model.UserGamePageDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
            @NotNull @PathVariable("userId") UUID userId,
            @NotNull @Min(value = 1L) @PathVariable("gameId") Long gameId
    ) {
        return libraryUseCase.getUserGameStatus(userId, gameId)
                .map(userGameMapper::toApiUserGame)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<UserGameDTO> upsertGameInLibrary(
            @NotNull @PathVariable("userId") UUID userId,
            @NotNull @Min(value = 1L) @PathVariable("gameId") Long gameId,
            @Valid @RequestBody UpdateGameStatusRequestDTO updateGameStatusRequestDTO
    ) {
        return libraryUseCase.upsertGameInLibrary(
                userId,
                gameId,
                userGameMapper.toDomainGameStatus(updateGameStatusRequestDTO.getStatus())
        )
        .map(userGameMapper::toApiUserGame)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.noContent().build());
    }

    @Override
    public ResponseEntity<Void> removeGameFromLibrary(
            @NotNull @PathVariable("userId") UUID userId,
            @NotNull @Min(value = 1L) @PathVariable("gameId") Long gameId
    ) {
        libraryUseCase.removeGameFromLibrary(userId, gameId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> addGameToFavorites(
            @NotNull @PathVariable("userId") UUID userId,
            @NotNull @Min(value = 1L) @PathVariable("gameId") Long gameId
    ) {
        libraryUseCase.addGameToFavorites(userId, gameId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> removeGameFromFavorites(
            @NotNull @PathVariable("userId") UUID userId,
            @NotNull @Min(value = 1L) @PathVariable("gameId") Long gameId
    ) {
        libraryUseCase.removeGameFromFavorites(userId, gameId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<UserGamePageDTO> listFavoriteGames(
            @NotNull @PathVariable("userId") UUID userId,
            @Valid @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @Valid @RequestParam(value = "size", required = false, defaultValue = "20") Integer size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userGameMapper.toApiUserGamePage(libraryUseCase.listFavoriteGames(userId, pageable)));
    }
}
