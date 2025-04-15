## Structure des modèles (Models)

### Entités principales

1. **Event.java** - Représente un événement
    - Propriétés: id, name, startDate, endDate, description, imageUrl, status, structure
    - Relation avec Structure (ManyToOne)
    - Relation avec EventCategory (Enum)
    - Relation avec EventStatus (Enum)

2. **EventStatus.java** - Énumération des statuts d'événement
    - Valeurs: DRAFT, PUBLISHED, CANCELLED, COMPLETED

3. **EventCategory.java** - Énumération des catégories d'événement
    - Valeurs: CONCERT, THEATRE, SPORT, EXHIBITION, CONFERENCE, FESTIVAL, OTHER

4. **EventHistory.java** - Historique d'un événement
    - Propriétés: id, expectedTickets, soldTickets, attendees, event
    - Relation avec Event (OneToOne)

5. **EventLocation.java** - Emplacement d'un événement
    - Propriétés: id, name, structure
    - Relation avec Structure (ManyToOne)
    - Relation avec Placement (OneToMany)

6. **Placement.java** - Représente un placement dans un événement
    - Propriétés: id, name, price, capacity, placementType, location
    - Relation avec EventLocation (ManyToOne)
    - Relation avec PlacementType (Enum)
    - Relation avec Event (ManyToMany)

7. **PlacementType.java** - Énumération des types de placement
    - Valeurs: SEAT_PLACEMENT, FREE_PLACEMENT

8. **Seat.java** - Représente un siège dans un placement
    - Propriétés: id, number, zone, row, available, placement
    - Relation avec Placement (ManyToOne)

9. **Ticket.java** - Représente un billet
    - Propriétés: id, qrCode, status, issueDate, eventRating, scanned, placement, event, order
    - Relation avec Placement (ManyToOne)
    - Relation avec Event (ManyToOne)
    - Relation avec Order (ManyToOne)

10. **Order.java** - Représente une commande
    - Propriétés: id, ticketCount, user
    - Relation avec User (ManyToOne)
    - Relation avec Ticket (OneToMany)

11. **Spectator.java** - Représente un spectateur
    - Propriétés: id, firstName, lastName, email, ticket
    - Relation avec Ticket (OneToOne)

12. **StructureFavorite.java** - Représente une structure favorite d'un utilisateur
    - Propriétés: id, addDate, structure, user
    - Relation avec Structure (ManyToOne)
    - Relation avec User (ManyToOne)

### Classes d'association

1. **EventPlacement.java** - Classe d'association entre Event et Placement (table COMPOSER)
    - Propriétés: eventId, placementId
    - Relation avec Event (ManyToOne)
    - Relation avec Placement (ManyToOne)

## DAOs (Data Access Objects)

1. **EventDao.java** - Interface pour l'accès aux données des événements
2. **EventHistoryDao.java** - Interface pour l'accès aux données des historiques d'événement
3. **EventLocationDao.java** - Interface pour l'accès aux données des emplacements d'événement
4. **PlacementDao.java** - Interface pour l'accès aux données des placements
5. **SeatDao.java** - Interface pour l'accès aux données des sièges
6. **TicketDao.java** - Interface pour l'accès aux données des billets
7. **OrderDao.java** - Interface pour l'accès aux données des commandes
8. **SpectatorDao.java** - Interface pour l'accès aux données des spectateurs
9. **StructureFavoriteDao.java** - Interface pour l'accès aux données des structures favorites

## Services

1. **EventService.java** - Service pour la gestion des événements
    - Méthodes: findAllEvents(), findEventById(), findEventsByStructure(), findEventsByCategory(), findEventsByStatus(),
      createEvent(), updateEvent(), deleteEvent(), publishEvent(), cancelEvent(), getEventHistory()

2. **EventLocationService.java** - Service pour la gestion des emplacements d'événement
    - Méthodes: findAllEventLocations(), findEventLocationById(), findEventLocationsByStructure(),
      createEventLocation(), updateEventLocation(), deleteEventLocation()

3. **PlacementService.java** - Service pour la gestion des placements
    - Méthodes: findAllPlacements(), findPlacementById(), findPlacementsByEventLocation(), findPlacementsByEvent(),
      createPlacement(), updatePlacement(), deletePlacement(), assignPlacementToEvent()

