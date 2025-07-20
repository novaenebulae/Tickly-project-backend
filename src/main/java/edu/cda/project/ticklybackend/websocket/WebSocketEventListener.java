package edu.cda.project.ticklybackend.websocket;

import edu.cda.project.ticklybackend.dtos.statistics.EventTicketStatisticsDto;
import edu.cda.project.ticklybackend.services.interfaces.StatisticsService;
import edu.cda.project.ticklybackend.utils.LoggingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Listener for WebSocket subscription events.
 * Sends initial data to clients when they subscribe to specific topics.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener implements ApplicationListener<SessionSubscribeEvent> {

    private final SimpMessagingTemplate messagingTemplate;
    private final StatisticsService statisticsService;

    // Pattern to extract eventId from statistics topic: /topic/event/{eventId}/statistics
    private static final Pattern STATISTICS_TOPIC_PATTERN = Pattern.compile("/topic/event/(\\d+)/statistics");

    @Override
    public void onApplicationEvent(SessionSubscribeEvent event) {
        LoggingUtils.logMethodEntry(log, "onApplicationEvent", "event", event);

        try {
            SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
            String destination = headers.getDestination();
            
            if (destination == null) {
                log.warn("Received subscription event with null destination");
                return;
            }
            
            log.debug("Received subscription to destination: {}", destination);
            
            // Check if the subscription is for an event statistics topic
            Matcher matcher = STATISTICS_TOPIC_PATTERN.matcher(destination);
            if (matcher.matches()) {
                // Extract the eventId from the destination
                String eventIdStr = matcher.group(1);
                Long eventId = Long.parseLong(eventIdStr);
                
                log.info("Client subscribed to statistics for event ID: {}, sending initial data", eventId);
                
                // Get the current statistics for the event
                try {
                    EventTicketStatisticsDto statistics = statisticsService.getEventTicketStats(eventId);
                    
                    // Send the statistics to the topic
                    log.debug("Sending initial statistics for event ID: {} to topic: {}", eventId, destination);
                    messagingTemplate.convertAndSend(destination, statistics);
                    
                    log.info("Initial statistics sent successfully for event ID: {}", eventId);
                } catch (Exception e) {
                    log.error("Error getting or sending initial statistics for event ID: {}", eventId, e);
                }
            }
            
            LoggingUtils.logMethodExit(log, "onApplicationEvent");
        } finally {
            LoggingUtils.clearContext();
        }
    }
}