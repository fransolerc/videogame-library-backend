package com.proyecto.domain.model;

/**
 * Representa un usuario en el dominio de la aplicación.
 *
 * @param id       El identificador único del usuario.
 * @param username El nombre de usuario.
 * @param email    El correo electrónico del usuario.
 * @param password La contraseña del usuario (debería estar encriptada).
 */
public record User(
        String id,
        String username,
        String email,
        String password
) {
}
