package com.proyecto.infrastructure.adapter.in.web.controller;

import com.proyecto.application.port.in.LoginUserUseCase;
import com.proyecto.application.port.in.RegisterUserUseCase;
import com.proyecto.domain.model.User;
import com.proyecto.infrastructure.adapter.in.web.mapper.UserMapper;
import com.proyecto.videogames.generated.api.UsersApi;
import com.proyecto.videogames.generated.model.LoginRequestDTO;
import com.proyecto.videogames.generated.model.LoginResponseDTO;
import com.proyecto.videogames.generated.model.UserDTO;
import com.proyecto.videogames.generated.model.UserRegistrationRequestDTO;
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
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserRegistrationRequestDTO userRegistrationRequest) {
        User domainUser = registerUserUseCase.registerUser(
                userRegistrationRequest.getUsername(),
                userRegistrationRequest.getEmail(),
                userRegistrationRequest.getPassword()
        );

        UserDTO apiUser = userMapper.toApiUser(domainUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiUser);
    }

    @Override
    public ResponseEntity<LoginResponseDTO> loginUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        return loginUserUseCase.loginUser(loginRequest.getEmail(), loginRequest.getPassword())
                .map(userMapper::toLoginResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
