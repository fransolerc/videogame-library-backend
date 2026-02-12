package com.proyecto.application.port.out.persistence;

import com.proyecto.domain.model.User;
import java.util.Optional;

/**
 * Puerto de salida para la persistencia de usuarios.
 */
public interface UserRepositoryInterface {

    /**
     * Guarda un usuario en el repositorio.
     *
     * @param user El usuario a guardar.
     * @return El usuario guardado.
     */
    User save(User user);

    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param email El correo electrónico a buscar.
     * @return Un Optional con el usuario si se encuentra.
     */
    Optional<User> findByEmail(String email);

}