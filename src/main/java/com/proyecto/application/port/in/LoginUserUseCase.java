package com.proyecto.application.port.in;

import com.proyecto.domain.model.LoginResult;
import java.util.Optional;

/**
 * Puerto de entrada (use case) para el inicio de sesión de usuarios.
 */
public interface LoginUserUseCase {

    /**
     * Intenta autenticar a un usuario con sus credenciales y devuelve un resultado de login con el token y el usuario.
     *
     * @param email La dirección de correo electrónico del usuario.
     * @param password La contraseña del usuario.
     * @return Un Optional que contiene el LoginResult si la autenticación es exitosa, o vacío si falla.
     */
    Optional<LoginResult> loginUser(String email, String password);
}
