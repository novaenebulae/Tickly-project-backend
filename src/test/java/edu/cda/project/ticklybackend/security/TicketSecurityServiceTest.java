package edu.cda.project.ticklybackend.security;

import edu.cda.project.ticklybackend.models.event.Event;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.ticket.Ticket;
import edu.cda.project.ticklybackend.repositories.ticket.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TicketSecurityServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private StructureSecurityService structureSecurityService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TicketSecurityService ticketSecurityService;

    private Ticket ticket;
    private Event event;
    private Structure structure;
    private String validQrCode;

    @BeforeEach
    void setUp() {
        // Create test data
        validQrCode = UUID.randomUUID().toString();

        structure = new Structure();
        structure.setId(1L);
        structure.setName("Test Structure");

        // Set event with start and end dates
        Instant now = Instant.now();
        Instant eventStart = now.plus(2, ChronoUnit.HOURS); // Event starts in 2 hours
        Instant eventEnd = eventStart.plus(3, ChronoUnit.HOURS); // Event lasts 3 hours

        event = new Event();
        event.setId(1L);
        event.setName("Test Event");
        event.setStructure(structure);
        event.setStartDate(eventStart);
        event.setEndDate(eventEnd);

        ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setQrCodeValue(validQrCode);
        ticket.setEvent(event);
    }

    @Test
    void canValidateTicket_WithValidQrCodeAndAuthorizedUser_ShouldReturnTrue() {
        // Arrange
        // Create a new event with start time 30 minutes ago and end time 2 hours from now
        Instant now = Instant.now();
        Instant eventStart = now.minus(30, ChronoUnit.MINUTES);
        Instant eventEnd = now.plus(2, ChronoUnit.HOURS);

        Event currentEvent = new Event();
        currentEvent.setId(10L);
        currentEvent.setName("Current Event");
        currentEvent.setStructure(structure);
        currentEvent.setStartDate(eventStart);
        currentEvent.setEndDate(eventEnd);

        Ticket currentTicket = new Ticket();
        currentTicket.setId(UUID.randomUUID());
        currentTicket.setQrCodeValue(validQrCode);
        currentTicket.setEvent(currentEvent);

        when(ticketRepository.findByQrCodeValue(validQrCode)).thenReturn(Optional.of(currentTicket));
        when(structureSecurityService.isStructureStaff(structure.getId(), authentication)).thenReturn(true);

        // Act
        boolean result = ticketSecurityService.canValidateTicket(validQrCode, authentication);

        // Assert
        assertTrue(result, "User with valid permissions should be able to validate the ticket");
    }

    @Test
    void canValidateTicket_WithValidQrCodeButUnauthorizedUser_ShouldReturnFalse() {
        // Arrange
        when(ticketRepository.findByQrCodeValue(validQrCode)).thenReturn(Optional.of(ticket));
        when(structureSecurityService.isStructureStaff(structure.getId(), authentication)).thenReturn(false);

        // Act
        boolean result = ticketSecurityService.canValidateTicket(validQrCode, authentication);

        // Assert
        assertFalse(result, "User without valid permissions should not be able to validate the ticket");
    }

    @Test
    void canValidateTicket_WithInvalidQrCode_ShouldReturnFalse() {
        // Arrange
        String invalidQrCode = "invalid-qr-code";
        when(ticketRepository.findByQrCodeValue(invalidQrCode)).thenReturn(Optional.empty());

        // Act
        boolean result = ticketSecurityService.canValidateTicket(invalidQrCode, authentication);

        // Assert
        assertFalse(result, "Invalid QR code should not be validated");
    }

    @Test
    void canValidateTicket_WithNullQrCode_ShouldReturnFalse() {
        // Act
        boolean result = ticketSecurityService.canValidateTicket(null, authentication);

        // Assert
        assertFalse(result, "Null QR code should not be validated");
    }

    @Test
    void canValidateTicket_WithEmptyQrCode_ShouldReturnFalse() {
        // Act
        boolean result = ticketSecurityService.canValidateTicket("", authentication);

        // Assert
        assertFalse(result, "Empty QR code should not be validated");
    }

    @Test
    void canValidateTicket_WithNullAuthentication_ShouldReturnFalse() {
        // Act
        boolean result = ticketSecurityService.canValidateTicket(validQrCode, null);

        // Assert
        assertFalse(result, "Null authentication should not be validated");
    }

    @Test
    void canValidateTicket_WithinValidTimeWindow_ShouldReturnTrue() {
        // Arrange
        // Create a new event with start time 30 minutes from now (within the 1 hour validation window)
        Instant now = Instant.now();
        Instant eventStart = now.plus(30, ChronoUnit.MINUTES);
        Instant eventEnd = eventStart.plus(3, ChronoUnit.HOURS);

        Event validTimeEvent = new Event();
        validTimeEvent.setId(2L);
        validTimeEvent.setName("Valid Time Event");
        validTimeEvent.setStructure(structure);
        validTimeEvent.setStartDate(eventStart);
        validTimeEvent.setEndDate(eventEnd);

        Ticket validTimeTicket = new Ticket();
        validTimeTicket.setId(UUID.randomUUID());
        validTimeTicket.setQrCodeValue("valid-time-qr");
        validTimeTicket.setEvent(validTimeEvent);

        when(ticketRepository.findByQrCodeValue("valid-time-qr")).thenReturn(Optional.of(validTimeTicket));
        when(structureSecurityService.isStructureStaff(structure.getId(), authentication)).thenReturn(true);

        // Act
        boolean result = ticketSecurityService.canValidateTicket("valid-time-qr", authentication);

        // Assert
        assertTrue(result, "Ticket should be validated within the valid time window (1 hour before event start)");
    }

    @Test
    void canValidateTicket_DuringEvent_ShouldReturnTrue() {
        // Arrange
        // Create a new event that has already started but not ended
        Instant now = Instant.now();
        Instant eventStart = now.minus(1, ChronoUnit.HOURS); // Started 1 hour ago
        Instant eventEnd = now.plus(1, ChronoUnit.HOURS); // Ends in 1 hour

        Event ongoingEvent = new Event();
        ongoingEvent.setId(3L);
        ongoingEvent.setName("Ongoing Event");
        ongoingEvent.setStructure(structure);
        ongoingEvent.setStartDate(eventStart);
        ongoingEvent.setEndDate(eventEnd);

        Ticket ongoingTicket = new Ticket();
        ongoingTicket.setId(UUID.randomUUID());
        ongoingTicket.setQrCodeValue("ongoing-event-qr");
        ongoingTicket.setEvent(ongoingEvent);

        when(ticketRepository.findByQrCodeValue("ongoing-event-qr")).thenReturn(Optional.of(ongoingTicket));
        when(structureSecurityService.isStructureStaff(structure.getId(), authentication)).thenReturn(true);

        // Act
        boolean result = ticketSecurityService.canValidateTicket("ongoing-event-qr", authentication);

        // Assert
        assertTrue(result, "Ticket should be validated during the event");
    }

    @Test
    void canValidateTicket_TooEarlyBeforeEvent_ShouldReturnFalse() {
        // Arrange
        // Create a new event that starts more than 1 hour from now
        Instant now = Instant.now();
        Instant eventStart = now.plus(2, ChronoUnit.HOURS); // Starts in 2 hours (outside 1 hour window)
        Instant eventEnd = eventStart.plus(1, ChronoUnit.HOURS);

        Event futureEvent = new Event();
        futureEvent.setId(4L);
        futureEvent.setName("Future Event");
        futureEvent.setStructure(structure);
        futureEvent.setStartDate(eventStart);
        futureEvent.setEndDate(eventEnd);

        Ticket futureTicket = new Ticket();
        futureTicket.setId(UUID.randomUUID());
        futureTicket.setQrCodeValue("future-event-qr");
        futureTicket.setEvent(futureEvent);

        when(ticketRepository.findByQrCodeValue("future-event-qr")).thenReturn(Optional.of(futureTicket));
        when(structureSecurityService.isStructureStaff(structure.getId(), authentication)).thenReturn(true);

        // Act
        boolean result = ticketSecurityService.canValidateTicket("future-event-qr", authentication);

        // Assert
        assertFalse(result, "Ticket should not be validated more than 1 hour before event start");
    }

    @Test
    void canValidateTicket_AfterEventEnded_ShouldReturnFalse() {
        // Arrange
        // Create a new event that has already ended
        Instant now = Instant.now();
        Instant eventStart = now.minus(3, ChronoUnit.HOURS); // Started 3 hours ago
        Instant eventEnd = now.minus(1, ChronoUnit.HOURS); // Ended 1 hour ago

        Event pastEvent = new Event();
        pastEvent.setId(5L);
        pastEvent.setName("Past Event");
        pastEvent.setStructure(structure);
        pastEvent.setStartDate(eventStart);
        pastEvent.setEndDate(eventEnd);

        Ticket pastTicket = new Ticket();
        pastTicket.setId(UUID.randomUUID());
        pastTicket.setQrCodeValue("past-event-qr");
        pastTicket.setEvent(pastEvent);

        when(ticketRepository.findByQrCodeValue("past-event-qr")).thenReturn(Optional.of(pastTicket));
        when(structureSecurityService.isStructureStaff(structure.getId(), authentication)).thenReturn(true);

        // Act
        boolean result = ticketSecurityService.canValidateTicket("past-event-qr", authentication);

        // Assert
        assertFalse(result, "Ticket should not be validated after event has ended");
    }
}
