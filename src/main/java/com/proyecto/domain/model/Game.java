package com.proyecto.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

/**
 * Representa un videojuego en el dominio de la aplicación.
 *
 * @param id            El identificador único del juego.
 * @param name          El nombre del juego.
 * @param summary       El resumen detallado del juego.
 * @param storyline     Una breve descripción de la historia del juego.
 * @param releaseDate   La fecha de lanzamiento.
 * @param rating        Puntuación media de los usuarios (0-100).
 * @param coverImageUrl La URL de la imagen de portada.
 * @param platforms     Lista de nombres de plataformas donde está disponible.
 * @param genres        Una lista de géneros a los que pertenece el juego.
 * @param videos        Lista de URLs de videos relacionados (trailers, gameplays).
 * @param screenshots   Lista de URLs de capturas de pantalla.
 * @param artworks      Una lista de artworks asociados al juego.
 */
public record Game(
        Long id,
        String name,
        String summary,
        String storyline,
        @JsonProperty("release_date")
        LocalDate releaseDate,
        Double rating,
        String coverImageUrl,
        List<String> platforms,
        List<String> genres,
        List<String> videos,
        List<String> screenshots,
        List<Artwork> artworks
) {
}
