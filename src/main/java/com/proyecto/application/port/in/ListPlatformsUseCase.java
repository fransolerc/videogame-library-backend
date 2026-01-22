package com.proyecto.application.port.in;

import com.proyecto.domain.model.Platform;
import java.util.List;

/**
 * Puerto de entrada (use case) para listar las plataformas de videojuegos.
 */
public interface ListPlatformsUseCase {

    /**
     * Obtiene una lista de todas las plataformas de videojuegos.
     *
     * @return Una lista de objetos Platform.
     */
    List<Platform> listPlatforms();
}
