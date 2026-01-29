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
import org.junit.jupiter.api.Nested;
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
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        lenient().when(userDetails.getUsername()).thenReturn(userEmail);
        lenient().when(gameProviderPort.findByExternalId(anyLong())).thenReturn(Optional.of(mock(Game.class)));
        mockUser(userId);
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    class UpsertGameInLibraryTests {
        @Test
        void shouldCreateEntry_whenGameNotInLibrary() {
            when(libraryRepositoryPort.findByUserIdAndGameId(userId.toString(), gameId)).thenReturn(Optional.empty());
            when(libraryRepositoryPort.save(any(UserGame.class))).thenAnswer(i -> i.getArgument(0));

            Optional<UserGame> result = libraryService.upsertGameInLibrary(userId, gameId, GameStatus.PLAYING);

            assertTrue(result.isPresent());
            assertEquals(GameStatus.PLAYING, result.get().status());
            verify(libraryRepositoryPort).save(any(UserGame.class));
        }

        @Test
        void shouldUpdateEntry_whenGameInLibrary() {
            UserGame existing = new UserGame(userId.toString(), gameId, GameStatus.WANT_TO_PLAY, LocalDateTime.now(), false);
            when(libraryRepositoryPort.findByUserIdAndGameId(userId.toString(), gameId)).thenReturn(Optional.of(existing));
            when(libraryRepositoryPort.update(any(UserGame.class))).thenAnswer(i -> i.getArgument(0));

            Optional<UserGame> result = libraryService.upsertGameInLibrary(userId, gameId, GameStatus.COMPLETED);

            assertTrue(result.isPresent());
            assertEquals(GameStatus.COMPLETED, result.get().status());
            verify(libraryRepositoryPort).update(any(UserGame.class));
        }

        @Test
        void shouldDeleteEntry_whenStatusIsNoneAndNotFavorite() {
            UserGame existing = new UserGame(userId.toString(), gameId, GameStatus.PLAYING, LocalDateTime.now(), false);
            when(libraryRepositoryPort.findByUserIdAndGameId(userId.toString(), gameId)).thenReturn(Optional.of(existing));

            Optional<UserGame> result = libraryService.upsertGameInLibrary(userId, gameId, GameStatus.NONE);

            assertTrue(result.isEmpty());
            verify(libraryRepositoryPort).deleteByUserIdAndGameId(userId.toString(), gameId);
        }

        @Test
        void shouldNotDeleteEntry_whenStatusIsNoneButIsFavorite() {
            UserGame existing = new UserGame(userId.toString(), gameId, GameStatus.PLAYING, LocalDateTime.now(), true);
            when(libraryRepositoryPort.findByUserIdAndGameId(userId.toString(), gameId)).thenReturn(Optional.of(existing));
            when(libraryRepositoryPort.update(any(UserGame.class))).thenAnswer(i -> i.getArgument(0));

            Optional<UserGame> result = libraryService.upsertGameInLibrary(userId, gameId, GameStatus.NONE);

            assertTrue(result.isPresent());
            assertEquals(GameStatus.NONE, result.get().status());
            assertTrue(result.get().isFavorite());
            verify(libraryRepositoryPort).update(any(UserGame.class));
        }
    }

    @Nested
    class FavoritesTests {
        @Test
        void addGameToFavorites_shouldCreateEntry_whenNotInLibrary() {
            when(libraryRepositoryPort.findByUserIdAndGameId(userId.toString(), gameId)).thenReturn(Optional.empty());

            libraryService.addGameToFavorites(userId, gameId);

            verify(libraryRepositoryPort).save(argThat(game ->
                    game.status() == GameStatus.NONE && game.isFavorite()
            ));
        }

        @Test
        void addGameToFavorites_shouldUpdateEntry_whenInLibrary() {
            UserGame existing = new UserGame(userId.toString(), gameId, GameStatus.PLAYING, LocalDateTime.now(), false);
            when(libraryRepositoryPort.findByUserIdAndGameId(userId.toString(), gameId)).thenReturn(Optional.of(existing));

            libraryService.addGameToFavorites(userId, gameId);

            verify(libraryRepositoryPort).update(argThat(game ->
                    game.status() == GameStatus.PLAYING && game.isFavorite()
            ));
        }

        @Test
        void removeGameFromFavorites_shouldDeleteEntry_whenStatusIsNone() {
            UserGame existing = new UserGame(userId.toString(), gameId, GameStatus.NONE, LocalDateTime.now(), true);
            when(libraryRepositoryPort.findByUserIdAndGameId(userId.toString(), gameId)).thenReturn(Optional.of(existing));

            libraryService.removeGameFromFavorites(userId, gameId);

            verify(libraryRepositoryPort).deleteByUserIdAndGameId(userId.toString(), gameId);
        }

        @Test
        void removeGameFromFavorites_shouldUpdateEntry_whenStatusIsNotNone() {
            UserGame existing = new UserGame(userId.toString(), gameId, GameStatus.PLAYING, LocalDateTime.now(), true);
            when(libraryRepositoryPort.findByUserIdAndGameId(userId.toString(), gameId)).thenReturn(Optional.of(existing));

            libraryService.removeGameFromFavorites(userId, gameId);

            verify(libraryRepositoryPort).update(argThat(game ->
                    game.status() == GameStatus.PLAYING && !game.isFavorite()
            ));
        }
    }

    @Nested
    class AuthorizationTests {
        @Test
        void shouldThrowException_whenUserIdsDoNotMatch() {
            mockUser(UUID.randomUUID());
            assertThrows(UnauthorizedLibraryAccessException.class, () -> libraryService.listUserLibrary(userId));
        }

        @Test
        void shouldThrowException_whenAuthenticationIsNull() {
            when(securityContext.getAuthentication()).thenReturn(null);
            assertThrows(UnauthorizedLibraryAccessException.class, () -> libraryService.listUserLibrary(userId));
        }

        @Test
        void shouldThrowException_whenNotAuthenticated() {
            when(authentication.isAuthenticated()).thenReturn(false);
            assertThrows(UnauthorizedLibraryAccessException.class, () -> libraryService.listUserLibrary(userId));
        }

        @Test
        void shouldThrowException_whenPrincipalIsNull() {
            when(authentication.getPrincipal()).thenReturn(null);
            assertThrows(UnauthorizedLibraryAccessException.class, () -> libraryService.listUserLibrary(userId));
        }

        @Test
        void shouldThrowException_whenUserNotFoundInDb() {
            when(userRepositoryPort.findByEmail(userEmail)).thenReturn(Optional.empty());
            assertThrows(UnauthorizedLibraryAccessException.class, () -> libraryService.listUserLibrary(userId));
        }

        @Test
        void shouldGetEmail_whenPrincipalIsNotUserDetails() {
            when(authentication.getPrincipal()).thenReturn("user-principal-string");
            User user = new User(userId.toString(), "testuser", "user-principal-string", "password");
            when(userRepositoryPort.findByEmail("user-principal-string")).thenReturn(Optional.of(user));

            assertDoesNotThrow(() -> libraryService.listUserLibrary(userId));
            verify(userRepositoryPort).findByEmail("user-principal-string");
        }
    }

    private void mockUser(UUID authenticatedUserId) {
        User user = new User(authenticatedUserId.toString(), "testuser", userEmail, "password");
        lenient().when(userRepositoryPort.findByEmail(userEmail)).thenReturn(Optional.of(user));
    }
}
