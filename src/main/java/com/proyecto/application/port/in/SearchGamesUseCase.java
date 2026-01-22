package com.proyecto.application.port.in;

import com.proyecto.domain.model.Game;
import java.util.List;
import java.util.Optional;

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

    /**
     * Busca un juego por su ID externo.
     *
     * @param id El ID del juego.
     * @return Un Optional con el juego si se encuentra.
     */
    Optional<Game> getGameById(Long id);

}
