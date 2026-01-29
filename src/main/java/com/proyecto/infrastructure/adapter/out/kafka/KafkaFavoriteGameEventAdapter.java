package com.proyecto.infrastructure.adapter.out.kafka;

import com.proyecto.application.port.out.FavoriteGameEventPort;
import com.proyecto.domain.event.FavoriteGameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaFavoriteGameEventAdapter implements FavoriteGameEventPort {

    private static final Logger logger = LoggerFactory.getLogger(KafkaFavoriteGameEventAdapter.class);

    private final KafkaTemplate<String, FavoriteGameEvent> kafkaTemplate;
    private final String favoriteGamesTopic;

    public KafkaFavoriteGameEventAdapter(KafkaTemplate<String, FavoriteGameEvent> kafkaTemplate, String favoriteGamesTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.favoriteGamesTopic = favoriteGamesTopic;
    }

    @Override
    public void publishFavoriteGameEvent(FavoriteGameEvent event) {
        try {
            logger.info("Publishing favorite game event to topic '{}': {}", favoriteGamesTopic, event);
            kafkaTemplate.send(favoriteGamesTopic, event.userId().toString(), event);
        } catch (Exception e) {
            logger.error("Error publishing favorite game event to Kafka", e);
        }
    }
}
