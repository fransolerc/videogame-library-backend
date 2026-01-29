package com.proyecto.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento que se publica cuando un usuario marca un juego como favorito.
 *
 * @param userId El ID del usuario que realizó la acción.
 * @param gameId El ID del juego afectado.
 * @param isFavorite El nuevo estado de favorito del juego.
 * @param timestamp La fecha y hora en que ocurrió el evento.
 */
public record FavoriteGameEvent(
        UUID userId,
        Long gameId,
        boolean isFavorite,
        LocalDateTime timestamp
) {
}
