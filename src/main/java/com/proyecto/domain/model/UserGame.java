package com.proyecto.domain.model;

import java.time.LocalDateTime;

/**
 * Representa un juego en la biblioteca de un usuario.
 *
 * @param userId    El ID del usuario.
 * @param gameId    El ID del juego (IGDB).
 * @param status    El estado del juego (ej. JUGANDO, COMPLETADO).
 * @param addedAt   Fecha y hora en que se añadió a la biblioteca.
 * @param isFavorite Si el juego está marcado como favorito.
 */
public record UserGame(
        String userId,
        Long gameId,
        GameStatus status,
        LocalDateTime addedAt,
        Boolean isFavorite
) {
}
