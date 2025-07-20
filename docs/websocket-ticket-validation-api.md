# WebSocket Ticket Validation API Documentation

## Overview

This document describes the WebSocket API for real-time ticket validation in the Tickly application. The API allows frontend clients to receive real-time updates when tickets are validated, including updated ticket information and event statistics.

## WebSocket Connection

### Connection Endpoint

```
/api/v1/ws-tickly
```

The WebSocket endpoint uses SockJS for browser compatibility and STOMP as the messaging protocol.

### Authentication

Authentication is required to connect to the WebSocket endpoint. The JWT token should be included in the connection headers:

```javascript
const socket = new SockJS('/api/v1/ws-tickly');
const stompClient = Stomp.over(socket);

stompClient.connect({
  'Authorization': 'Bearer ' + jwtToken
}, function(frame) {
  console.log('Connected to WebSocket');
  // Subscribe to topics after successful connection
});
```

## Topics

### Ticket Updates

When a ticket is validated, the updated ticket information is broadcast to the following topic:

```
/topic/event/{eventId}/ticket-update
```

Where `{eventId}` is the ID of the event the ticket belongs to.

#### Message Format

The message contains a JSON representation of the updated ticket:

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "qrCodeValue": "qr-code-unique-value-string",
  "status": "USED",
  "participant": {
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com"
  },
  "eventSnapshot": {
    "eventId": 21,
    "name": "Scène Ouverte Poésie & Slam",
    "startDate": "2025-07-20T20:00:00Z",
    "address": {
      "street": "123 Main St",
      "city": "Paris",
      "zipCode": "75001",
      "country": "France"
    },
    "mainPhotoUrl": "http://example.com/photos/event21.jpg"
  },
  "audienceZoneSnapshot": {
    "audienceZoneId": 42,
    "name": "Fosse Chapelle",
    "seatingType": "STANDING"
  },
  "structureSnapshot": {
    "id": 6,
    "name": "La Chapelle",
    "logoUrl": "http://example.com/logos/chapelle.png"
  },
  "reservation_date_time": "2025-06-15T14:30:00Z"
}
```

### Event Statistics

When a client subscribes to the following topic, they will immediately receive the current event statistics:

```
/topic/event/{eventId}/statistics
```

Where `{eventId}` is the ID of the event.

Additionally, when a ticket is validated, updated event statistics are broadcast to this same topic.

#### Message Format

The message contains a JSON representation of the simplified event statistics:

```json
{
  "eventId": 21,
  "eventName": "Scène Ouverte Poésie & Slam",
  "totalTickets": 231,
  "scannedTickets": 20,
  "remainingTickets": 211,
  "fillRate": 66.0
}
```

## Subscription Example

Here's an example of how to subscribe to both topics for a specific event:

```javascript
// Subscribe to ticket updates
stompClient.subscribe('/topic/event/' + eventId + '/ticket-update', function(message) {
  const updatedTicket = JSON.parse(message.body);
  console.log('Received ticket update:', updatedTicket);
  // Update the ticket in the UI
  updateTicketInList(updatedTicket);
});

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

## Error Handling

If the WebSocket connection fails or statistics are not received, the frontend should implement a fallback mechanism:

```javascript
// If WebSocket connection fails
socket.onerror = function(error) {
  console.error('WebSocket error:', error);
  console.log('Using fallback statistics generated from ticket data');
  // Generate statistics from local data
  const statistics = generateStatisticsFromTicketData(tickets);
  updateStatisticsDashboard(statistics);
};

// If WebSocket connection closes unexpectedly
socket.onclose = function(event) {
  console.warn('WebSocket connection closed:', event);
  // Attempt to reconnect after a delay
  setTimeout(function() {
    connectWebSocket();
  }, 5000);
};
```

## Security Considerations

- The WebSocket endpoint is secured using Spring Security
- Only authenticated users can establish a connection and subscribe to topics
- The backend validates that the user has permission to access the event statistics before broadcasting them
- The frontend should include the JWT token in the WebSocket connection headers
- The backend will reject subscription requests for topics that the user is not authorized to access

## Limitations

- The WebSocket connection may be interrupted if the user's authentication token expires
- The frontend should handle reconnection and re-authentication in such cases
- Large numbers of simultaneous connections may impact server performance
- The frontend should implement a fallback mechanism in case the WebSocket connection is not available