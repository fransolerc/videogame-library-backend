package com.proyecto.infrastructure.adapter.in.web.mapper;

import com.proyecto.videogames.generated.model.Game;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.net.URI;
import java.util.List;

@Mapper(componentModel = "spring")
public interface GameMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "genres", source = "genres")
    @Mapping(target = "releaseDate", source = "releaseDate")
    @Mapping(target = "summary", source = "summary")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "platforms", source = "platforms")
    @Mapping(target = "coverImageUrl", source = "coverImageUrl")
    @Mapping(target = "videos", source = "videos")
    @Mapping(target = "screenshots", source = "screenshots")
    Game toApiGame(com.proyecto.domain.model.Game domainGame);

    default URI mapStringToUri(String url) {
        return url != null ? URI.create(url) : null;
    }

    default List<URI> mapStringListToUriList(List<String> urls) {
        if (urls == null) {
            return null;
        }
        return urls.stream()
                .map(this::mapStringToUri)
                .collect(java.util.stream.Collectors.toList());
    }
}