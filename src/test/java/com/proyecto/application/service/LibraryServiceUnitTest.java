package com.proyecto.application.service;

import com.proyecto.application.port.out.GameProviderPort;
import com.proyecto.application.port.out.LibraryRepositoryPort;
import com.proyecto.application.port.out.UserRepositoryPort;
import com.proyecto.domain.exception.UnauthorizedLibraryAccessException;
import com.proyecto.domain.model.Game;
import com.proyecto.domain.model.GameStatus;
import com.proyecto.domain.model.User;
import com.proyecto.domain.model.UserGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryServiceUnitTest {

    @Mock
    private LibraryRepositoryPort libraryRepositoryPort;

    @Mock
    private GameProviderPort gameProviderPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private LibraryService libraryService;

    private final UUID userId = UUID.randomUUID();
    private final Long gameId = 123L;
    private final String userEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void upsertGameInLibrary_ShouldAddNewGame_WhenGameDoesNotExistInLibrary() {
        // Arrange
        mockAuthentication();
        mockUser(userId);

        when(gameProviderPort.findByExternalId(gameId)).thenReturn(Optional.of(mock(Game.class)));
        when(libraryRepositoryPort.findByUserIdAndGameId(userId.toString(), gameId)).thenReturn(Optional.empty());
        
        UserGame savedUserGame = new UserGame(userId.toString(), gameId, GameStatus.PLAYING, LocalDateTime.now());
        when(libraryRepositoryPort.save(any(UserGame.class))).thenReturn(savedUserGame);

        // Act
        UserGame result = libraryService.upsertGameInLibrary(userId, gameId, GameStatus.PLAYING);

        // Assert
        assertNotNull(result);
        assertEquals(userId.toString(), result.userId());
        assertEquals(gameId, result.gameId());
        assertEquals(GameStatus.PLAYING, result.status());

        verify(libraryRepositoryPort).save(any(UserGame.class));
        verify(libraryRepositoryPort, never()).update(any(UserGame.class));
    }

    @Test
    void upsertGameInLibrary_ShouldUpdateGame_WhenGameExistsInLibrary() {
        // Arrange
        mockAuthentication();
        mockUser(userId);

        when(gameProviderPort.findByExternalId(gameId)).thenReturn(Optional.of(mock(Game.class)));
        
        UserGame existingUserGame = new UserGame(userId.toString(), gameId, GameStatus.WANT_TO_PLAY, LocalDateTime.now());
        when(libraryRepositoryPort.findByUserIdAndGameId(userId.toString(), gameId)).thenReturn(Optional.of(existingUserGame));

        UserGame updatedUserGame = new UserGame(userId.toString(), gameId, GameStatus.COMPLETED, existingUserGame.addedAt());
        when(libraryRepositoryPort.update(any(UserGame.class))).thenReturn(updatedUserGame);

        // Act
        UserGame result = libraryService.upsertGameInLibrary(userId, gameId, GameStatus.COMPLETED);

        // Assert
        assertNotNull(result);
        assertEquals(GameStatus.COMPLETED, result.status());

        verify(libraryRepositoryPort).update(any(UserGame.class));
        verify(libraryRepositoryPort, never()).save(any(UserGame.class));
    }

    @Test
    void upsertGameInLibrary_ShouldThrowUnauthorizedException_WhenUserIdsDoNotMatch() {
        // Arrange
        UUID differentUserId = UUID.randomUUID();
        mockAuthentication();
        mockUser(differentUserId);

        // Act & Assert
        UnauthorizedLibraryAccessException thrown = assertThrows(
                UnauthorizedLibraryAccessException.class,
                () -> libraryService.upsertGameInLibrary(userId, gameId, GameStatus.PLAYING)
        );

        assertTrue(thrown.getMessage().contains("is not authorized to access library of user"));
        verify(gameProviderPort, never()).findByExternalId(anyLong());
        verify(libraryRepositoryPort, never()).findByUserIdAndGameId(anyString(), anyLong());
    }

    @Test
    void removeGameFromLibrary_ShouldCallRepositoryDelete_WhenAuthorized() {
        // Arrange
        mockAuthentication();
        mockUser(userId);

        // Act
        libraryService.removeGameFromLibrary(userId, gameId);

        // Assert
        verify(libraryRepositoryPort, times(1)).deleteByUserIdAndGameId(userId.toString(), gameId);
    }

    @Test
    void removeGameFromLibrary_ShouldThrowUnauthorizedException_WhenUserIdsDoNotMatch() {
        // Arrange
        UUID differentUserId = UUID.randomUUID();
        mockAuthentication();
        mockUser(differentUserId);

        // Act & Assert
        UnauthorizedLibraryAccessException thrown = assertThrows(
                UnauthorizedLibraryAccessException.class,
                () -> libraryService.removeGameFromLibrary(userId, gameId)
        );

        assertTrue(thrown.getMessage().contains("is not authorized to access library of user"));
        verify(libraryRepositoryPort, never()).deleteByUserIdAndGameId(anyString(), anyLong());
    }

    private void mockAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(userEmail);
    }

    private void mockUser(UUID authenticatedUserId) {
        User user = new User(authenticatedUserId.toString(), "testuser", userEmail, "password");
        when(userRepositoryPort.findByEmail(userEmail)).thenReturn(Optional.of(user));
    }
}