4. **TicketService.java** - Service pour la gestion des billets
    - Méthodes: findAllTickets(), findTicketById(), findTicketsByEvent(), findTicketsByUser(), createTicket(),
      cancelTicket(), validateTicket(), generateQRCode(), rateEvent()

5. **OrderService.java** - Service pour la gestion des commandes
    - Méthodes: findAllOrders(), findOrderById(), findOrdersByUser(), createOrder(), cancelOrder(),
      calculateOrderTotal(), addTicketToOrder()

6. **SpectatorService.java** - Service pour la gestion des spectateurs
    - Méthodes: findAllSpectators(), findSpectatorById(), findSpectatorsByEvent(), createSpectator(), updateSpectator(),
      deleteSpectator(), assignTicketToSpectator()

7. **FavoriteService.java** - Service pour la gestion des favoris
    - Méthodes: findAllFavorites(), findFavoriteById(), findFavoritesByUser(), addFavorite(), removeFavorite(),
      isFavorite()

## Contrôleurs (Controllers)

1. **EventController.java** - Contrôleur pour les opérations sur les événements
    - Endpoints: GET /api/events, GET /api/events/{id}, POST /api/events, PUT /api/events/{id}, DELETE /api/events/{id},
      GET /api/events/by-category/{category}, GET /api/events/by-structure/{structureId}

2. **EventLocationController.java** - Contrôleur pour les opérations sur les emplacements d'événement
    - Endpoints: GET /api/locations, GET /api/locations/{id}, POST /api/locations, PUT /api/locations/{id}, DELETE
      /api/locations/{id}, GET /api/locations/by-structure/{structureId}

3. **PlacementController.java** - Contrôleur pour les opérations sur les placements
    - Endpoints: GET /api/placements, GET /api/placements/{id}, POST /api/placements, PUT /api/placements/{id}, DELETE
      /api/placements/{id}, GET /api/placements/by-location/{locationId}, GET /api/placements/by-event/{eventId}

4. **TicketController.java** - Contrôleur pour les opérations sur les billets
    - Endpoints: GET /api/tickets, GET /api/tickets/{id}, POST /api/tickets, PUT /api/tickets/{id}, DELETE
      /api/tickets/{id}, GET /api/tickets/by-event/{eventId}, GET /api/tickets/by-user/{userId}, POST
      /api/tickets/validate/{qrCode}

5. **OrderController.java** - Contrôleur pour les opérations sur les commandes
    - Endpoints: GET /api/orders, GET /api/orders/{id}, POST /api/orders, PUT /api/orders/{id}, DELETE /api/orders/{id},
      GET /api/orders/by-user/{userId}, POST /api/orders/{orderId}/add-ticket

6. **SpectatorController.java** - Contrôleur pour les opérations sur les spectateurs
    - Endpoints: GET /api/spectators, GET /api/spectators/{id}, POST /api/spectators, PUT /api/spectators/{id}, DELETE
      /api/spectators/{id}, GET /api/spectators/by-event/{eventId}

7. **FavoriteController.java** - Contrôleur pour les opérations sur les favoris
    - Endpoints: GET /api/favorites, GET /api/favorites/{id}, POST /api/favorites, DELETE /api/favorites/{id}, GET
      /api/favorites/by-user/{userId}, GET /api/favorites/is-favorite/{userId}/{structureId}

## DTOs (Data Transfer Objects)

1. **EventDto.java** - DTO pour les événements
2. **EventLocationDto.java** - DTO pour les emplacements d'événement
3. **PlacementDto.java** - DTO pour les placements
4. **TicketDto.java** - DTO pour les billets
5. **OrderDto.java** - DTO pour les commandes
6. **SpectatorDto.java** - DTO pour les spectateurs

## Sécurité (Security)

1. **IsEventManager.java** - Annotation pour vérifier si l'utilisateur peut gérer un événement
2. **EventSecurityCheck.java** - Aspect pour vérifier les permissions sur les événements
3. **IsTicketOwner.java** - Annotation pour vérifier si l'utilisateur est propriétaire d'un billet
4. **TicketSecurityCheck.java** - Aspect pour vérifier les permissions sur les billets

