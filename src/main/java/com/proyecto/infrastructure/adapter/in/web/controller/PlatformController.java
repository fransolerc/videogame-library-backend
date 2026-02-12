package com.proyecto.infrastructure.adapter.in.web.controller;

import com.proyecto.application.port.in.PlatformServiceInterface;
import com.proyecto.domain.model.Platform;
import com.proyecto.infrastructure.adapter.in.web.mapper.PlatformMapper;
import com.proyecto.videogames.generated.api.PlatformsApi;
import com.proyecto.videogames.generated.model.PlatformDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PlatformController implements PlatformsApi {

    private final PlatformServiceInterface platformServiceInterface;
    private final PlatformMapper platformMapper;

    public PlatformController(PlatformServiceInterface platformServiceInterface, PlatformMapper platformMapper) {
        this.platformServiceInterface = platformServiceInterface;
        this.platformMapper = platformMapper;
    }

    @Override
    public ResponseEntity<List<PlatformDTO>> listPlatforms() {
        List<Platform> domainPlatforms = platformServiceInterface.listPlatforms();
        return ResponseEntity.ok(platformMapper.toApiPlatformList(domainPlatforms));
    }
}
