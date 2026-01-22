package com.proyecto.application.port.in;

import java.util.Optional;

/**
 * Puerto de entrada (use case) para el inicio de sesión de usuarios.
 */
public interface LoginUserUseCase {

    /**
     * Intenta autenticar a un usuario con sus credenciales y devuelve un token JWT.
     *
     * @param email La dirección de correo electrónico del usuario.
     * @param password La contraseña del usuario.
     * @return Un Optional que contiene el token JWT si la autenticación es exitosa, o vacío si falla.
     */
    Optional<String> loginUser(String email, String password);
}
