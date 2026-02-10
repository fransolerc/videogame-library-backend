package com.proyecto.infrastructure.adapter.in.web.mapper.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

@Component
public class MappingUtils {

    public URI mapStringToUri(String url) {
        return url != null ? URI.create(url) : null;
    }

    public List<URI> mapStringListToUriList(List<String> urls) {
        if (urls == null) {
            return Collections.emptyList();
        }
        return urls.stream()
                .map(this::mapStringToUri)
                .toList();
    }

    public Double mapRating(Double rating) {
        if (rating == null) {
            return null;
        }
        BigDecimal bd = BigDecimal.valueOf(rating / 10.0);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static OffsetDateTime map(LocalDateTime value) {
        return value != null ? value.atOffset(ZoneOffset.UTC) : null;
    }
}
