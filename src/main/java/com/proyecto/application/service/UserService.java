package com.proyecto.application.service;

import com.proyecto.application.port.in.RegisterUserUseCase;
import com.proyecto.application.port.out.UserRepositoryPort;
import com.proyecto.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public UserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public User registerUser(String username, String email, String password) {
        // TODO: Aquí deberíamos encriptar la contraseña antes de guardarla
        // TODO: Validar si el email ya existe

        User newUser = new User(UUID.randomUUID().toString(), username, email, password);
        return userRepositoryPort.save(newUser);
    }
}
