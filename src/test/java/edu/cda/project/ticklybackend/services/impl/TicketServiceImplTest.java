package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.common.PaginatedResponseDto;
import edu.cda.project.ticklybackend.dtos.statistics.EventTicketStatisticsDto;
import edu.cda.project.ticklybackend.dtos.ticket.TicketResponseDto;
import edu.cda.project.ticklybackend.dtos.ticket.TicketValidationResponseDto;
import edu.cda.project.ticklybackend.enums.TicketStatus;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.mappers.ticket.TicketMapper;
import edu.cda.project.ticklybackend.models.event.Event;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.ticket.Ticket;
import edu.cda.project.ticklybackend.models.user.SpectatorUser;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.event.EventRepository;
import edu.cda.project.ticklybackend.repositories.ticket.ReservationRepository;
import edu.cda.project.ticklybackend.repositories.ticket.TicketRepository;
import edu.cda.project.ticklybackend.security.TicketSecurityService;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import edu.cda.project.ticklybackend.services.interfaces.MailingService;
import edu.cda.project.ticklybackend.services.interfaces.StatisticsService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import edu.cda.project.ticklybackend.utils.AuthUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private AuthUtils authUtils;

    @Mock
    private TicketSecurityService ticketSecurityService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private MailingService mailingService;
    
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    
    @Mock
    private StatisticsService statisticsService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private User validUser;
    private Ticket validTicket;
    private Ticket invalidTicket;
    private Event event;
    private Structure structure;
    private String validQrCode;
    private String invalidQrCode;

    @BeforeEach
    void setUp() {
        // Create test data
        validQrCode = UUID.randomUUID().toString();
        invalidQrCode = UUID.randomUUID().toString();

        validUser = new SpectatorUser();
        validUser.setId(1L);
        validUser.setEmail("user@example.com");

        structure = new Structure();
        structure.setId(1L);
        structure.setName("Test Structure");

        event = new Event();
        event.setId(1L);
        event.setName("Test Event");
        event.setStructure(structure);
        // Set event dates for validation tests
        event.setStartDate(Instant.now().plusSeconds(3600)); // 1 hour from now
        event.setEndDate(Instant.now().plusSeconds(7200));   // 2 hours from now

        validTicket = new Ticket();
        validTicket.setId(UUID.randomUUID());
        validTicket.setQrCodeValue(validQrCode);
        validTicket.setEvent(event);
        validTicket.setStatus(TicketStatus.VALID);
        validTicket.setParticipantFirstName("John");
        validTicket.setParticipantLastName("Doe");
        validTicket.setParticipantEmail("john.doe@example.com");

        invalidTicket = new Ticket();
        invalidTicket.setId(UUID.randomUUID());
        invalidTicket.setQrCodeValue(invalidQrCode);
        invalidTicket.setEvent(event);
        invalidTicket.setStatus(TicketStatus.USED); // Already used ticket
        invalidTicket.setParticipantFirstName("Jane");
        invalidTicket.setParticipantLastName("Smith");
        invalidTicket.setParticipantEmail("jane.smith@example.com");
    }

    @Test
    void validateTicket_ValidTicket_ShouldReturnSuccessResponse() {
        // Arrange
        UUID ticketId = validTicket.getId();
        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(validUser);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(validTicket));
        
        // Mock the ticket mapper for WebSocket broadcasting
        TicketResponseDto mockTicketDto = new TicketResponseDto();
        when(ticketMapper.toDto(validTicket)).thenReturn(mockTicketDto);
        
        // Mock the statistics service for WebSocket broadcasting
        EventTicketStatisticsDto mockStats = new EventTicketStatisticsDto();
        when(statisticsService.getEventTicketStats(event.getId())).thenReturn(mockStats);

        // Act
        TicketValidationResponseDto response = ticketService.validateTicket(ticketId);

        // Assert
        assertNotNull(response);
        assertEquals(ticketId, response.getTicketId());
        assertEquals(TicketStatus.USED, response.getStatus());
        assertEquals("Billet validé avec succès.", response.getMessage());

        // Verify ticket was updated
        verify(ticketRepository).save(validTicket);
        assertEquals(TicketStatus.USED, validTicket.getStatus());
        assertNotNull(validTicket.getValidationDate());
        
        // Verify WebSocket broadcasting
        verify(messagingTemplate).convertAndSend(
                eq("/topic/event/" + event.getId() + "/ticket-update"),
                any(TicketResponseDto.class));
        verify(messagingTemplate).convertAndSend(
                eq("/topic/event/" + event.getId() + "/statistics"),
                any(EventTicketStatisticsDto.class));
        verify(statisticsService).getEventTicketStats(event.getId());
    }

    @Test
    void validateTicket_AlreadyUsedTicket_ShouldReturnErrorResponse() {
        // Arrange
        UUID ticketId = invalidTicket.getId();
        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(validUser);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(invalidTicket));

        // Act
        TicketValidationResponseDto response = ticketService.validateTicket(ticketId);

        // Assert
        assertNotNull(response);
        assertEquals(ticketId, response.getTicketId());
        assertEquals(TicketStatus.USED, response.getStatus());
        assertEquals("Ce billet a déjà été utilisé.", response.getMessage());

        // Verify ticket was not updated
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void validateTicket_NonExistentTicket_ShouldThrowException() {
        // Arrange
        UUID nonExistentTicketId = UUID.randomUUID();
        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(validUser);
        when(ticketRepository.findById(nonExistentTicketId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            ticketService.validateTicket(nonExistentTicketId);
        });
    }

    @Test
    void validateTicket_ExpiredEvent_ShouldReturnErrorResponse() {
        // Arrange
        UUID ticketId = validTicket.getId();

        // Create an event that has already ended
        Event expiredEvent = new Event();
        expiredEvent.setId(2L);
        expiredEvent.setName("Expired Event");
        expiredEvent.setStructure(structure);
        expiredEvent.setStartDate(Instant.now().minusSeconds(7200)); // 2 hours ago
        expiredEvent.setEndDate(Instant.now().minusSeconds(3600));   // 1 hour ago

        Ticket expiredTicket = new Ticket();
        expiredTicket.setId(ticketId);
        expiredTicket.setQrCodeValue(validQrCode);
        expiredTicket.setEvent(expiredEvent);
        expiredTicket.setStatus(TicketStatus.VALID);
        expiredTicket.setParticipantFirstName("John");
        expiredTicket.setParticipantLastName("Doe");
        expiredTicket.setParticipantEmail("john.doe@example.com");

        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(validUser);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(expiredTicket));

        // Act
        TicketValidationResponseDto response = ticketService.validateTicket(ticketId);

        // Assert
        assertNotNull(response);
        assertEquals(ticketId, response.getTicketId());
        assertEquals(TicketStatus.VALID, response.getStatus());
        assertEquals("L'événement est terminé, le billet ne peut plus être validé.", response.getMessage());

        // Verify ticket was not updated
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void getEventTickets_WithoutFilters_ShouldReturnPaginatedTickets() {
        // Arrange
        Long eventId = event.getId();
        Pageable pageable = PageRequest.of(0, 10);

        // Create a list of tickets for the event
        Ticket ticket1 = new Ticket();
        ticket1.setId(UUID.randomUUID());
        ticket1.setEvent(event);
        ticket1.setStatus(TicketStatus.VALID);
        ticket1.setParticipantFirstName("Alice");
        ticket1.setParticipantLastName("Johnson");
        ticket1.setParticipantEmail("alice@example.com");

        Ticket ticket2 = new Ticket();
        ticket2.setId(UUID.randomUUID());
        ticket2.setEvent(event);
        ticket2.setStatus(TicketStatus.USED);
        ticket2.setParticipantFirstName("Bob");
        ticket2.setParticipantLastName("Smith");
        ticket2.setParticipantEmail("bob@example.com");

        List<Ticket> tickets = Arrays.asList(ticket1, ticket2);

        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(validUser);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(ticketRepository.findAllByEventId(eventId)).thenReturn(tickets);

        // Mock the DTO conversion
        TicketResponseDto dto1 = new TicketResponseDto();
        dto1.setId(ticket1.getId());
        TicketResponseDto dto2 = new TicketResponseDto();
        dto2.setId(ticket2.getId());

        when(ticketMapper.toDto(ticket1)).thenReturn(dto1);
        when(ticketMapper.toDto(ticket2)).thenReturn(dto2);

        // Act
        PaginatedResponseDto<TicketResponseDto> result = ticketService.getEventTickets(eventId, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalItems());
        assertEquals(2, result.getItems().size());
        assertEquals(0, result.getCurrentPage());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void getEventTickets_FilteredByStatus_ShouldReturnFilteredTickets() {
        // Arrange
        Long eventId = event.getId();
        Pageable pageable = PageRequest.of(0, 10);

        // Create a list of tickets for the event with different statuses
        Ticket validTicket1 = new Ticket();
        validTicket1.setId(UUID.randomUUID());
        validTicket1.setEvent(event);
        validTicket1.setStatus(TicketStatus.VALID);
        validTicket1.setParticipantFirstName("Alice");
        validTicket1.setParticipantLastName("Johnson");
        validTicket1.setParticipantEmail("alice@example.com");

        Ticket validTicket2 = new Ticket();
        validTicket2.setId(UUID.randomUUID());
        validTicket2.setEvent(event);
        validTicket2.setStatus(TicketStatus.VALID);
        validTicket2.setParticipantFirstName("Charlie");
        validTicket2.setParticipantLastName("Brown");
        validTicket2.setParticipantEmail("charlie@example.com");

        Ticket usedTicket = new Ticket();
        usedTicket.setId(UUID.randomUUID());
        usedTicket.setEvent(event);
        usedTicket.setStatus(TicketStatus.USED);
        usedTicket.setParticipantFirstName("Bob");
        usedTicket.setParticipantLastName("Smith");
        usedTicket.setParticipantEmail("bob@example.com");

        List<Ticket> allTickets = Arrays.asList(validTicket1, validTicket2, usedTicket);

        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(validUser);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(ticketRepository.findAllByEventId(eventId)).thenReturn(allTickets);

        // Mock the DTO conversion
        TicketResponseDto dto1 = new TicketResponseDto();
        dto1.setId(validTicket1.getId());
        TicketResponseDto dto2 = new TicketResponseDto();
        dto2.setId(validTicket2.getId());

        when(ticketMapper.toDto(validTicket1)).thenReturn(dto1);
        when(ticketMapper.toDto(validTicket2)).thenReturn(dto2);

        // Act - Filter by VALID status
        PaginatedResponseDto<TicketResponseDto> result = ticketService.getEventTickets(eventId, TicketStatus.VALID, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalItems());
        assertEquals(2, result.getItems().size());
    }

    @Test
    void getEventTickets_FilteredBySearch_ShouldReturnMatchingTickets() {
        // Arrange
        Long eventId = event.getId();
        Pageable pageable = PageRequest.of(0, 10);
        String searchTerm = "alice";

        // Create a list of tickets for the event with different names
        Ticket ticket1 = new Ticket();
        ticket1.setId(UUID.randomUUID());
        ticket1.setEvent(event);
        ticket1.setStatus(TicketStatus.VALID);
        ticket1.setParticipantFirstName("Alice");
        ticket1.setParticipantLastName("Johnson");
        ticket1.setParticipantEmail("alice@example.com");

        Ticket ticket2 = new Ticket();
        ticket2.setId(UUID.randomUUID());
        ticket2.setEvent(event);
        ticket2.setStatus(TicketStatus.VALID);
        ticket2.setParticipantFirstName("Bob");
        ticket2.setParticipantLastName("Smith");
        ticket2.setParticipantEmail("bob@example.com");

        List<Ticket> allTickets = Arrays.asList(ticket1, ticket2);

        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(validUser);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(ticketRepository.findAllByEventId(eventId)).thenReturn(allTickets);

        // Mock the DTO conversion
        TicketResponseDto dto1 = new TicketResponseDto();
        dto1.setId(ticket1.getId());

        when(ticketMapper.toDto(ticket1)).thenReturn(dto1);

        // Act - Search for "alice"
        PaginatedResponseDto<TicketResponseDto> result = ticketService.getEventTickets(eventId, null, searchTerm, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalItems());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void getEventTickets_NonExistentEvent_ShouldThrowException() {
        // Arrange
        Long nonExistentEventId = 999L;
        Pageable pageable = PageRequest.of(0, 10);

        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(validUser);
        when(eventRepository.findById(nonExistentEventId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            ticketService.getEventTickets(nonExistentEventId, null, null, pageable);
        });
    }
    
    @Test
    void validateTicket_ShouldHandleStatisticsBroadcastingError() {
        // Arrange
        UUID ticketId = validTicket.getId();
        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(validUser);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(validTicket));
        
        // Mock the ticket mapper for WebSocket broadcasting
        TicketResponseDto mockTicketDto = new TicketResponseDto();
        when(ticketMapper.toDto(validTicket)).thenReturn(mockTicketDto);
        
        // Mock the statistics service to throw an exception
        when(statisticsService.getEventTicketStats(event.getId())).thenThrow(new RuntimeException("Test exception"));
        
        // Act
        TicketValidationResponseDto response = ticketService.validateTicket(ticketId);
        
        // Assert
        assertNotNull(response);
        assertEquals(ticketId, response.getTicketId());
        assertEquals(TicketStatus.USED, response.getStatus());
        assertEquals("Billet validé avec succès.", response.getMessage());
        
        // Verify ticket was updated
        verify(ticketRepository).save(validTicket);
        assertEquals(TicketStatus.USED, validTicket.getStatus());
        assertNotNull(validTicket.getValidationDate());
        
        // Verify WebSocket broadcasting for ticket update (should still happen)
        verify(messagingTemplate).convertAndSend(
                eq("/topic/event/" + event.getId() + "/ticket-update"),
                any(TicketResponseDto.class));
        
        // Verify statistics service was called but broadcasting statistics failed
        verify(statisticsService).getEventTicketStats(event.getId());
        verify(messagingTemplate, never()).convertAndSend(
                eq("/topic/event/" + event.getId() + "/statistics"),
                any(EventTicketStatisticsDto.class));
    }
}
