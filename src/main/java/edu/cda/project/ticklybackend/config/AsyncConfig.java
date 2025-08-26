package edu.cda.project.ticklybackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables asynchronous method execution and scheduled tasks for the application.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
}