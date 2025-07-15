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

        event = new Event();
        event.setId(1L);
        event.setName("Test Event");
        event.setStructure(structure);

        ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setQrCodeValue(validQrCode);
        ticket.setEvent(event);
    }

    @Test
    void canValidateTicket_WithValidQrCodeAndAuthorizedUser_ShouldReturnTrue() {
        // Arrange
        when(ticketRepository.findByQrCodeValue(validQrCode)).thenReturn(Optional.of(ticket));
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
}