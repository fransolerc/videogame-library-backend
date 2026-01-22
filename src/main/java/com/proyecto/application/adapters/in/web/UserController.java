package com.proyecto.application.adapters.in.web;

import com.proyecto.application.port.in.RegisterUserUseCase;
import com.proyecto.videogames.generated.api.UsersApi;
import com.proyecto.videogames.generated.model.User;
import com.proyecto.videogames.generated.model.UserRegistrationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController implements UsersApi {

    private final RegisterUserUseCase registerUserUseCase;
    private final UserMapper userMapper;

    public UserController(RegisterUserUseCase registerUserUseCase, UserMapper userMapper) {
        this.registerUserUseCase = registerUserUseCase;
        this.userMapper = userMapper;
    }

    @Override
    public ResponseEntity<User> registerUser(UserRegistrationRequest userRegistrationRequest) {
        com.proyecto.domain.model.User domainUser = registerUserUseCase.registerUser(
                userRegistrationRequest.getUsername(),
                userRegistrationRequest.getEmail(),
                userRegistrationRequest.getPassword()
        );

        User apiUser = userMapper.toApiUser(domainUser);
        return ResponseEntity.ok(apiUser);
    }
}
