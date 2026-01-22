package com.proyecto.domain.model;

/**
 * Representa una plataforma de videojuegos en el dominio.
 *
 * @param id El ID único de la plataforma (ej. de IGDB).
 * @param name El nombre de la plataforma.
 * @param generation La generación de la plataforma.
 * @param platformType El tipo de plataforma.
 */
public record Platform(Long id, String name, Integer generation, PlatformType platformType) {
}
