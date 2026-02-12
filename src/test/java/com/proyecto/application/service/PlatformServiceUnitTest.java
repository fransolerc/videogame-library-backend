package com.proyecto.application.service;

import com.proyecto.application.port.out.provider.PlatformProviderInterface;
import com.proyecto.domain.model.Platform;
import com.proyecto.domain.model.PlatformType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformServiceUnitTest {

    @Mock
    private PlatformProviderInterface platformProviderInterface;

    @InjectMocks
    private PlatformServiceService platformService;

    @ParameterizedTest
    @MethodSource("providePlatformLists")
    void listPlatforms_ShouldReturnListOfPlatforms(List<Platform> expectedPlatforms) {
        // Arrange
        when(platformProviderInterface.listPlatforms()).thenReturn(expectedPlatforms);

        // Act
        List<Platform> result = platformService.listPlatforms();

        // Assert
        assertEquals(expectedPlatforms, result);
        verify(platformProviderInterface).listPlatforms();
    }

    private static Stream<Arguments> providePlatformLists() {
        Platform platform1 = new Platform(1L, "PC", 1, PlatformType.COMPUTER);
        Platform platform2 = new Platform(2L, "PS5", 9, PlatformType.CONSOLE);

        return Stream.of(
                Arguments.of(List.of(platform1, platform2)),
                Arguments.of(Collections.emptyList())
        );
    }
}
