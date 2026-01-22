package com.proyecto.application.port.in;

import com.proyecto.domain.model.User;

public interface RegisterUserUseCase {
    User registerUser(String username, String email, String password);
}
