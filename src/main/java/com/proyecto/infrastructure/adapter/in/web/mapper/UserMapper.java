package com.proyecto.infrastructure.adapter.in.web.mapper;

import com.proyecto.domain.model.LoginResult;
import com.proyecto.domain.model.User;
import com.proyecto.videogames.generated.model.LoginResponseDTO;
import com.proyecto.videogames.generated.model.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    UserDTO toApiUser(User domainUser);

    @Mapping(target = "token", source = "token")
    @Mapping(target = "userId", source = "user.id")
    LoginResponseDTO toLoginResponse(LoginResult loginResult);
}
