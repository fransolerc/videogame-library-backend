package com.proyecto.application.port.out;

import com.proyecto.domain.event.FavoriteGameEvent;

/**
 * Puerto de salida para publicar eventos relacionados con juegos favoritos.
 */
public interface FavoriteGameEventPort {

    /**
     * Publica un evento de juego favorito.
     *
     * @param event El evento a publicar.
     */
    void publishFavoriteGameEvent(FavoriteGameEvent event);
}
