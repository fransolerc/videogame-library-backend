package com.proyecto.application.port.in;

import com.proyecto.domain.model.Game;
import java.util.List;

/**
 * Puerto de entrada (use case) para buscar videojuegos.
 */
public interface SearchGamesUseCase {

    /**
     * Busca juegos por su nombre.
     *
     * @param name El nombre a buscar.
     * @return Una lista de juegos encontrados.
     */
    List<Game> searchGamesByName(String name);

}
