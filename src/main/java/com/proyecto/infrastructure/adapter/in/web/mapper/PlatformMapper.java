package com.proyecto.infrastructure.adapter.in.web.mapper;

import com.proyecto.domain.model.Platform;
import com.proyecto.videogames.generated.model.PlatformDTO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PlatformMapper {

    PlatformDTO toApiPlatform(Platform domainPlatform);

    List<PlatformDTO> toApiPlatformList(List<Platform> domainPlatforms);
}
