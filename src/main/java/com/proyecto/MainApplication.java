package com.proyecto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableKafka
@EnableCaching
@EnableAsync
public class MainApplication {

    private MainApplication() {
        // Private constructor to hide the implicit public one
    }

    static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
