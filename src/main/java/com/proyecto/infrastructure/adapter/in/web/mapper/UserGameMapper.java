package com.proyecto.infrastructure.adapter.in.web.mapper;

import com.proyecto.domain.model.GameStatus;
import com.proyecto.domain.model.UserGame;
import com.proyecto.videogames.generated.model.GameStatusDTO;
import com.proyecto.videogames.generated.model.UserGameDTO;
import com.proyecto.videogames.generated.model.UserGamePageDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

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
    @Mapping(target = "isFavorite", source = "isFavorite")
    UserGameDTO toApiUserGame(UserGame domainUserGame);

    List<UserGameDTO> toApiUserGameList(List<UserGame> domainUserGames);

    GameStatusDTO toApiGameStatus(GameStatus status);

    GameStatus toDomainGameStatus(GameStatusDTO apiGameStatus);

    default UserGamePageDTO toApiUserGamePage(Page<UserGame> page) {
        if (page == null) {
            return null;
        }
        UserGamePageDTO pageDTO = new UserGamePageDTO();
        pageDTO.setContent(toApiUserGameList(page.getContent()));
        pageDTO.setTotalPages(page.getTotalPages());
        pageDTO.setTotalElements((int) page.getTotalElements());
        pageDTO.setNumber(page.getNumber());
        pageDTO.setSize(page.getSize());
        pageDTO.setFirst(page.isFirst());
        pageDTO.setLast(page.isLast());
        pageDTO.setNumberOfElements(page.getNumberOfElements());
        pageDTO.setEmpty(page.isEmpty());
        return pageDTO;
    }

    default OffsetDateTime map(LocalDateTime value) {
        return value != null ? value.atOffset(ZoneOffset.UTC) : null;
    }
}
