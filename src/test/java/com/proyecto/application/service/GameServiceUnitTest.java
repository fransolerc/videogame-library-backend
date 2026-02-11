package com.proyecto.application.service;

import com.proyecto.application.port.out.GameProviderPort;
import com.proyecto.domain.model.Game;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceUnitTest {

    @Mock
    private GameProviderPort gameProviderPort;

    @InjectMocks
    private GameService gameService;

    @ParameterizedTest
    @MethodSource("provideSearchQueriesAndExpectedGames")
    void searchGamesByName_ShouldReturnListOfGames(String query, List<Game> expectedGames) {
        // Arrange
        when(gameProviderPort.searchByName(query)).thenReturn(expectedGames);

        // Act
        List<Game> result = gameService.searchGamesByName(query);

        // Assert
        assertEquals(expectedGames, result);
        verify(gameProviderPort).searchByName(query);
    }

    private static Stream<Arguments> provideSearchQueriesAndExpectedGames() {
        return Stream.of(
                Arguments.of("Zelda", List.of(mock(Game.class))),
                Arguments.of("NonExistentGame", Collections.emptyList())
        );
    }

    @ParameterizedTest
    @MethodSource("provideGameIdAndExpectedGame")
    void getGameById_ShouldReturnGameOrEmpty(Long gameId, Optional<Game> expectedGame) {
        // Arrange
        when(gameProviderPort.findByExternalId(gameId)).thenReturn(expectedGame);

        // Act
        Optional<Game> result = gameService.getGameById(gameId);

        // Assert
        assertEquals(expectedGame, result);
        verify(gameProviderPort).findByExternalId(gameId);
    }

    private static Stream<Arguments> provideGameIdAndExpectedGame() {
        return Stream.of(
                Arguments.of(123L, Optional.of(mock(Game.class))),
                Arguments.of(999L, Optional.empty())
        );
    }

    @ParameterizedTest
    @MethodSource("provideIdsAndExpectedGames")
    void getGamesByIds_ShouldReturnListOfGames(List<Long> ids, List<Game> expectedGames) {
        // Arrange
        when(gameProviderPort.findMultipleByExternalIds(ids)).thenReturn(expectedGames);

        // Act
        List<Game> result = gameService.getGamesByIds(ids);

        // Assert
        assertEquals(expectedGames, result);
        verify(gameProviderPort).findMultipleByExternalIds(ids);
    }

    private static Stream<Arguments> provideIdsAndExpectedGames() {
        return Stream.of(
                Arguments.of(List.of(1L, 2L, 3L), List.of(mock(Game.class), mock(Game.class))),
                Arguments.of(Collections.emptyList(), Collections.emptyList())
        );
    }

    @ParameterizedTest
    @MethodSource("provideFilterSortAndExpectedGames")
    void filterGames_ShouldReturnListOfGames(String filter, String sort, Integer limit, Integer offset, List<Game> expectedGames) {
        // Arrange
        when(gameProviderPort.filterGames(filter, sort, limit, offset)).thenReturn(expectedGames);

        // Act
        List<Game> result = gameService.filterGames(filter, sort, limit, offset);

        // Assert
        assertEquals(expectedGames, result);
        verify(gameProviderPort).filterGames(filter, sort, limit, offset);
    }

    private static Stream<Arguments> provideFilterSortAndExpectedGames() {
        return Stream.of(
                Arguments.of("rating > 80", "rating desc", 10, 0, List.of(mock(Game.class))),
                Arguments.of("genres = (12, 32)", "name asc", 20, 0, List.of(mock(Game.class), mock(Game.class))),
                Arguments.of("platforms = (6)", null, null, null, Collections.emptyList())
        );
    }
}
