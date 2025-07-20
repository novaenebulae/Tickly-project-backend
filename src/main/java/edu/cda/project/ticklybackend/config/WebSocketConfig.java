package edu.cda.project.ticklybackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration pour le WebSocket avec STOMP.
 * Cette classe configure le broker de messages et les endpoints WebSocket.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure le broker de messages et les préfixes de destination.
     *
     * @param registry Le registre du broker de messages.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Préfixe pour les destinations où les clients peuvent s'abonner pour recevoir des messages
        registry.enableSimpleBroker("/topic");

        // Préfixe pour les destinations où les clients peuvent envoyer des messages
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Enregistre les endpoints STOMP.
     *
     * @param registry Le registre des endpoints STOMP.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint WebSocket avec support SockJS pour les navigateurs qui ne supportent pas WebSocket
        registry.addEndpoint("/api/v1/ws-tickly")
                .setAllowedOriginPatterns("*") // À ajuster en production pour limiter aux origines autorisées
                .withSockJS();
    }
}