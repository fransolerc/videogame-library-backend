package com.proyecto.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum PlatformType {
    CONSOLE(1),
    ARCADE(2),
    PLATFORM(3),
    OPERATING_SYSTEM(4),
    PORTABLE_CONSOLE(5),
    COMPUTER(6),
    UNKNOWN(0);

    private final int value;

    PlatformType(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    @JsonCreator
    public static PlatformType fromValue(Integer value) {
        if (value == null) {
            return UNKNOWN;
        }
        return Arrays.stream(PlatformType.values())
                .filter(type -> type.value == value)
                .findFirst()
                .orElse(UNKNOWN);
    }
}
