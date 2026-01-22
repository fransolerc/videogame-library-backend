package com.proyecto.infrastructure.adapter.in.web.mapper;

import com.proyecto.domain.model.Platform;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlatformMapper {

    com.proyecto.videogames.generated.model.Platform toApiPlatform(Platform domainPlatform);
}
