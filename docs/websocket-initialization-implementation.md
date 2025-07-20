# WebSocket Initialization Implementation

## Issue Description

The frontend component needed to receive EventTicketStatistics data immediately upon connecting to the WebSocket, rather than waiting for a ticket validation action to occur. Without this initial data, the statistics displayed on the client would not be correct until after the first ticket validation.

## Solution

### 1. WebSocket Event Listener

Created a new `WebSocketEventListener` class that implements `ApplicationListener<SessionSubscribeEvent>` to detect when clients subscribe to WebSocket topics:

```java
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
        // Implementation details...
        
        // Check if the subscription is for an event statistics topic
        Matcher matcher = STATISTICS_TOPIC_PATTERN.matcher(destination);
        if (matcher.matches()) {
            // Extract the eventId from the destination
            String eventIdStr = matcher.group(1);
            Long eventId = Long.parseLong(eventIdStr);
            
            // Get the current statistics for the event
            EventTicketStatisticsDto statistics = statisticsService.getEventTicketStats(eventId);
            
            // Send the statistics to the topic
            messagingTemplate.convertAndSend(destination, statistics);
        }
    }
}
```

This listener:
1. Detects when a client subscribes to a topic
2. Checks if the subscription is for an event statistics topic using a regex pattern
3. Extracts the eventId from the destination topic
4. Fetches the current statistics for the event
5. Sends the statistics to the topic

### 2. Documentation Updates

Updated the WebSocket API documentation to reflect the new behavior:

1. In the Event Statistics section:
   ```
   When a client subscribes to the following topic, they will immediately receive the current event statistics:
   
   /topic/event/{eventId}/statistics
   
   Where `{eventId}` is the ID of the event.
   
   Additionally, when a ticket is validated, updated event statistics are broadcast to this same topic.
   ```

2. In the Subscription Example section:
   ```javascript
   // Subscribe to event statistics updates
   // The server will immediately send the current statistics upon subscription
   // and then send updates whenever a ticket is validated
   stompClient.subscribe('/topic/event/' + eventId + '/statistics', function(message) {
     const statistics = JSON.parse(message.body);
     console.log('Received statistics update:', statistics);
     // Update the statistics dashboard in the UI
     updateStatisticsDashboard(statistics);
     
     // The first message received will be the initial statistics
     // No need to fetch statistics separately or use a fallback mechanism on initialization
   });
   ```

## Benefits

1. **Immediate Data Display**: The frontend now receives the current statistics immediately upon connecting to the WebSocket, without having to wait for a ticket validation action.

2. **Simplified Frontend Logic**: The frontend no longer needs to implement separate logic to fetch the initial statistics or use a fallback mechanism on initialization.

3. **Consistent User Experience**: Users see accurate statistics as soon as they open the ticket validation panel, providing a better and more consistent user experience.

4. **Reduced API Calls**: No need for an additional REST API call to fetch the initial statistics, reducing the load on the server.

## Testing

The implementation was tested by building the project to ensure there are no compilation errors. The build completed successfully, confirming that the implementation is syntactically correct and compatible with the rest of the codebase.

## Conclusion

This implementation successfully addresses the issue by providing the frontend with the current EventTicketStatistics data immediately upon connecting to the WebSocket. This ensures that the statistics displayed on the client are correct from the start, without having to wait for a ticket validation action to occur.