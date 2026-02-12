package com.proyecto.application.service;

import com.proyecto.application.port.out.event.FavoriteGameEventInterface;
import com.proyecto.application.port.out.provider.GameProviderInterface;
import com.proyecto.application.port.out.persistence.LibraryRepositoryInterface;
import com.proyecto.application.port.out.persistence.UserRepositoryInterface;
import com.proyecto.domain.exception.UnauthorizedLibraryAccessException;
import com.proyecto.domain.model.Game;
import com.proyecto.domain.model.GameStatus;
import com.proyecto.domain.model.User;
import com.proyecto.domain.model.UserGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryServiceUnitTest {

    @Mock
    private LibraryRepositoryInterface libraryRepositoryInterface;
    @Mock
    private GameProviderInterface gameProviderInterface;
    @Mock
    private UserRepositoryInterface userRepositoryInterface;
    @Mock
    private FavoriteGameEventInterface favoriteGameEventInterface;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private LibraryServiceService libraryService;

    private final UUID userId = UUID.randomUUID();
    private final Long gameId = 123L;
    private final String userEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        lenient().when(userDetails.getUsername()).thenReturn(userEmail);
        lenient().when(gameProviderInterface.findByExternalId(anyLong())).thenReturn(Optional.of(mock(Game.class)));
        
        User user = new User(userId.toString(), "testuser", userEmail, "password");
        lenient().when(userRepositoryInterface.findByEmail(userEmail)).thenReturn(Optional.of(user));
        
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    class GeneralLibraryTests {
        @Test
        void listUserLibrary_shouldReturnListOfGames() {
            when(libraryRepositoryInterface.findByUserId(userId.toString())).thenReturn(List.of(mock(UserGame.class)));
            List<UserGame> result = libraryService.listUserLibrary(userId);
            assertFalse(result.isEmpty());
            verify(libraryRepositoryInterface).findByUserId(userId.toString());
        }

        @ParameterizedTest
        @MethodSource("com.proyecto.application.service.LibraryServiceUnitTest#provideUserGame")
        void getUserGameStatus_shouldReturnCorrectOptional(UserGame game) {
            Optional<UserGame> expectedOptional = Optional.ofNullable(game);
            when(libraryRepositoryInterface.findByUserIdAndGameId(userId.toString(), gameId)).thenReturn(expectedOptional);
            
            Optional<UserGame> result = libraryService.getUserGameStatus(userId, gameId);
            
            assertEquals(expectedOptional, result);
        }

        @Test
        void removeGameFromLibrary_shouldCallDelete() {
            libraryService.removeGameFromLibrary(userId, gameId);
            verify(libraryRepositoryInterface).deleteByUserIdAndGameId(userId.toString(), gameId);
        }

        @Test
        void listFavoriteGames_shouldReturnPageOfGames() {
            PageRequest pageable = PageRequest.of(0, 20);
            Page<UserGame> page = new PageImpl<>(List.of(mock(UserGame.class)));
            when(libraryRepositoryInterface.findByUserIdAndIsFavoriteTrue(userId.toString(), pageable))
                    .thenReturn(page);

            Page<UserGame> result = libraryService.listFavoriteGames(userId, pageable);

            assertFalse(result.isEmpty());
            verify(libraryRepositoryInterface).findByUserIdAndIsFavoriteTrue(userId.toString(), pageable);
        }
    }

    @ParameterizedTest
    @MethodSource("provideUpsertGameInLibraryArguments")
    void upsertGameInLibrary_shouldHandleAllCases(UserGame existingEntry, GameStatus newStatus,
                                                  Class<? extends Exception> expectedException,
                                                  GameStatus expectedFinalStatus,
                                                  boolean shouldSave, boolean shouldUpdate, boolean shouldDelete) {
        // Arrange
        when(libraryRepositoryInterface.findByUserIdAndGameId(userId.toString(), gameId)).thenReturn(Optional.ofNullable(existingEntry));
        
        if (shouldSave) {
            when(libraryRepositoryInterface.save(any(UserGame.class))).thenAnswer(i -> i.getArgument(0));
        }
        if (shouldUpdate) {
            when(libraryRepositoryInterface.update(any(UserGame.class))).thenAnswer(i -> i.getArgument(0));
        }

        // Act & Assert
        if (expectedException != null) {
            assertThrows(expectedException, () -> libraryService.upsertGameInLibrary(userId, gameId, newStatus));
        } else {
            Optional<UserGame> result = libraryService.upsertGameInLibrary(userId, gameId, newStatus);
            if (expectedFinalStatus != null) {
                assertTrue(result.isPresent());
                assertEquals(expectedFinalStatus, result.get().status());
            } else {
                assertFalse(result.isPresent());
            }
        }

        verify(libraryRepositoryInterface, times(shouldSave ? 1 : 0)).save(any(UserGame.class));
        verify(libraryRepositoryInterface, times(shouldUpdate ? 1 : 0)).update(any(UserGame.class));
        verify(libraryRepositoryInterface, times(shouldDelete ? 1 : 0)).deleteByUserIdAndGameId(userId.toString(), gameId);
    }

    private static Stream<Arguments> provideUpsertGameInLibraryArguments() {
        UserGame existingFavorite = new UserGame(UUID.randomUUID().toString(), 456L, GameStatus.PLAYING, LocalDateTime.now(), true);
        UserGame existingNotFavorite = new UserGame(UUID.randomUUID().toString(), 789L, GameStatus.WANT_TO_PLAY, LocalDateTime.now(), false);

        return Stream.of(
                // Create new entry
                Arguments.of(null, GameStatus.PLAYING, null, GameStatus.PLAYING, true, false, false),
                // Don't create if status is NONE
                Arguments.of(null, GameStatus.NONE, null, null, false, false, false),
                // Update existing entry
                Arguments.of(existingNotFavorite, GameStatus.COMPLETED, null, GameStatus.COMPLETED, false, true, false),
                // Delete existing entry
                Arguments.of(existingNotFavorite, GameStatus.NONE, null, null, false, false, true),
                // Don't delete if favorite
                Arguments.of(existingFavorite, GameStatus.NONE, null, GameStatus.NONE, false, true, false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideAddGameToFavoritesArguments")
    void addGameToFavorites_shouldHandleAllCases(UserGame existingEntry, GameStatus expectedStatus, boolean expectedFavorite, boolean shouldSave, boolean shouldUpdate) {
        // Arrange
        when(libraryRepositoryInterface.findByUserIdAndGameId(userId.toString(), gameId)).thenReturn(Optional.ofNullable(existingEntry));
        
        if (shouldSave) {
            when(libraryRepositoryInterface.save(any(UserGame.class))).thenAnswer(i -> i.getArgument(0));
        }
        if (shouldUpdate) {
            when(libraryRepositoryInterface.update(any(UserGame.class))).thenAnswer(i -> i.getArgument(0));
        }

        // Act
        UserGame result = libraryService.addGameToFavorites(userId, gameId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedStatus, result.status());
        assertEquals(expectedFavorite, result.isFavorite());
        verify(libraryRepositoryInterface, times(shouldSave ? 1 : 0)).save(any(UserGame.class));
        verify(libraryRepositoryInterface, times(shouldUpdate ? 1 : 0)).update(any(UserGame.class));
        verify(favoriteGameEventInterface).publishFavoriteGameEvent(any());
    }

    private static Stream<Arguments> provideAddGameToFavoritesArguments() {
        UserGame existingNotFavorite = new UserGame(UUID.randomUUID().toString(), 789L, GameStatus.PLAYING, LocalDateTime.now(), false);

        return Stream.of(
                // Create new entry when not in library
                Arguments.of(null, GameStatus.NONE, true, true, false),
                // Update existing entry when in library
                Arguments.of(existingNotFavorite, GameStatus.PLAYING, true, false, true)
        );
    }

    @ParameterizedTest
    @MethodSource("provideRemoveGameFromFavoritesArguments")
    void removeGameFromFavorites_shouldHandleAllCases(UserGame existingEntry, boolean shouldDelete, boolean shouldUpdate, boolean shouldPublishEvent) {
        // Arrange
        when(libraryRepositoryInterface.findByUserIdAndGameId(userId.toString(), gameId)).thenReturn(Optional.of(existingEntry));
        if (shouldUpdate) {
            when(libraryRepositoryInterface.update(any(UserGame.class))).thenAnswer(i -> i.getArgument(0));
        }

        // Act
        libraryService.removeGameFromFavorites(userId, gameId);

        // Assert
        verify(libraryRepositoryInterface, times(shouldDelete ? 1 : 0)).deleteByUserIdAndGameId(userId.toString(), gameId);
        verify(libraryRepositoryInterface, times(shouldUpdate ? 1 : 0)).update(any(UserGame.class));
        verify(favoriteGameEventInterface, times(shouldPublishEvent ? 1 : 0)).publishFavoriteGameEvent(any());
    }

    private static Stream<Arguments> provideRemoveGameFromFavoritesArguments() {
        UserGame existingFavoriteNoneStatus = new UserGame(UUID.randomUUID().toString(), 456L, GameStatus.NONE, LocalDateTime.now(), true);
        UserGame existingFavoritePlayingStatus = new UserGame(UUID.randomUUID().toString(), 789L, GameStatus.PLAYING, LocalDateTime.now(), true);
        UserGame existingNotFavorite = new UserGame(UUID.randomUUID().toString(), 101L, GameStatus.PLAYING, LocalDateTime.now(), false);

        return Stream.of(
                // Delete entry when status is NONE and is favorite -> Should publish event
                Arguments.of(existingFavoriteNoneStatus, true, false, true),
                // Update entry when status is not NONE and is favorite -> Should publish event
                Arguments.of(existingFavoritePlayingStatus, false, true, true),
                // Update entry when status is not NONE and is NOT favorite -> Should NOT publish event
                Arguments.of(existingNotFavorite, false, true, false)
        );
    }
    
    public static Stream<Arguments> provideUserGame() {
        return Stream.of(
                Arguments.of(mock(UserGame.class)),
                Arguments.of((UserGame) null)
        );
    }

    @Nested
    class AuthorizationTests {
        @Test
        void shouldThrowException_whenUserIdsDoNotMatch() {
            User user = new User(UUID.randomUUID().toString(), "testuser", userEmail, "password");
            when(userRepositoryInterface.findByEmail(userEmail)).thenReturn(Optional.of(user));
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
            when(userRepositoryInterface.findByEmail(userEmail)).thenReturn(Optional.empty());
            assertThrows(UnauthorizedLibraryAccessException.class, () -> libraryService.listUserLibrary(userId));
        }

        @Test
        void shouldGetEmail_whenPrincipalIsNotUserDetails() {
            when(authentication.getPrincipal()).thenReturn("user-principal-string");
            User user = new User(userId.toString(), "testuser", "user-principal-string", "password");
            when(userRepositoryInterface.findByEmail("user-principal-string")).thenReturn(Optional.of(user));

            assertDoesNotThrow(() -> libraryService.listUserLibrary(userId));
            verify(userRepositoryInterface).findByEmail("user-principal-string");
        }
    }
}
