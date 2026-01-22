package com.proyecto.application.port.out;

import com.proyecto.domain.model.Platform;
import java.util.List;

/**
 * Puerto de salida (driven port) para obtener datos de plataformas desde un proveedor externo.
 */
public interface PlatformProviderPort {

    /**
     * Obtiene una lista de todas las plataformas de videojuegos desde el proveedor externo.
     *
     * @return Una lista de objetos Platform.
     */
    List<Platform> listPlatforms();
}
