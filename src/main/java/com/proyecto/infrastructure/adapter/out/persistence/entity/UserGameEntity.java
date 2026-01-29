package com.proyecto.infrastructure.adapter.out.persistence.entity;

import com.proyecto.domain.model.GameStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_games")
@Getter
@Setter
@NoArgsConstructor
@IdClass(UserGameId.class)
public class UserGameEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity user;

    @Id
    @Column(name = "game_id")
    private Long gameId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @Column(nullable = false)
    private Boolean isFavorite = false;

    public UserGameEntity(UserEntity user, Long gameId, GameStatus status) {
        this.user = user;
        this.gameId = gameId;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
        if (isFavorite == null) {
            isFavorite = false;
        }
    }
}
