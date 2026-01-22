package com.proyecto.infrastructure.adapter.in.web.mapper;

import com.proyecto.videogames.generated.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    User toApiUser(com.proyecto.domain.model.User domainUser);
}
