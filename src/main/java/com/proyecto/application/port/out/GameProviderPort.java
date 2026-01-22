package com.proyecto.application.port.out;

import com.proyecto.domain.model.Game;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (driven port) para obtener datos de juegos desde un proveedor externo.
 */
public interface GameProviderPort {

    /**
     * Busca un juego utilizando el identificador del proveedor externo.
     *
     * @param externalId El ID del juego en el sistema del proveedor (ej. IGDB).
     * @return Un Optional que contiene el juego si se encuentra, o un Optional vacío si no.
     */
    Optional<Game> findByExternalId(Long externalId);

    /**
     * Busca juegos por su nombre en el proveedor externo.
     *
     * @param name El nombre (o parte del nombre) a buscar.
     * @return Una lista de juegos que coinciden con la búsqueda.
     */
    List<Game> searchByName(String name);

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
