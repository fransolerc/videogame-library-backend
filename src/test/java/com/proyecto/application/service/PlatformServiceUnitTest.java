package com.proyecto.application.service;

import com.proyecto.application.port.out.PlatformProviderPort;
import com.proyecto.domain.model.Platform;
import com.proyecto.domain.model.PlatformType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformServiceUnitTest {

    @Mock
    private PlatformProviderPort platformProviderPort;

    @InjectMocks
    private PlatformService platformService;

    @Test
    void listPlatforms_ShouldReturnListOfPlatforms() {
        // Arrange
        Platform platform1 = new Platform(1L, "PC", 1, PlatformType.COMPUTER);
        Platform platform2 = new Platform(2L, "PS5", 9, PlatformType.CONSOLE);
        List<Platform> expectedPlatforms = List.of(platform1, platform2);

        when(platformProviderPort.listPlatforms()).thenReturn(expectedPlatforms);

        // Act
        List<Platform> result = platformService.listPlatforms();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedPlatforms, result);
        verify(platformProviderPort, times(1)).listPlatforms();
    }

    @Test
    void listPlatforms_ShouldReturnEmptyList_WhenProviderReturnsEmpty() {
        // Arrange
        when(platformProviderPort.listPlatforms()).thenReturn(Collections.emptyList());

        // Act
        List<Platform> result = platformService.listPlatforms();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(platformProviderPort, times(1)).listPlatforms();
    }
}
