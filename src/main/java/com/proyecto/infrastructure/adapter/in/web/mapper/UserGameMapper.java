package com.proyecto.infrastructure.adapter.in.web.mapper;

import com.proyecto.domain.model.GameStatus;
import com.proyecto.domain.model.UserGame;
import com.proyecto.infrastructure.adapter.in.web.mapper.util.MappingUtils;
import com.proyecto.videogames.generated.model.GameStatusDTO;
import com.proyecto.videogames.generated.model.PageableDTO;
import com.proyecto.videogames.generated.model.SortDTO;
import com.proyecto.videogames.generated.model.UserGameDTO;
import com.proyecto.videogames.generated.model.UserGamePageDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = MappingUtils.class)
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
        pageDTO.setTotalElements(page.getTotalElements());
        pageDTO.setNumber(page.getNumber());
        pageDTO.setSize(page.getSize());
        pageDTO.setFirst(page.isFirst());
        pageDTO.setLast(page.isLast());
        pageDTO.setNumberOfElements(page.getNumberOfElements());
        pageDTO.setEmpty(page.isEmpty());

        PageableDTO pageableDTO = new PageableDTO();
        pageableDTO.setPageNumber(page.getPageable().getPageNumber());
        pageableDTO.setPageSize(page.getPageable().getPageSize());

        SortDTO pageableSortDTO = new SortDTO();
        pageableSortDTO.setSorted(page.getPageable().getSort().isSorted());
        pageableSortDTO.setUnsorted(page.getPageable().getSort().isUnsorted());
        pageableSortDTO.setEmpty(page.getPageable().getSort().isEmpty());
        pageableDTO.setSort(pageableSortDTO);
        pageDTO.setPageable(pageableDTO);

        SortDTO topLevelSortDTO = new SortDTO();
        topLevelSortDTO.setSorted(page.getSort().isSorted());
        topLevelSortDTO.setUnsorted(page.getSort().isUnsorted());
        topLevelSortDTO.setEmpty(page.getSort().isEmpty());
        pageDTO.setSort(topLevelSortDTO);

        return pageDTO;
    }
}
