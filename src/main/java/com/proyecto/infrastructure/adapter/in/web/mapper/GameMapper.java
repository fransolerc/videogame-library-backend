package com.proyecto.infrastructure.adapter.in.web.mapper;

import com.proyecto.domain.model.Game;
import com.proyecto.videogames.generated.model.GameDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface GameMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "genres", source = "genres")
    @Mapping(target = "releaseDate", source = "releaseDate")
    @Mapping(target = "summary", source = "summary")
    @Mapping(target = "storyline", source = "storyline")
    @Mapping(target = "rating", expression = "java(mapRating(domainGame.rating()))")
    @Mapping(target = "platforms", source = "platforms")
    @Mapping(target = "coverImageUrl", source = "coverImageUrl")
    @Mapping(target = "videos", source = "videos")
    @Mapping(target = "screenshots", source = "screenshots")
    @Mapping(target = "artworks", source = "artworks")
    GameDTO toApiGame(Game domainGame);

    List<GameDTO> toApiGameList(List<Game> domainGames);

    default URI mapStringToUri(String url) {
        return url != null ? URI.create(url) : null;
    }

    default List<URI> mapStringListToUriList(List<String> urls) {
        if (urls == null) {
            return Collections.emptyList();
        }
        return urls.stream()
                .map(this::mapStringToUri)
                .toList();
    }

    default Double mapRating(Double rating) {
        if (rating == null) {
            return null;
        }
        BigDecimal bd = BigDecimal.valueOf(rating / 10.0);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
