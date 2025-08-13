package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.event.EventDetailResponseDto;
import edu.cda.project.ticklybackend.dtos.event.EventStatusUpdateDto;
import edu.cda.project.ticklybackend.enums.EventStatus;
import edu.cda.project.ticklybackend.exceptions.BadRequestException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.mappers.event.EventMapper;
import edu.cda.project.ticklybackend.models.event.Event;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.repositories.event.EventRepository;
import edu.cda.project.ticklybackend.security.OrganizationalSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImpTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    @InjectMocks
    private OrganizationalSecurityService organizationalSecurityService;

    private Event testEvent;
    private Event testPublishedEvent;
    private EventDetailResponseDto detailDto;
    private Long eventId;
    private Long publishedEventId;

    @BeforeEach
    void setUp() {
        publishedEventId = 2L;
        eventId = 1L;
        Structure structure = new Structure();
        structure.setId(7L);
        structure.setName("My Structure");

        testEvent = new Event();
        testEvent.setId(eventId);
        testEvent.setName("My Event");
        testEvent.setStatus(EventStatus.DRAFT);
        testEvent.setDeleted(false);
        testEvent.setStructure(structure);
        testEvent.setStartDate(Instant.now().plusSeconds(3600));
        testEvent.setEndDate(Instant.now().plusSeconds(7200));

        testPublishedEvent = new Event();
        testPublishedEvent.setId(publishedEventId);
        testPublishedEvent.setName("My Event Test");
        testPublishedEvent.setStatus(EventStatus.PUBLISHED);
        testPublishedEvent.setDeleted(false);
        testPublishedEvent.setStructure(structure);
        testPublishedEvent.setStartDate(Instant.now().plusSeconds(3600));
        testPublishedEvent.setEndDate(Instant.now().plusSeconds(7200));

        detailDto = new EventDetailResponseDto();
        detailDto.setId(eventId);
        detailDto.setName("My Event");
        detailDto.setStatus(EventStatus.DRAFT);
    }

    @Test
    void getEventById_InvalidId_ThrowsResourceNotFound() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());
        when(eventRepository.findByIdIncludingDeleted(eventId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.getEventById(eventId));
        verify(eventRepository, times(1)).findById(eventId);
        verify(eventRepository, times(1)).findByIdIncludingDeleted(eventId);
    }

    @Test
    void deleteEvent_WhenDraft_MarksDeleted() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(testEvent)).thenReturn(testEvent);

        eventService.deleteEvent(eventId);

        assertTrue(testEvent.isDeleted());
        verify(eventRepository).findById(eventId);
        verify(eventRepository).save(testEvent);
    }

    @Test
    void deleteEvent_WhenPublished_ThrowsBadRequest() {
        testEvent.setStatus(EventStatus.PUBLISHED);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));

        assertThrows(BadRequestException.class, () -> eventService.deleteEvent(eventId));
        verify(eventRepository).findById(eventId);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void updateEventStatus_FromDraftToPublished_UpdatesAndReturnsDto() {
        EventStatusUpdateDto statusUpdateDto = new EventStatusUpdateDto();
        statusUpdateDto.setStatus(EventStatus.PUBLISHED);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(testEvent)).thenReturn(testEvent);
        when(eventMapper.toDetailDto(testEvent)).thenReturn(detailDto);

        EventDetailResponseDto result = eventService.updateEventStatus(eventId, statusUpdateDto);

        assertNotNull(result);
        assertEquals(eventId, result.getId());
        verify(eventRepository).findById(eventId);
        verify(eventRepository).save(testEvent);
        verify(eventMapper).toDetailDto(testEvent);
    }

    @Test
    void updateEventStatus_FromPublishedToDraft_ThrowsBadRequest() {
        testEvent.setStatus(EventStatus.PUBLISHED);
        EventStatusUpdateDto statusUpdateDto = new EventStatusUpdateDto();
        statusUpdateDto.setStatus(EventStatus.DRAFT);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));

        assertThrows(BadRequestException.class, () -> eventService.updateEventStatus(eventId, statusUpdateDto));
        verify(eventRepository).findById(eventId);
        verify(eventRepository, never()).save(any(Event.class));
    }
}
