package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.common.PaginatedResponseDto;
import edu.cda.project.ticklybackend.dtos.statistics.EventTicketStatisticsDto;
import edu.cda.project.ticklybackend.dtos.ticket.*;
import edu.cda.project.ticklybackend.enums.EventStatus;
import edu.cda.project.ticklybackend.enums.TicketStatus;
import edu.cda.project.ticklybackend.exceptions.AccessDeniedException;
import edu.cda.project.ticklybackend.exceptions.BadRequestException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.mappers.ticket.TicketMapper;
import edu.cda.project.ticklybackend.models.event.Event;
import edu.cda.project.ticklybackend.models.event.EventAudienceZone;
import edu.cda.project.ticklybackend.models.ticket.Reservation;
import edu.cda.project.ticklybackend.models.ticket.Ticket;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.event.EventRepository;
import edu.cda.project.ticklybackend.repositories.ticket.ReservationRepository;
import edu.cda.project.ticklybackend.repositories.ticket.TicketRepository;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import edu.cda.project.ticklybackend.services.interfaces.MailingService;
import edu.cda.project.ticklybackend.services.interfaces.StatisticsService;
import edu.cda.project.ticklybackend.services.interfaces.TicketService;
import edu.cda.project.ticklybackend.utils.AuthUtils;
import edu.cda.project.ticklybackend.utils.LoggingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final TicketMapper ticketMapper;
    private final AuthUtils authUtils;
    private final FileStorageService fileStorageService;
    private final MailingService mailingService;
    private final SimpMessagingTemplate messagingTemplate;
    private final StatisticsService statisticsService;

    @Value("${tickly.mail.frontend-base-url}")
    private String frontendBaseUrl;

    @Override
    @Transactional
    public ReservationConfirmationDto createReservation(ReservationRequestDto requestDto) {
        LoggingUtils.logMethodEntry(log, "createReservation", "requestDto", requestDto);

        User currentUser = authUtils.getCurrentAuthenticatedUser();
        LoggingUtils.setUserId(currentUser.getId());
        log.info("L'utilisateur {} crée une réservation pour l'événement {}", currentUser.getEmail(), requestDto.getEventId());

        try {
            Event event = eventRepository.findById(requestDto.getEventId())
                    .orElseThrow(() -> new ResourceNotFoundException("Événement avec ID " + requestDto.getEventId() + " non trouvé."));

            // Vérification que l'événement n'a pas encore commencé
            Instant now = Instant.now();
            if (event.getStartDate().isBefore(now) || event.getStartDate().equals(now)) {
                log.warn("Tentative de réservation pour un événement déjà commencé. Événement ID: {}, Date de début: {}, Heure actuelle: {}",
                        event.getId(), event.getStartDate(), now);
                throw new BadRequestException("Impossible de réserver des billets pour un événement qui a déjà commencé");
            }

            // Récupérer la zone d'audience directement depuis l'événement
            EventAudienceZone zone = event.getAudienceZones().stream()
                    .filter(audienceZone -> audienceZone.getId().equals(requestDto.getAudienceZoneId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Zone d'audience avec ID " + requestDto.getAudienceZoneId() + " non trouvée."));

            // Validation de la logique métier
            if (event.getStatus() != EventStatus.PUBLISHED) {
                throw new BadRequestException("Les billets ne peuvent être réservés que pour des événements PUBLIÉS.");
            }
            if (!zone.getEvent().getId().equals(event.getId())) {
                throw new BadRequestException("La zone d'audience spécifiée n'appartient pas à l'événement demandé.");
            }

            // --- Vérification de la capacité ---
            // On compte uniquement les billets VALID et USED pour vérifier la capacité
            // Les billets CANCELLED ne sont pas comptés, ce qui libère de la capacité
            long existingTickets = ticketRepository.countByEventAudienceZoneIdAndStatusIn(
                    zone.getId(),
                    Arrays.asList(TicketStatus.VALID, TicketStatus.USED)
            );
            if (existingTickets + requestDto.getParticipants().size() > zone.getAllocatedCapacity()) {
                throw new BadRequestException("Capacité insuffisante dans la zone sélectionnée.");
            }
            // --- Fin de la vérification de la capacité ---

            Reservation reservation = new Reservation();
            reservation.setUser(currentUser);

            for (ParticipantInfoDto participant : requestDto.getParticipants()) {
                Ticket ticket = new Ticket();
                ticket.setEvent(event);
                ticket.setEventAudienceZone(zone);
                ticket.setUser(currentUser);
                ticket.setParticipantFirstName(participant.getFirstName());
                ticket.setParticipantLastName(participant.getLastName());
                ticket.setParticipantEmail(participant.getEmail());

                reservation.addTicket(ticket);
            }

            Reservation savedReservation = reservationRepository.save(reservation);
            log.info("Réservation {} créée avec succès pour l'utilisateur {}.", savedReservation.getId(), currentUser.getEmail());

            // Conversion en DTOs pour l'envoi des emails
            List<TicketResponseDto> ticketDtos = buildTicketResponseDtoList(savedReservation.getTickets());

            try {
                // Récupérer les UUIDs des billets pour l'envoi des emails
                List<UUID> ticketIds = savedReservation.getTickets().stream()
                        .map(Ticket::getId)
                        .collect(Collectors.toList());

                // Envoi des liens de billets à l'acheteur principal
                if (!ticketIds.isEmpty()) {
                    mailingService.sendTickets(
                            currentUser.getEmail(),
                            currentUser.getFirstName(),
                            event.getName(),
                            ticketIds,
                            frontendBaseUrl
                    );
                    log.info("Liens de billets envoyés à l'acheteur principal : {}", currentUser.getEmail());
                }

                // Envoi des liens de billets individuels aux participants qui le souhaitent
                for (int i = 0; i < requestDto.getParticipants().size(); i++) {
                    ParticipantInfoDto participant = requestDto.getParticipants().get(i);

                    // Vérifier si le participant veut recevoir son billet par email
                    if (Boolean.TRUE.equals(participant.getSendTicketByEmail()) &&
                            !participant.getEmail().equals(currentUser.getEmail())) {

                        Ticket ticket = savedReservation.getTickets().get(i);
                        String participantName = participant.getFirstName() + " " + participant.getLastName();

                        mailingService.sendIndividualTicket(
                                participant.getEmail(),
                                participantName,
                                event.getName(),
                                ticket.getId(),
                                frontendBaseUrl
                        );
                        log.info("Lien de billet individuel envoyé à : {} pour l'événement {}",
                                participant.getEmail(), event.getName());
                    }
                }

            } catch (Exception e) {
                LoggingUtils.logException(log, "Erreur lors de l'envoi des emails avec liens de billets pour la réservation " +
                        savedReservation.getId() + ". La réservation est confirmée mais l'envoi des emails a échoué", e);
            }

            ReservationConfirmationDto confirmationDto = new ReservationConfirmationDto();
            confirmationDto.setReservationId(savedReservation.getId());
            confirmationDto.setReservationDate(ZonedDateTime.ofInstant(savedReservation.getReservationDate(), ZoneOffset.UTC));
            confirmationDto.setTickets(ticketDtos);

            LoggingUtils.logMethodExit(log, "createReservation", confirmationDto);
            return confirmationDto;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    /**
     * Annule une réservation et tous les billets associés.
     * Les billets annulés libèrent des places pour l'événement.
     * Seul le propriétaire de la réservation peut l'annuler.
     *
     * @param reservationId L'ID de la réservation à annuler.
     * @return true si l'annulation a réussi, false sinon.
     */
    @Override
    @Transactional
    public boolean cancelReservation(Long reservationId) {
        LoggingUtils.logMethodEntry(log, "cancelReservation", "reservationId", reservationId);

        try {

            Reservation reservation = this.reservationRepository.findById(reservationId).orElseThrow(() -> new ResourceNotFoundException("Réservation avec ID " + reservationId + " non trouvée."));

            // Vérifier que l'utilisateur actuel est le propriétaire de la réservation
            User currentUser = authUtils.getCurrentAuthenticatedUser();
            if (currentUser == null || !reservation.getUser().getId().equals(currentUser.getId())) {
                log.warn("Tentative d'annulation d'une réservation par un utilisateur non autorisé. Réservation ID: {}, Utilisateur ID: {}",
                        reservationId, currentUser != null ? currentUser.getId() : "non authentifié");
                throw new AccessDeniedException("Vous n'êtes pas autorisé à annuler cette réservation");
            }

            // Récupérer tous les billets de la réservation
            List<Ticket> tickets = reservation.getTickets();
            if (tickets.isEmpty()) {
                log.warn("Aucun billet trouvé pour la réservation ID: {}", reservationId);
                return false;
            }

            // Vérifier que les billets peuvent être annulés (ils doivent être VALID)
            for (Ticket ticket : tickets) {
                if (ticket.getStatus() != TicketStatus.VALID) {
                    log.warn("Impossible d'annuler la réservation ID: {} car le billet ID: {} a le statut: {}",
                            reservationId, ticket.getId(), ticket.getStatus());
                    throw new BadRequestException("Impossible d'annuler la réservation car certains billets ont déjà été utilisés ou annulés");
                }
            }

            // Annuler tous les billets
            for (Ticket ticket : tickets) {
                ticket.setStatus(TicketStatus.CANCELLED);
                ticketRepository.save(ticket);
                log.debug("Billet ID: {} annulé", ticket.getId());
            }

            log.info("Réservation ID: {} annulée avec succès. {} billets annulés", reservationId, tickets.size());
            LoggingUtils.logMethodExit(log, "cancelReservation", true);
            return true;

        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de l'annulation de la réservation avec l'id: " + reservationId, e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public List<ReservationConfirmationDto> getMyReservations() {
        LoggingUtils.logMethodEntry(log, "getMyReservations");

        try {
            User currentUser = authUtils.getCurrentAuthenticatedUser();
            LoggingUtils.setUserId(currentUser.getId());

            List<Reservation> reservations = reservationRepository.findAllByUserId(currentUser.getId());
            List<ReservationConfirmationDto> confirmationDtos = new ArrayList<>();

            reservations.forEach(reservation -> {
                ReservationConfirmationDto confirmationDto = new ReservationConfirmationDto();
                confirmationDto.setReservationId(reservation.getId());
                confirmationDto.setReservationDate(ZonedDateTime.ofInstant(reservation.getReservationDate(), ZoneOffset.UTC));
                confirmationDto.setTickets(buildTicketResponseDtoList(reservation.getTickets()));
                confirmationDtos.add(confirmationDto);
            });

            LoggingUtils.logMethodExit(log, "getMyReservations", reservations);
            return confirmationDtos;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public TicketResponseDto getTicketDetails(UUID ticketId) {
        LoggingUtils.logMethodEntry(log, "getTicketDetails", "ticketId", ticketId);

        try {
            User currentUser = authUtils.getCurrentAuthenticatedUser();
            LoggingUtils.setUserId(currentUser.getId());

            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new ResourceNotFoundException("Billet avec ID " + ticketId + " non trouvé."));

            // Vérification de sécurité : l'utilisateur doit être le propriétaire
            boolean isOwner = ticket.getUser().getId().equals(currentUser.getId());
            if (!isOwner) {
                log.warn("Tentative d'accès non autorisé au billet {} par l'utilisateur {}", ticketId, currentUser.getEmail());
                throw new BadRequestException("Vous n'avez pas la permission de voir ce billet.");
            }

            TicketResponseDto result = buildTicketResponseDto(ticket);
            LoggingUtils.logMethodExit(log, "getTicketDetails", result);
            return result;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public TicketResponseDto getPublicTicketDetails(UUID ticketId) {
        LoggingUtils.logMethodEntry(log, "getPublicTicketDetails", "ticketId", ticketId);

        try {
            log.info("Accès public au billet avec ID: {}", ticketId);

            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new ResourceNotFoundException("Billet avec ID " + ticketId + " non trouvé."));

            TicketResponseDto result = buildTicketResponseDto(ticket);
            LoggingUtils.logMethodExit(log, "getPublicTicketDetails", result);
            return result;
        } finally {
            LoggingUtils.clearContext();
        }
    }


    // Méthode d'aide pour construire les URL complètes des photos et s'assurer que tous les champs sont correctement initialisés
    private TicketResponseDto buildTicketResponseDto(Ticket ticket) {
        TicketResponseDto dto = ticketMapper.toDto(ticket);

        // S'assurer que le participant est initialisé
        if (dto.getParticipant() == null) {
            ParticipantInfoDto participant = new ParticipantInfoDto();
            participant.setFirstName(ticket.getParticipantFirstName());
            participant.setLastName(ticket.getParticipantLastName());
            participant.setEmail(ticket.getParticipantEmail());
            dto.setParticipant(participant);
        }

        // Construire les URLs complètes des photos
        if (dto.getEventSnapshot() != null && dto.getEventSnapshot().getMainPhotoUrl() != null) {
            String fullUrl = fileStorageService.getFileUrl(dto.getEventSnapshot().getMainPhotoUrl(), "events/main");
            dto.getEventSnapshot().setMainPhotoUrl(fullUrl);
        }

        if (dto.getStructureSnapshot() != null && dto.getStructureSnapshot().getLogoUrl() != null) {
            String fullUrl = fileStorageService.getFileUrl(dto.getStructureSnapshot().getLogoUrl(), "structures/logos");
            dto.getStructureSnapshot().setLogoUrl(fullUrl);
        }

        return dto;
    }

    private List<TicketResponseDto> buildTicketResponseDtoList(List<Ticket> tickets) {
        return tickets.stream()
                .map(this::buildTicketResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TicketValidationResponseDto validateTicket(UUID ticketId) {
        LoggingUtils.logMethodEntry(log, "validateTicket", "ticketId", ticketId);

        try {
            User currentUser = authUtils.getCurrentAuthenticatedUser();
            LoggingUtils.setUserId(currentUser.getId());

            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new ResourceNotFoundException("Billet avec ID " + ticketId + " non trouvé."));

            // Vérification que le billet est valide
            if (ticket.getStatus() != TicketStatus.VALID) {
                log.warn("Tentative de validation d'un billet {} avec statut {}", ticketId, ticket.getStatus());

                ParticipantInfoDto participantInfo = new ParticipantInfoDto();
                participantInfo.setFirstName(ticket.getParticipantFirstName());
                participantInfo.setLastName(ticket.getParticipantLastName());
                participantInfo.setEmail(ticket.getParticipantEmail());

                return new TicketValidationResponseDto(
                        ticket.getId(),
                        ticket.getStatus(),
                        "Ce billet a déjà été " + (ticket.getStatus() == TicketStatus.USED ? "utilisé" : "annulé") + ".",
                        participantInfo,
                        ticket.getValidationDate()
                );
            }

            // Vérification que l'événement n'est pas terminé
            Instant now = Instant.now();
            Instant eventEnd = ticket.getEvent().getEndDate();
            if (now.isAfter(eventEnd)) {
                log.warn("Tentative de validation d'un billet {} pour un événement terminé", ticketId);

                ParticipantInfoDto participantInfo = new ParticipantInfoDto();
                participantInfo.setFirstName(ticket.getParticipantFirstName());
                participantInfo.setLastName(ticket.getParticipantLastName());
                participantInfo.setEmail(ticket.getParticipantEmail());

                return new TicketValidationResponseDto(
                        ticket.getId(),
                        ticket.getStatus(),
                        "L'événement est terminé, le billet ne peut plus être validé.",
                        participantInfo,
                        ticket.getValidationDate()
                );
            }

            // Validation du billet
            ticket.setStatus(TicketStatus.USED);
            ticket.setValidationDate(Instant.now());
            ticketRepository.save(ticket);

            log.info("Billet {} validé avec succès par {}", ticketId, currentUser.getEmail());

            // Broadcast the updated ticket via WebSocket
            Long eventId = ticket.getEvent().getId();
            TicketResponseDto updatedTicketDto = buildTicketResponseDto(ticket);
            log.debug("Broadcasting ticket update for ticket ID: {} to topic: /topic/event/{}/ticket-update",
                    ticketId, eventId);
            messagingTemplate.convertAndSend(
                    "/topic/event/" + eventId + "/ticket-update",
                    updatedTicketDto
            );

            // Get and broadcast simplified event statistics via WebSocket
            try {
                EventTicketStatisticsDto statistics = statisticsService.getEventTicketStats(eventId);
                log.debug("Broadcasting ticket statistics for event ID: {} to topic: /topic/event/{}/statistics",
                        eventId, eventId);
                messagingTemplate.convertAndSend(
                        "/topic/event/" + eventId + "/statistics",
                        statistics
                );
            } catch (Exception e) {
                log.error("Error getting or broadcasting ticket statistics for event ID: {}", eventId, e);
                // Continue execution even if statistics broadcasting fails
            }

            ParticipantInfoDto participantInfo = new ParticipantInfoDto();
            participantInfo.setFirstName(ticket.getParticipantFirstName());
            participantInfo.setLastName(ticket.getParticipantLastName());
            participantInfo.setEmail(ticket.getParticipantEmail());

            TicketValidationResponseDto result = new TicketValidationResponseDto(
                    ticket.getId(),
                    TicketStatus.USED,
                    "Billet validé avec succès.",
                    participantInfo,
                    ticket.getValidationDate()
            );

            LoggingUtils.logMethodExit(log, "validateTicket", result);
            return result;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public PaginatedResponseDto<TicketResponseDto> getEventTickets(Long eventId, TicketStatus status, String search, Pageable pageable) {
        LoggingUtils.logMethodEntry(log, "getEventTickets", "eventId", eventId, "status", status, "search", search);

        try {
            User currentUser = authUtils.getCurrentAuthenticatedUser();
            LoggingUtils.setUserId(currentUser.getId());

            // Vérifier que l'événement existe
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new ResourceNotFoundException("Événement avec ID " + eventId + " non trouvé."));

            // Récupérer tous les billets pour l'événement
            List<Ticket> allTickets = ticketRepository.findAllByEventId(eventId);

            // Filtrer par statut si spécifié
            if (status != null) {
                allTickets = allTickets.stream()
                        .filter(ticket -> ticket.getStatus() == status)
                        .collect(Collectors.toList());
            }

            // Filtrer par terme de recherche si spécifié
            if (StringUtils.hasText(search)) {
                String searchLower = search.toLowerCase();
                allTickets = allTickets.stream()
                        .filter(ticket ->
                                (ticket.getParticipantFirstName() != null && ticket.getParticipantFirstName().toLowerCase().contains(searchLower)) ||
                                        (ticket.getParticipantLastName() != null && ticket.getParticipantLastName().toLowerCase().contains(searchLower)) ||
                                        (ticket.getParticipantEmail() != null && ticket.getParticipantEmail().toLowerCase().contains(searchLower)) ||
                                        ticket.getId().toString().contains(searchLower)
                        )
                        .collect(Collectors.toList());
            }

            // Pagination manuelle
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allTickets.size());

            List<Ticket> pageTickets = start < end ? allTickets.subList(start, end) : new ArrayList<>();
            Page<Ticket> ticketPage = new PageImpl<>(pageTickets, pageable, allTickets.size());

            // Convertir en DTOs
            List<TicketResponseDto> ticketDtos = buildTicketResponseDtoList(ticketPage.getContent());
            PaginatedResponseDto<TicketResponseDto> result = new PaginatedResponseDto<>(ticketDtos, ticketPage.getTotalElements(),
                    ticketPage.getNumber(), ticketPage.getSize(), ticketPage.getTotalPages());

            LoggingUtils.logMethodExit(log, "getEventTickets", result);
            return result;
        } finally {
            LoggingUtils.clearContext();
        }
    }
}
