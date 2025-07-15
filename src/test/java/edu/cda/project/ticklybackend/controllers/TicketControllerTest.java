package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.ticket.ParticipantInfoDto;
import edu.cda.project.ticklybackend.dtos.ticket.TicketValidationRequestDto;
import edu.cda.project.ticklybackend.dtos.ticket.TicketValidationResponseDto;
import edu.cda.project.ticklybackend.enums.TicketStatus;
import edu.cda.project.ticklybackend.services.interfaces.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketController ticketController;

    private TicketValidationRequestDto validationRequestDto;
    private TicketValidationResponseDto validationResponseDto;
    private UUID ticketId;
    private String validQrCode;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test data
        ticketId = UUID.randomUUID();
        validQrCode = UUID.randomUUID().toString();

        // Create validation request DTO
        validationRequestDto = new TicketValidationRequestDto();
        validationRequestDto.setScannedQrCodeValue(validQrCode);

        // Create participant info DTO
        ParticipantInfoDto participantInfoDto = new ParticipantInfoDto();
        participantInfoDto.setFirstName("John");
        participantInfoDto.setLastName("Doe");
        participantInfoDto.setEmail("john.doe@example.com");

        // Create validation response DTO
        validationResponseDto = new TicketValidationResponseDto();
        validationResponseDto.setTicketId(ticketId);
        validationResponseDto.setStatus(TicketStatus.USED);
        validationResponseDto.setMessage("Billet validé avec succès.");
        validationResponseDto.setParticipant(participantInfoDto);
    }

    @Test
    void validateTicket_ShouldReturnValidationResponse() {
        // Arrange
        when(ticketService.validateTicket(validationRequestDto)).thenReturn(validationResponseDto);

        // Act
        ResponseEntity<TicketValidationResponseDto> response = ticketController.validateTicket(validationRequestDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ticketId, response.getBody().getTicketId());
        assertEquals(TicketStatus.USED, response.getBody().getStatus());
        assertEquals("Billet validé avec succès.", response.getBody().getMessage());
        assertNotNull(response.getBody().getParticipant());
        assertEquals("John", response.getBody().getParticipant().getFirstName());
        assertEquals("Doe", response.getBody().getParticipant().getLastName());
        assertEquals("john.doe@example.com", response.getBody().getParticipant().getEmail());

        // Verify
        verify(ticketService, times(1)).validateTicket(validationRequestDto);
    }

    // Note: Testing the PreAuthorize annotation would require integration tests with Spring Security
    // This unit test only verifies the controller's behavior assuming the security check passes
}