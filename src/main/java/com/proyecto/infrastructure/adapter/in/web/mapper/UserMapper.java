package com.proyecto.infrastructure.adapter.in.web.mapper;

import com.proyecto.domain.model.LoginResult;
import com.proyecto.videogames.generated.model.LoginResponse;
import com.proyecto.videogames.generated.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    User toApiUser(com.proyecto.domain.model.User domainUser);

    @Mapping(target = "token", source = "token")
    @Mapping(target = "userId", source = "user.id")
    LoginResponse toLoginResponse(LoginResult loginResult);
}
