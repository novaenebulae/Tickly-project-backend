package edu.cda.project.ticklybackend.utils;

import edu.cda.project.ticklybackend.enums.EventStatus;
import edu.cda.project.ticklybackend.repositories.event.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventStatusUpdateUtils {

    private final EventRepository eventRepository;

    /**
     * Met à jour le statut d'un événement expiré en utilisant une nouvelle transaction
     * complètement indépendante du contexte transactionnel parent.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateExpiredEventStatus(Long eventId) {
        int updatedRows = eventRepository.updateEventStatusToCompleted(
                eventId,
                EventStatus.COMPLETED,
                EventStatus.PUBLISHED
        );

        if (updatedRows > 0) {
            log.info("Event ID: {} automatically marked as COMPLETED", eventId);
        }
    }
}
