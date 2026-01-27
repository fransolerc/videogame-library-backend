package com.proyecto.infrastructure.adapter.in.web.mapper;

import com.proyecto.domain.model.GameStatus;
import com.proyecto.domain.model.UserGame;
import com.proyecto.videogames.generated.model.GameStatusDTO;
import com.proyecto.videogames.generated.model.UserGameDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserGameMapper {

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "gameId", source = "gameId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "addedAt", source = "addedAt")
    UserGameDTO toApiUserGame(UserGame domainUserGame);

    List<UserGameDTO> toApiUserGameList(List<UserGame> domainUserGames);

    GameStatusDTO toApiGameStatus(GameStatus status);

    GameStatus toDomainGameStatus(GameStatusDTO apiGameStatus);

    default OffsetDateTime map(LocalDateTime value) {
        return value != null ? value.atOffset(ZoneOffset.UTC) : null;
    }
}
