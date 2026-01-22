package com.proyecto.application.port.in;

import com.proyecto.domain.model.Game;
import java.util.List;

/**
 * Puerto de entrada (use case) para filtrar juegos con criterios avanzados.
 */
public interface FilterGamesUseCase {

    /**
     * Filtra juegos utilizando una consulta de filtrado avanzada.
     *
     * @param filter La consulta de filtrado en el formato de la API de IGDB (ej. "genres = (12, 32)").
     * @param sort El campo por el que ordenar los resultados (ej. "total_rating desc").
     * @param limit El número máximo de resultados a devolver.
     * @param offset El número de resultados a saltar para la paginación.
     * @return Una lista de juegos que coinciden con los criterios de filtrado.
     */
    List<Game> filterGames(String filter, String sort, Integer limit, Integer offset);
}
