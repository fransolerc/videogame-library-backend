package com.proyecto.infrastructure.adapter.in.web.mapper;

import com.proyecto.domain.model.Game;
import com.proyecto.infrastructure.adapter.in.web.mapper.util.MappingUtils;
import com.proyecto.videogames.generated.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = MappingUtils.class)
public interface GameMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "releaseDate", source = "releaseDate")
    GameDTO toApiGame(Game domainGame);

    List<GameDTO> toApiGameList(List<Game> domainGames);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "platforms", source = "platforms")
    @Mapping(target = "releaseDate", source = "releaseDate")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "coverImageUrl", source = "coverImageUrl")
    @Mapping(target = "summary", source = "summary")
    @Mapping(target = "storyline", source = "storyline")
    @Mapping(target = "videos", source = "videos")
    @Mapping(target = "screenshots", source = "screenshots")
    @Mapping(target = "genres", source = "genres")
    @Mapping(target = "artworks", source = "artworks")
    GameSummaryDTO toApiGameSummary(Game domainGame);

    default GamePageDTO toApiGamePage(Page<Game> domainPage) {
        if (domainPage == null) {
            return null;
        }

        GamePageDTO pageDTO = new GamePageDTO();
        pageDTO.setContent(toApiGameList(domainPage.getContent()));
        pageDTO.setTotalPages(domainPage.getTotalPages());
        pageDTO.setTotalElements(domainPage.getTotalElements());
        pageDTO.setLast(domainPage.isLast());
        pageDTO.setFirst(domainPage.isFirst());
        pageDTO.setSize(domainPage.getSize());
        pageDTO.setNumber(domainPage.getNumber());
        pageDTO.setNumberOfElements(domainPage.getNumberOfElements());
        pageDTO.setEmpty(domainPage.isEmpty());

        PageableDTO pageableDTO = new PageableDTO();
        pageableDTO.setPageNumber(domainPage.getNumber());
        pageableDTO.setPageSize(domainPage.getSize());
        
        if (domainPage.getSort().isSorted()) {
            SortDTO sortDTO = new SortDTO();
            sortDTO.setSorted(domainPage.getSort().isSorted());
            sortDTO.setUnsorted(domainPage.getSort().isUnsorted());
            sortDTO.setEmpty(domainPage.getSort().isEmpty());
            pageableDTO.setSort(sortDTO);
            pageDTO.setSort(sortDTO);
        }

        pageDTO.setPageable(pageableDTO);

        return pageDTO;
    }
}
