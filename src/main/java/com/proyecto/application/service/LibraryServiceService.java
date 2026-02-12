package com.proyecto.application.service;

import com.proyecto.application.port.in.LibraryServiceInterface;
import com.proyecto.application.port.out.event.FavoriteGameEventInterface;
import com.proyecto.application.port.out.provider.GameProviderInterface;
import com.proyecto.application.port.out.persistence.LibraryRepositoryInterface;
import com.proyecto.application.port.out.persistence.UserRepositoryInterface;
import com.proyecto.domain.event.FavoriteGameEvent;
import com.proyecto.domain.exception.UnauthorizedLibraryAccessException;
import com.proyecto.domain.model.GameStatus;
import com.proyecto.domain.model.UserGame;
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
public class LibraryServiceService implements LibraryServiceInterface {

    private final LibraryRepositoryInterface libraryRepositoryInterface;
    private final GameProviderInterface gameProviderInterface;
    private final UserRepositoryInterface userRepositoryInterface;
    private final FavoriteGameEventInterface favoriteGameEventInterface;

    public LibraryServiceService(LibraryRepositoryInterface libraryRepositoryInterface, GameProviderInterface gameProviderInterface,
                                 UserRepositoryInterface userRepositoryInterface, FavoriteGameEventInterface favoriteGameEventInterface) {
        this.libraryRepositoryInterface = libraryRepositoryInterface;
        this.gameProviderInterface = gameProviderInterface;
        this.userRepositoryInterface = userRepositoryInterface;
        this.favoriteGameEventInterface = favoriteGameEventInterface;
    }

    @Override
    public Optional<UserGame> upsertGameInLibrary(UUID userId, Long gameId, GameStatus status) {
        checkAuthorization(userId);
        String userIdString = userId.toString();

        var _ = gameProviderInterface.findByExternalId(gameId)
                .orElseThrow(() -> new RuntimeException("Game with id " + gameId + " not found"));

        Optional<UserGame> existingEntryOpt = libraryRepositoryInterface.findByUserIdAndGameId(userIdString, gameId);

        if (existingEntryOpt.isPresent()) {
            UserGame existingEntry = existingEntryOpt.get();

            if (status == GameStatus.NONE && Boolean.FALSE.equals(existingEntry.isFavorite())) {
                libraryRepositoryInterface.deleteByUserIdAndGameId(userIdString, gameId);
                return Optional.empty();
            } else {
                UserGame updatedEntry = new UserGame(userIdString, gameId, status, existingEntry.addedAt(), existingEntry.isFavorite());
                return Optional.of(libraryRepositoryInterface.update(updatedEntry));
            }
        } else {
            if (status == GameStatus.NONE) {
                return Optional.empty();
            }
            UserGame newEntry = new UserGame(userIdString, gameId, status, LocalDateTime.now(), false);
            return Optional.of(libraryRepositoryInterface.save(newEntry));
        }
    }

    @Override
    public List<UserGame> listUserLibrary(UUID userId) {
        checkAuthorization(userId);
        return libraryRepositoryInterface.findByUserId(userId.toString());
    }

    @Override
    public Optional<UserGame> getUserGameStatus(UUID userId, Long gameId) {
        checkAuthorization(userId);
        return libraryRepositoryInterface.findByUserIdAndGameId(userId.toString(), gameId);
    }

    @Override
    public void removeGameFromLibrary(UUID userId, Long gameId) {
        checkAuthorization(userId);
        libraryRepositoryInterface.deleteByUserIdAndGameId(userId.toString(), gameId);
    }

    @Override
    public UserGame addGameToFavorites(UUID userId, Long gameId) {
        checkAuthorization(userId);
        String userIdString = userId.toString();

        UserGame updatedUserGame = libraryRepositoryInterface.findByUserIdAndGameId(userIdString, gameId)
                .map(existingEntry -> {
                    UserGame updated = new UserGame(userIdString, gameId, existingEntry.status(), existingEntry.addedAt(), true);
                    return libraryRepositoryInterface.update(updated);
                })
                .orElseGet(() -> {
                    var _ = gameProviderInterface.findByExternalId(gameId)
                            .orElseThrow(() -> new RuntimeException("Game with id " + gameId + " not found"));
                    UserGame newFavorite = new UserGame(userIdString, gameId, GameStatus.NONE, LocalDateTime.now(), true);
                    return libraryRepositoryInterface.save(newFavorite);
                });

        // Publicar el evento
        FavoriteGameEvent event = new FavoriteGameEvent(userId, gameId, true, LocalDateTime.now());
        favoriteGameEventInterface.publishFavoriteGameEvent(event);

        return updatedUserGame;
    }

    @Override
    public void removeGameFromFavorites(UUID userId, Long gameId) {
        checkAuthorization(userId);
        String userIdString = userId.toString();
        UserGame userGame = libraryRepositoryInterface.findByUserIdAndGameId(userIdString, gameId)
                .orElseThrow(() -> new RuntimeException("Game not found in library"));

        // Solo publicar el evento si el juego era realmente un favorito
        if (Boolean.TRUE.equals(userGame.isFavorite())) {
            FavoriteGameEvent event = new FavoriteGameEvent(userId, gameId, false, LocalDateTime.now());
            favoriteGameEventInterface.publishFavoriteGameEvent(event);
        }

        if (userGame.status() == GameStatus.NONE) {
            libraryRepositoryInterface.deleteByUserIdAndGameId(userIdString, gameId);
        } else {
            UserGame updatedUserGame = new UserGame(userIdString, gameId, userGame.status(), userGame.addedAt(), false);
            libraryRepositoryInterface.update(updatedUserGame);
        }
    }

    @Override
    public Page<UserGame> listFavoriteGames(UUID userId, Pageable pageable) {
        checkAuthorization(userId);
        return libraryRepositoryInterface.findByUserIdAndIsFavoriteTrue(userId.toString(), pageable);
    }

    private void checkAuthorization(UUID requestedUserId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedLibraryAccessException("User not authenticated.");
        }

        String authenticatedUserEmail = getAuthenticatedUserEmail(authentication);

        userRepositoryInterface.findByEmail(authenticatedUserEmail)
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
