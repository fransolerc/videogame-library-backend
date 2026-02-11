package com.proyecto.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento que se publica cuando un usuario marca o desmarca un juego como favorito.
 *
 * @param gameId El ID del juego afectado.
 * @param userId El ID del usuario que realizó la acción.
 * @param isFavorite El nuevo estado de favorito del juego (true si es favorito, false si no lo es).
 * @param timestamp La fecha y hora en que ocurrió el evento.
 */
public record FavoriteGameEvent(
        Long gameId,
        UUID userId,
        boolean isFavorite,
        LocalDateTime timestamp
) {
}
