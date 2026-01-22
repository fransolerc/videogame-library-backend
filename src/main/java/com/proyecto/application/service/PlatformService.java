package com.proyecto.application.service;

import com.proyecto.application.port.in.ListPlatformsUseCase;
import com.proyecto.application.port.out.PlatformProviderPort;
import com.proyecto.domain.model.Platform;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlatformService implements ListPlatformsUseCase {

    private final PlatformProviderPort platformProviderPort;

    public PlatformService(PlatformProviderPort platformProviderPort) {
        this.platformProviderPort = platformProviderPort;
    }

    @Override
    public List<Platform> listPlatforms() {
        return platformProviderPort.listPlatforms();
    }
}
