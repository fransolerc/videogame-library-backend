package com.proyecto.application.port.out.provider;

import com.proyecto.domain.model.Platform;
import java.util.List;

/**
 * Puerto de salida (driven port) para obtener datos de plataformas desde un proveedor externo.
 */
public interface PlatformProviderInterface {

    /**
     * Obtiene una lista de todas las plataformas de videojuegos desde el proveedor externo.
     *
     * @return Una lista de objetos Platform.
     */
    List<Platform> listPlatforms();
}
