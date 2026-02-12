package com.proyecto.application.service;

import com.proyecto.application.port.in.PlatformServiceInterface; // Importar PlatformUseCase
import com.proyecto.application.port.out.provider.PlatformProviderInterface;
import com.proyecto.domain.model.Platform;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlatformServiceService implements PlatformServiceInterface {

    private final PlatformProviderInterface platformProviderInterface;

    public PlatformServiceService(PlatformProviderInterface platformProviderInterface) {
        this.platformProviderInterface = platformProviderInterface;
    }

    @Override
    public List<Platform> listPlatforms() {
        return platformProviderInterface.listPlatforms();
    }
}
