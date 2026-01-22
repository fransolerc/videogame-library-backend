package com.proyecto.infrastructure.adapter.in.web.controller;

import com.proyecto.infrastructure.adapter.in.web.mapper.UserMapper;
import com.proyecto.application.port.in.LoginUserUseCase;
import com.proyecto.application.port.in.RegisterUserUseCase;
import com.proyecto.videogames.generated.api.UsersApi;
import com.proyecto.videogames.generated.model.LoginRequest;
import com.proyecto.videogames.generated.model.LoginResponse;
import com.proyecto.videogames.generated.model.User;
import com.proyecto.videogames.generated.model.UserRegistrationRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController implements UsersApi {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final UserMapper userMapper;

    public UserController(RegisterUserUseCase registerUserUseCase, LoginUserUseCase loginUserUseCase, UserMapper userMapper) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.userMapper = userMapper;
    }

    @Override
    public ResponseEntity<User> registerUser(@Valid @RequestBody UserRegistrationRequest userRegistrationRequest) {
        com.proyecto.domain.model.User domainUser = registerUserUseCase.registerUser(
                userRegistrationRequest.getUsername(),
                userRegistrationRequest.getEmail(),
                userRegistrationRequest.getPassword()
        );

        User apiUser = userMapper.toApiUser(domainUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiUser);
    }

    @Override
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        return loginUserUseCase.loginUser(loginRequest.getEmail(), loginRequest.getPassword())
                .map(token -> {
                    LoginResponse loginResponse = new LoginResponse();
                    loginResponse.setToken(token);
                    return ResponseEntity.ok(loginResponse);
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}