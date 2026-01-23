package com.proyecto.infrastructure.adapter.in.web.controller;

import com.proyecto.application.port.in.ListPlatformsUseCase;
import com.proyecto.infrastructure.adapter.in.web.mapper.PlatformMapper;
import com.proyecto.videogames.generated.api.PlatformsApi;
import com.proyecto.videogames.generated.model.Platform;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PlatformController implements PlatformsApi {

    private final ListPlatformsUseCase listPlatformsUseCase;
    private final PlatformMapper platformMapper;

    public PlatformController(ListPlatformsUseCase listPlatformsUseCase, PlatformMapper platformMapper) {
        this.listPlatformsUseCase = listPlatformsUseCase;
        this.platformMapper = platformMapper;
    }

    @Override
    public ResponseEntity<List<Platform>> listPlatforms() {
        List<com.proyecto.domain.model.Platform> domainPlatforms = listPlatformsUseCase.listPlatforms();
        List<Platform> apiPlatforms = domainPlatforms.stream()
                .map(platformMapper::toApiPlatform)
                .toList();
        return ResponseEntity.ok(apiPlatforms);
    }
}
