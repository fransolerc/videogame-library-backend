package com.proyecto.infrastructure.adapter.in.web.mapper;

import com.proyecto.domain.model.Game;
import com.proyecto.infrastructure.adapter.in.web.mapper.util.MappingUtils;
import com.proyecto.videogames.generated.model.GameDTO;
import com.proyecto.videogames.generated.model.GameSummaryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = MappingUtils.class)
public interface GameMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "genres", source = "genres")
    @Mapping(target = "releaseDate", source = "releaseDate")
    @Mapping(target = "summary", source = "summary")
    @Mapping(target = "storyline", source = "storyline")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "coverImageUrl", source = "coverImageUrl")
    @Mapping(target = "videos", source = "videos")
    @Mapping(target = "screenshots", source = "screenshots")
    @Mapping(target = "artworks", source = "artworks")
    GameDTO toApiGame(Game domainGame);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "platforms", source = "platforms")
    @Mapping(target = "releaseDate", source = "releaseDate")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "coverImageUrl", source = "coverImageUrl")
    GameSummaryDTO toApiGameSummary(Game domainGame);

    List<GameSummaryDTO> toApiGameSummaryList(List<Game> domainGames);
}
