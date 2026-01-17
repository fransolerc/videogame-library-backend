package com.proyecto.application.port.out;

import com.proyecto.domain.model.Game;
import java.util.Optional;

/**
 * Puerto de salida (driven port) para la persistencia de la entidad Game.
 * Este contrato será implementado por un adaptador en la capa de infraestructura.
 */
public interface GameRepositoryPort {

    /**
     * Guarda una entidad Game en el sistema de persistencia.
     *
     * @param game El juego a guardar.
     * @return El juego guardado (puede incluir un ID actualizado o generado).
     */
    Game save(Game game);

    /**
     * Busca un juego por su identificador único.
     *
     * @param id El ID del juego.
     * @return Un Optional que contiene el juego si se encuentra, o un Optional vacío si no.
     */
    Optional<Game> findById(String id);

}