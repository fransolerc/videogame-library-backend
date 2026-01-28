package com.proyecto.application.service;

import com.proyecto.application.port.out.GameProviderPort;
import com.proyecto.domain.model.Game;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceUnitTest {

    @Mock
    private GameProviderPort gameProviderPort;

    @InjectMocks
    private GameService gameService;

    @Test
    void searchGamesByName_ShouldReturnListOfGames() {
        // Arrange
        String query = "Zelda";
        Game game = mock(Game.class);
        List<Game> expectedGames = List.of(game);

        when(gameProviderPort.searchByName(query)).thenReturn(expectedGames);

        // Act
        List<Game> result = gameService.searchGamesByName(query);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedGames, result);
        verify(gameProviderPort).searchByName(query);
    }

    @Test
    void getGameById_ShouldReturnGame_WhenFound() {
        // Arrange
        Long gameId = 123L;
        Game expectedGame = mock(Game.class);
        when(gameProviderPort.findByExternalId(gameId)).thenReturn(Optional.of(expectedGame));

        // Act
        Optional<Game> result = gameService.getGameById(gameId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedGame, result.get());
        verify(gameProviderPort).findByExternalId(gameId);
    }

    @Test
    void getGameById_ShouldReturnEmpty_WhenNotFound() {
        // Arrange
        Long gameId = 999L;
        when(gameProviderPort.findByExternalId(gameId)).thenReturn(Optional.empty());

        // Act
        Optional<Game> result = gameService.getGameById(gameId);

        // Assert
        assertTrue(result.isEmpty());
        verify(gameProviderPort).findByExternalId(gameId);
    }

    @Test
    void filterGames_ShouldReturnListOfGames() {
        // Arrange
        String filter = "rating > 80";
        String sort = "rating desc";
        Integer limit = 10;
        Integer offset = 0;
        
        Game game = mock(Game.class);
        List<Game> expectedGames = List.of(game);

        when(gameProviderPort.filterGames(filter, sort, limit, offset)).thenReturn(expectedGames);

        // Act
        List<Game> result = gameService.filterGames(filter, sort, limit, offset);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedGames, result);
        verify(gameProviderPort).filterGames(filter, sort, limit, offset);
    }
}
