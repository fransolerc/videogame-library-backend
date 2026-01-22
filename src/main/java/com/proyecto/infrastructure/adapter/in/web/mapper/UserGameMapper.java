package com.proyecto.infrastructure.adapter.in.web.mapper;

import com.proyecto.domain.model.GameStatus;
import com.proyecto.videogames.generated.model.UserGame;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface UserGameMapper {

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "gameId", source = "gameId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "addedAt", source = "addedAt")
    UserGame toApiUserGame(com.proyecto.domain.model.UserGame domainUserGame);

    GameStatus toDomainGameStatus(com.proyecto.videogames.generated.model.GameStatus apiGameStatus);

    default OffsetDateTime map(LocalDateTime value) {
        return value != null ? value.atOffset(ZoneOffset.UTC) : null;
    }
}
