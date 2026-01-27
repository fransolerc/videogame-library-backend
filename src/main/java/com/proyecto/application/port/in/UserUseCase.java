package com.proyecto.application.port.in;

import com.proyecto.domain.model.LoginResult;
import com.proyecto.domain.model.User;
import java.util.Optional;

public interface UserUseCase {

    User registerUser(String username, String email, String password);

    Optional<LoginResult> loginUser(String email, String password);
}
