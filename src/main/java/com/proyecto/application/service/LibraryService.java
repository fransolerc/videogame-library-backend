package com.proyecto.application.service;

import com.proyecto.application.port.in.LibraryUseCase;
import com.proyecto.application.port.out.GameProviderPort;
import com.proyecto.application.port.out.LibraryRepositoryPort;
import com.proyecto.application.port.out.UserRepositoryPort;
import com.proyecto.domain.exception.UnauthorizedLibraryAccessException;
import com.proyecto.domain.model.GameStatus;
import com.proyecto.domain.model.UserGame;
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

    private final LibraryRepositoryPort libraryRepositoryPort;
    private final GameProviderPort gameProviderPort;
    private final UserRepositoryPort userRepositoryPort;

    public LibraryService(LibraryRepositoryPort libraryRepositoryPort, GameProviderPort gameProviderPort, UserRepositoryPort userRepositoryPort) {
        this.libraryRepositoryPort = libraryRepositoryPort;
        this.gameProviderPort = gameProviderPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public UserGame upsertGameInLibrary(UUID userId, Long gameId, GameStatus status) {
        checkAuthorization(userId);
        String userIdString = userId.toString();

        gameProviderPort.findByExternalId(gameId)
                .orElseThrow(() -> new RuntimeException("Game with id " + gameId + " not found"));

        return libraryRepositoryPort.findByUserIdAndGameId(userIdString, gameId)
                .map(existingEntry -> {
                    UserGame updatedEntry = new UserGame(userIdString, gameId, status, existingEntry.addedAt());
                    return libraryRepositoryPort.save(updatedEntry);
                })
                .orElseGet(() -> {
                    UserGame newEntry = new UserGame(userIdString, gameId, status, LocalDateTime.now());
                    return libraryRepositoryPort.save(newEntry);
                });
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
