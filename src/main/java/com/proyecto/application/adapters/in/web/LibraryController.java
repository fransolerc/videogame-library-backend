package com.proyecto.application.adapters.in.web;

import com.proyecto.application.port.in.AddGameToLibraryUseCase;
import com.proyecto.videogames.generated.api.LibraryApi;
import com.proyecto.videogames.generated.model.AddGameToLibraryRequest;
import com.proyecto.videogames.generated.model.UserGame;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class LibraryController implements LibraryApi {

    private final AddGameToLibraryUseCase addGameToLibraryUseCase;
    private final UserGameMapper userGameMapper;

    public LibraryController(AddGameToLibraryUseCase addGameToLibraryUseCase, UserGameMapper userGameMapper) {
        this.addGameToLibraryUseCase = addGameToLibraryUseCase;
        this.userGameMapper = userGameMapper;
    }

    @Override
    public ResponseEntity<UserGame> addGameToLibrary(
            @NotNull @PathVariable("userId") UUID userId,
            @Valid @RequestBody AddGameToLibraryRequest addGameToLibraryRequest
    ) {
        com.proyecto.domain.model.UserGame domainUserGame = addGameToLibraryUseCase.addGameToLibrary(
                userId.toString(),
                addGameToLibraryRequest.getGameId(),
                userGameMapper.toDomainGameStatus(addGameToLibraryRequest.getStatus())
        );

        UserGame apiUserGame = userGameMapper.toApiUserGame(domainUserGame);
        return ResponseEntity.ok(apiUserGame);
    }
}
