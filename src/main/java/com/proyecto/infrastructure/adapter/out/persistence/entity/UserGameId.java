package com.proyecto.infrastructure.adapter.out.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserGameId implements Serializable {
    private String user;
    private Long gameId;
}
