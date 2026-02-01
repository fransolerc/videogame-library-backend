package com.proyecto.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

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
 * @param summary       El resumen detallado del juego.
 * @param storyline     A short description of a games story.
 * @param videos        Lista de URLs de videos relacionados (trailers, gameplays).
 * @param screenshots   Lista de URLs de capturas de pantalla.
 * @param platforms     Lista de nombres de plataformas donde está disponible.
 * @param rating        Puntuación media de los usuarios (0-100).
 * @param artworks      Una lista de artworks asociados al juego.
 */
public record Game(
        Long id,
        String name,
        List<String> genres,
        @JsonProperty("release_date")
        LocalDate releaseDate,
        String coverImageUrl,
        String summary,
        String storyline,
        List<String> videos,
        List<String> screenshots,
        List<String> platforms,
        Double rating,
        List<Artwork> artworks
) {
}
