package com.proyecto.application.service;

import com.proyecto.application.port.in.LibraryUseCase;
import com.proyecto.application.port.out.GameProviderPort;
import com.proyecto.application.port.out.LibraryRepositoryPort;
import com.proyecto.application.port.out.UserRepositoryPort;
import com.proyecto.domain.exception.UnauthorizedLibraryAccessException;
import com.proyecto.domain.model.GameStatus;
import com.proyecto.domain.model.UserGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LibraryService implements LibraryUseCase {

    private static final Logger logger = LoggerFactory.getLogger(LibraryService.class);

    private final LibraryRepositoryPort libraryRepositoryPort;
    private final GameProviderPort gameProviderPort;
    private final UserRepositoryPort userRepositoryPort;

    public LibraryService(LibraryRepositoryPort libraryRepositoryPort, GameProviderPort gameProviderPort, UserRepositoryPort userRepositoryPort) {
        this.libraryRepositoryPort = libraryRepositoryPort;
        this.gameProviderPort = gameProviderPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public Optional<UserGame> upsertGameInLibrary(UUID userId, Long gameId, GameStatus status) {
        checkAuthorization(userId);
        String userIdString = userId.toString();

        var _ = gameProviderPort.findByExternalId(gameId)
                .orElseThrow(() -> new RuntimeException("Game with id " + gameId + " not found"));

        Optional<UserGame> existingEntryOpt = libraryRepositoryPort.findByUserIdAndGameId(userIdString, gameId);

        if (existingEntryOpt.isPresent()) {
            UserGame existingEntry = existingEntryOpt.get();
            
            logger.info("CHECKING GARBAGE COLLECTION: New Status={}, Existing Favorite={}", status, existingEntry.isFavorite());

            // Si el nuevo estado es NONE y el juego NO es favorito, borrar.
            if (status == GameStatus.NONE && !existingEntry.isFavorite()) {
                logger.info("GARBAGE COLLECTION TRIGGERED: Deleting game {} for user {}", gameId, userId);
                libraryRepositoryPort.deleteByUserIdAndGameId(userIdString, gameId);
                return Optional.empty();
            } else {
                logger.info("UPDATING: Keeping game {} for user {}", gameId, userId);
                UserGame updatedEntry = new UserGame(userIdString, gameId, status, existingEntry.addedAt(), existingEntry.isFavorite());
                return Optional.of(libraryRepositoryPort.update(updatedEntry));
            }
        } else {
            // Si no existe y el estado es NONE, no crear nada.
            if (status == GameStatus.NONE) {
                return Optional.empty();
            }
            UserGame newEntry = new UserGame(userIdString, gameId, status, LocalDateTime.now(), false);
            return Optional.of(libraryRepositoryPort.save(newEntry));
        }
    }

    @Override
    public List<UserGame> listUserLibrary(UUID userId) {
        checkAuthorization(userId);
        return libraryRepositoryPort.findByUserId(userId.toString());
    }

    @Override
    public Optional<UserGame> getUserGameStatus(UUID userId, Long gameId) {
        checkAuthorization(userId);
        return libraryRepositoryPort.findByUserIdAndGameId(userId.toString(), gameId);
    }

    @Override
    public void removeGameFromLibrary(UUID userId, Long gameId) {
        checkAuthorization(userId);
        libraryRepositoryPort.deleteByUserIdAndGameId(userId.toString(), gameId);
    }

    @Override
    public void addGameToFavorites(UUID userId, Long gameId) {
        checkAuthorization(userId);
        String userIdString = userId.toString();

        libraryRepositoryPort.findByUserIdAndGameId(userIdString, gameId)
                .ifPresentOrElse(
                        existingEntry -> {
                            UserGame updatedUserGame = new UserGame(userIdString, gameId, existingEntry.status(), existingEntry.addedAt(), true);
                            libraryRepositoryPort.update(updatedUserGame);
                        },
                        () -> {
                            var _ = gameProviderPort.findByExternalId(gameId)
                                    .orElseThrow(() -> new RuntimeException("Game with id " + gameId + " not found"));
                            UserGame newFavorite = new UserGame(userIdString, gameId, GameStatus.NONE, LocalDateTime.now(), true);
                            libraryRepositoryPort.save(newFavorite);
                        }
                );
    }

    @Override
    public void removeGameFromFavorites(UUID userId, Long gameId) {
        checkAuthorization(userId);
        String userIdString = userId.toString();
        UserGame userGame = libraryRepositoryPort.findByUserIdAndGameId(userIdString, gameId)
                .orElseThrow(() -> new RuntimeException("Game not found in library"));

        if (userGame.status() == GameStatus.NONE) {
            libraryRepositoryPort.deleteByUserIdAndGameId(userIdString, gameId);
        } else {
            UserGame updatedUserGame = new UserGame(userIdString, gameId, userGame.status(), userGame.addedAt(), false);
            libraryRepositoryPort.update(updatedUserGame);
        }
    }

    @Override
    public Page<UserGame> listFavoriteGames(UUID userId, Pageable pageable) {
        checkAuthorization(userId);
        return libraryRepositoryPort.findByUserIdAndIsFavoriteTrue(userId.toString(), pageable);
    }

    private void checkAuthorization(UUID requestedUserId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedLibraryAccessException("User not authenticated.");
        }

        String authenticatedUserEmail = getAuthenticatedUserEmail(authentication);

        userRepositoryPort.findByEmail(authenticatedUserEmail)
                .ifPresentOrElse(
                        authenticatedUser -> {
                            if (!authenticatedUser.id().equals(requestedUserId.toString())) {
                                throw new UnauthorizedLibraryAccessException("User " + authenticatedUser.id() + " is not authorized to access library of user " + requestedUserId);
                            }
                        },
                        () -> {
                            throw new UnauthorizedLibraryAccessException("Authenticated user not found in database.");
                        }
                );
    }

    private String getAuthenticatedUserEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal == null) {
            throw new UnauthorizedLibraryAccessException("Authentication principal is null.");
        }
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return principal.toString();
    }
}
