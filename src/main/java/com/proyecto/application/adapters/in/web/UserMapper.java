package com.proyecto.application.adapters.in.web;

import com.proyecto.videogames.generated.model.User;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserMapper {

    public User toApiUser(com.proyecto.domain.model.User domainUser) {
        if (domainUser == null) {
            return null;
        }

        User apiUser = new User();
        if (domainUser.id() != null) {
            apiUser.setId(UUID.fromString(domainUser.id()));
        }
        apiUser.setUsername(domainUser.username());
        apiUser.setEmail(domainUser.email());
        
        return apiUser;
    }
}
