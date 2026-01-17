package com.proyecto.domain.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Representa un videojuego en el dominio de la aplicación.
 *
 * @param id            El identificador único del juego (puede ser de una API externa o un UUID).
 * @param name          El nombre del juego.
 * @param genres        Una lista de géneros a los que pertenece el juego.
 * @param releaseDate   La fecha de lanzamiento.
 * @param coverImageUrl La URL de la imagen de portada.
 */
public record Game(
        String id,
        String name,
        List<String> genres,
        LocalDate releaseDate,
        String coverImageUrl
) {
}
