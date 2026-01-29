package com.proyecto.infrastructure.config.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kafka.topic")
@Getter
@Setter
public class KafkaTopicProperties {
    private String favoriteGames;
}
