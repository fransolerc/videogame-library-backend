package com.proyecto.infrastructure.adapter.in.web.controller;

import com.proyecto.application.port.in.UserUseCase;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
public class UserController implements UsersApi {

    private final UserUseCase userUseCase;
    private final UserMapper userMapper;

    public UserController(UserUseCase userUseCase, UserMapper userMapper) {
        this.userUseCase = userUseCase;
        this.userMapper = userMapper;
    }

    @Override
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserRegistrationRequestDTO userRegistrationRequest) {
        User domainUser = userUseCase.registerUser(
                userRegistrationRequest.getUsername(),
                userRegistrationRequest.getEmail(),
                userRegistrationRequest.getPassword()
        );

        UserDTO apiUser = userMapper.toApiUser(domainUser);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(apiUser.getId())
                .toUri();

        return ResponseEntity.created(location).body(apiUser);
    }

    @Override
    public ResponseEntity<LoginResponseDTO> loginUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        return userUseCase.loginUser(loginRequest.getEmail(), loginRequest.getPassword())
                .map(userMapper::toLoginResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
