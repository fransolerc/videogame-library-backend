package com.proyecto.infrastructure.adapter.out.kafka;

import com.proyecto.application.port.out.event.FavoriteGameEventInterface;
import com.proyecto.domain.event.FavoriteGameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;

@Component
public class KafkaFavoriteGameEventAdapter implements FavoriteGameEventInterface {

    private static final Logger logger = LoggerFactory.getLogger(KafkaFavoriteGameEventAdapter.class);

    private final KafkaTemplate<String, FavoriteGameEvent> kafkaTemplate;
    private final String favoriteGamesTopic;

    public KafkaFavoriteGameEventAdapter(KafkaTemplate<String, FavoriteGameEvent> kafkaTemplate, String favoriteGamesTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.favoriteGamesTopic = favoriteGamesTopic;
    }

    @Override
    @Async
    public void publishFavoriteGameEvent(FavoriteGameEvent event) {
        try {
            logger.info("Attempting to publish favorite game event to topic '{}' for user {}", favoriteGamesTopic, event.userId());
            
            CompletableFuture<SendResult<String, FavoriteGameEvent>> future = kafkaTemplate.send(favoriteGamesTopic, event.userId().toString(), event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Successfully published event for user: {} to topic-partition {}-{}", 
                                event.userId(), result.getRecordMetadata().topic(), result.getRecordMetadata().partition());
                } else {
                    logger.error("Failed to publish event for user: {}. Reason: {}", 
                                event.userId(), ex.getMessage());
                }
            });

        } catch (Exception e) {
            // This exception is for issues before sending (e.g., serialization)
            logger.error("Error preparing favorite game event for Kafka", e);
        }
    }
}
