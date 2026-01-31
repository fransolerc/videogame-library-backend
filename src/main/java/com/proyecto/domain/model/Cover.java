package com.proyecto.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the cover art of a game from the IGDB API.
 *
 * @param id               The unique identifier for the cover.
 * @param alphaChannel     Whether the image has an alpha channel.
 * @param animated         Whether the image is animated.
 * @param checksum         The checksum of the image.
 * @param game             The ID of the game this cover is associated with.
 * @param gameLocalization The ID of the game localization this cover is associated with.
 * @param height           The height of the image in pixels.
 * @param imageId          The ID of the image on IGDB's servers.
 * @param url              The URL of the image.
 * @param width            The width of the image in pixels.
 */
public record Cover(
    Long id,
    @JsonProperty("alpha_channel")
    boolean alphaChannel,
    boolean animated,
    String checksum,
    Long game,
    @JsonProperty("game_localization")
    Long gameLocalization,
    Integer height,
    @JsonProperty("image_id")
    String imageId,
    String url,
    Integer width
) {
}
