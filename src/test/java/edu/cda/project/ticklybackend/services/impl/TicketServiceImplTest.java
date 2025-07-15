package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.ticket.TicketValidationRequestDto;
import edu.cda.project.ticklybackend.dtos.ticket.TicketValidationResponseDto;
import edu.cda.project.ticklybackend.enums.TicketStatus;
import edu.cda.project.ticklybackend.exceptions.BadRequestException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.mappers.ticket.TicketMapper;
import edu.cda.project.ticklybackend.models.event.Event;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.ticket.Ticket;
import edu.cda.project.ticklybackend.models.user.SpectatorUser;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.ticket.TicketRepository;
import edu.cda.project.ticklybackend.security.TicketSecurityService;
import edu.cda.project.ticklybackend.utils.AuthUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private AuthUtils authUtils;

    @Mock
    private TicketSecurityService ticketSecurityService;

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
    private TicketValidationRequestDto validationRequestDto;

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

        validationRequestDto = new TicketValidationRequestDto();
        validationRequestDto.setScannedQrCodeValue(validQrCode);
    }

    @Test
    void validateTicket_WithValidQrCodeAndAuthorizedUser_ShouldReturnValidationResponse() {
        // Arrange
        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(validUser);
        when(authUtils.getCurrentAuthentication()).thenReturn(authentication);
        when(ticketRepository.findByQrCodeValue(validQrCode)).thenReturn(Optional.of(validTicket));
        when(ticketSecurityService.canValidateTicket(validQrCode, authentication)).thenReturn(true);

        // Act
        TicketValidationResponseDto response = ticketService.validateTicket(validationRequestDto);

        // Assert
        assertNotNull(response);
        assertEquals(validTicket.getId(), response.getTicketId());
        assertEquals(TicketStatus.USED, response.getStatus()); // Status should be changed to USED
        assertEquals("Billet validé avec succès.", response.getMessage());
        assertNotNull(response.getParticipant());
        assertEquals("John", response.getParticipant().getFirstName());
        assertEquals("Doe", response.getParticipant().getLastName());
        assertEquals("john.doe@example.com", response.getParticipant().getEmail());

        // Verify
        verify(ticketRepository, times(1)).findByQrCodeValue(validQrCode);
        verify(ticketSecurityService, times(1)).canValidateTicket(validQrCode, authentication);
        verify(ticketRepository, times(1)).save(validTicket);
    }

    @Test
    void validateTicket_WithInvalidQrCode_ShouldThrowResourceNotFoundException() {
        // Arrange
        String nonExistentQrCode = "non-existent-qr-code";
        TicketValidationRequestDto invalidRequestDto = new TicketValidationRequestDto();
        invalidRequestDto.setScannedQrCodeValue(nonExistentQrCode);

        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(validUser);
        when(ticketRepository.findByQrCodeValue(nonExistentQrCode)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            ticketService.validateTicket(invalidRequestDto);
        });

        // Verify
        verify(ticketRepository, times(1)).findByQrCodeValue(nonExistentQrCode);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void validateTicket_WithUnauthorizedUser_ShouldThrowBadRequestException() {
        // Arrange
        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(validUser);
        when(authUtils.getCurrentAuthentication()).thenReturn(authentication);
        when(ticketRepository.findByQrCodeValue(validQrCode)).thenReturn(Optional.of(validTicket));
        when(ticketSecurityService.canValidateTicket(validQrCode, authentication)).thenReturn(false);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            ticketService.validateTicket(validationRequestDto);
        });

        // Verify
        verify(ticketRepository, times(1)).findByQrCodeValue(validQrCode);
        verify(ticketSecurityService, times(1)).canValidateTicket(validQrCode, authentication);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void validateTicket_WithAlreadyUsedTicket_ShouldThrowBadRequestException() {
        // Arrange
        TicketValidationRequestDto invalidRequestDto = new TicketValidationRequestDto();
        invalidRequestDto.setScannedQrCodeValue(invalidQrCode);

        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(validUser);
        when(authUtils.getCurrentAuthentication()).thenReturn(authentication);
        when(ticketRepository.findByQrCodeValue(invalidQrCode)).thenReturn(Optional.of(invalidTicket));
        when(ticketSecurityService.canValidateTicket(invalidQrCode, authentication)).thenReturn(true);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            ticketService.validateTicket(invalidRequestDto);
        });

        // Verify
        verify(ticketRepository, times(1)).findByQrCodeValue(invalidQrCode);
        verify(ticketSecurityService, times(1)).canValidateTicket(invalidQrCode, authentication);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }
}
