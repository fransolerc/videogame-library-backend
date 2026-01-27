package com.proyecto.application.port.in;

import com.proyecto.domain.model.UserGame;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada (use case) para listar los juegos de la biblioteca de un usuario.
 */
public interface ListUserLibraryUseCase {

    /**
     * Lista todos los juegos de la biblioteca de un usuario.
     *
     * @param userId El ID del usuario.
     * @return Una lista de UserGame que representan los juegos en la biblioteca del usuario.
     */
    List<UserGame> listUserLibrary(UUID userId);
}
