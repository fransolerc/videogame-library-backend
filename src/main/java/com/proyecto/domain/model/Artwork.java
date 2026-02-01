package com.proyecto.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an artwork image for a game from the IGDB API.
 *
 * @param id            The unique identifier for the artwork.
 * @param alphaChannel  Whether the image has an alpha channel.
 * @param animated      Whether the image is animated.
 * @param artworkType   The type of the artwork.
 * @param checksum      The checksum of the image.
 * @param game          The ID of the game this artwork is associated with.
 * @param height        The height of the image in pixels.
 * @param imageId       The ID of the image on IGDB's servers.
 * @param url           The URL of the image.
 * @param width         The width of the image in pixels.
 */
public record Artwork(
    Long id,
    @JsonProperty("alpha_channel")
    Boolean alphaChannel,
    Boolean animated,
    @JsonProperty("artwork_type")
    Long artworkType,
    String checksum,
    Long game,
    Integer height,
    @JsonProperty("image_id")
    String imageId,
    String url,
    Integer width
) {
}
