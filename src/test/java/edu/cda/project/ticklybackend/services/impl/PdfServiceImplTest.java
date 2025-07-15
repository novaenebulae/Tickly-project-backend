package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.ticket.ParticipantInfoDto;
import edu.cda.project.ticklybackend.dtos.ticket.TicketResponseDto;
import edu.cda.project.ticklybackend.enums.EventStatus;
import edu.cda.project.ticklybackend.enums.SeatingType;
import edu.cda.project.ticklybackend.enums.TicketStatus;
import edu.cda.project.ticklybackend.models.event.Event;
import edu.cda.project.ticklybackend.models.event.EventAddress;
import edu.cda.project.ticklybackend.models.event.EventAudienceZone;
import edu.cda.project.ticklybackend.models.structure.AudienceZoneTemplate;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.ticket.Reservation;
import edu.cda.project.ticklybackend.models.ticket.Ticket;
import edu.cda.project.ticklybackend.models.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PdfServiceImplTest {

    @InjectMocks
    private PdfServiceImpl pdfService;

    private Ticket testTicket;
    private TicketResponseDto testTicketDto;
    private List<Ticket> testTickets;
    private List<TicketResponseDto> testTicketDtos;

    @BeforeEach
    void setUp() {
        // Setup test structure
        Structure structure = new Structure();
        structure.setId(1L);
        structure.setName("Test Venue");

        // Setup test event address
        EventAddress eventAddress = new EventAddress();
        eventAddress.setStreet("123 Test Street");
        eventAddress.setCity("Test City");
        eventAddress.setZipCode("12345");
        eventAddress.setCountry("Test Country");

        // Setup test event
        Event event = new Event();
        event.setId(1L);
        event.setName("Test Event");
        event.setStructure(structure);
        event.setAddress(eventAddress);
        event.setStartDate(Instant.now().plusSeconds(86400)); // Tomorrow
        event.setEndDate(Instant.now().plusSeconds(90000)); // Tomorrow + 1 hour
        event.setStatus(EventStatus.PUBLISHED);

        // Setup test audience zone template
        AudienceZoneTemplate template = new AudienceZoneTemplate();
        template.setId(1L);
        template.setName("General Admission");
        template.setSeatingType(SeatingType.STANDING);
        template.setMaxCapacity(100);

        // Setup test audience zone
        EventAudienceZone audienceZone = new EventAudienceZone();
        audienceZone.setId(1L);
        audienceZone.setEvent(event);
        audienceZone.setTemplate(template);
        audienceZone.setAllocatedCapacity(100);

        // Setup test reservation
        Reservation reservation = new Reservation();
        reservation.setId(1L);

        // Setup test ticket
        testTicket = new Ticket();
        testTicket.setId(UUID.randomUUID());
        testTicket.setEvent(event);
        testTicket.setEventAudienceZone(audienceZone);
        testTicket.setParticipantFirstName("John");
        testTicket.setParticipantLastName("Doe");
        testTicket.setParticipantEmail("john.doe@example.com");
        testTicket.setStatus(TicketStatus.VALID);
        testTicket.setReservationDate(Instant.now());
        testTicket.setQrCodeValue("TEST-QR-CODE-12345");
        testTicket.setReservation(reservation);

        // Create a list of tickets
        testTickets = Arrays.asList(testTicket);

        // Setup test DTOs
        TicketResponseDto.EventTicketSnapshotDto eventSnapshotDto = new TicketResponseDto.EventTicketSnapshotDto();
        eventSnapshotDto.setEventId(1L);
        eventSnapshotDto.setName("Test Event");
        eventSnapshotDto.setStartDate(ZonedDateTime.ofInstant(Instant.now().plusSeconds(86400), ZoneId.systemDefault()));

        TicketResponseDto.AudienceZoneTicketSnapshotDto audienceZoneSnapshotDto = new TicketResponseDto.AudienceZoneTicketSnapshotDto();
        audienceZoneSnapshotDto.setAudienceZoneId(1L);
        audienceZoneSnapshotDto.setName("General Admission");
        audienceZoneSnapshotDto.setSeatingType(SeatingType.STANDING);

        ParticipantInfoDto participantInfoDto = new ParticipantInfoDto();
        participantInfoDto.setFirstName("John");
        participantInfoDto.setLastName("Doe");
        participantInfoDto.setEmail("john.doe@example.com");
        participantInfoDto.setSendTicketByEmail(false);

        testTicketDto = new TicketResponseDto();
        testTicketDto.setId(UUID.randomUUID());
        testTicketDto.setEventSnapshot(eventSnapshotDto);
        testTicketDto.setAudienceZoneSnapshot(audienceZoneSnapshotDto);
        testTicketDto.setParticipant(participantInfoDto);
        testTicketDto.setStatus(TicketStatus.VALID);
        testTicketDto.setReservation_date_time(ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
        testTicketDto.setQrCodeValue("TEST-QR-CODE-12345");

        // Create a list of ticket DTOs
        testTicketDtos = Arrays.asList(testTicketDto);
    }

    @Test
    void generateTicketsPdf_WithValidTickets_ShouldReturnPdfBytes() {
        // Act
        byte[] result = pdfService.generateTicketsPdf(testTickets);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void generateTicketsPdf_WithEmptyList_ShouldReturnEmptyByteArray() {
        // Act
        byte[] result = pdfService.generateTicketsPdf(Collections.emptyList());

        // Assert
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void generateTicketsPdf_WithNullList_ShouldReturnEmptyByteArray() {
        // Act
        byte[] result = pdfService.generateTicketsPdf(null);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void generateTicketsPdfFromDto_WithValidTicketDtos_ShouldReturnPdfBytes() {
        // Act
        byte[] result = pdfService.generateTicketsPdfFromDto(testTicketDtos);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void generateTicketsPdfFromDto_WithEmptyList_ShouldReturnEmptyByteArray() {
        // Act
        byte[] result = pdfService.generateTicketsPdfFromDto(Collections.emptyList());

        // Assert
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void generateTicketsPdfFromDto_WithNullList_ShouldReturnEmptyByteArray() {
        // Act
        byte[] result = pdfService.generateTicketsPdfFromDto(null);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void generateSingleTicketPdfFromDto_WithValidTicketDto_ShouldReturnPdfBytes() {
        // Act
        byte[] result = pdfService.generateSingleTicketPdfFromDto(testTicketDto);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void generateSingleTicketPdfFromDto_WithNullTicketDto_ShouldReturnEmptyByteArray() {
        // Act
        byte[] result = pdfService.generateSingleTicketPdfFromDto(null);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.length);
    }
}