package edu.cda.project.ticklybackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration pour activer le traitement asynchrone et les tâches planifiées.
 *
 * @EnableAsync permet l'exécution de méthodes @Async dans des threads séparés.
 * @EnableScheduling active la détection et l'exécution de méthodes @Scheduled.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
}