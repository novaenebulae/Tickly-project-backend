package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.ticket.*;
import edu.cda.project.ticklybackend.enums.EventStatus;
import edu.cda.project.ticklybackend.enums.TicketStatus;
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
import edu.cda.project.ticklybackend.security.TicketSecurityService;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import edu.cda.project.ticklybackend.services.interfaces.MailingService;
import edu.cda.project.ticklybackend.services.interfaces.TicketService;
import edu.cda.project.ticklybackend.utils.AuthUtils;
import edu.cda.project.ticklybackend.utils.LoggingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
    private final TicketSecurityService ticketSecurityService;
    
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
            long existingTickets = ticketRepository.countByEventAudienceZoneId(zone.getId());
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

    @Override
    public List<TicketResponseDto> getMyTickets() {
        LoggingUtils.logMethodEntry(log, "getMyTickets");

        try {
            User currentUser = authUtils.getCurrentAuthenticatedUser();
            LoggingUtils.setUserId(currentUser.getId());

            List<Ticket> tickets = ticketRepository.findByUserId(currentUser.getId());
            List<TicketResponseDto> result = buildTicketResponseDtoList(tickets);

            LoggingUtils.logMethodExit(log, "getMyTickets", result);
            return result;
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

    @Override
    @Transactional
    public TicketValidationResponseDto validateTicket(TicketValidationRequestDto validationDto) {
        LoggingUtils.logMethodEntry(log, "validateTicket", "validationDto", validationDto);

        try {
            User validator = authUtils.getCurrentAuthenticatedUser();
            LoggingUtils.setUserId(validator.getId());
            log.info("L'utilisateur {} tente de valider le billet avec la valeur QR : {}", validator.getEmail(), validationDto.getScannedQrCodeValue());

            Ticket ticket = ticketRepository.findByQrCodeValue(validationDto.getScannedQrCodeValue())
                    .orElseThrow(() -> new ResourceNotFoundException("Billet avec QR code " + validationDto.getScannedQrCodeValue() + " non trouvé."));

            // Vérification de sécurité : le validateur doit avoir les droits sur la structure de l'événement.
            if (!ticketSecurityService.canValidateTicket(validationDto.getScannedQrCodeValue(), authUtils.getCurrentAuthentication())) {
                log.warn("Tentative de validation de billet non autorisée par l'utilisateur {}", validator.getEmail());
                throw new BadRequestException("Vous n'avez pas les droits nécessaires pour valider ce billet.");
            }

            if (ticket.getStatus() != TicketStatus.VALID) {
                log.warn("La validation a échoué pour le billet {}. Statut actuel : {}.", ticket.getId(), ticket.getStatus());
                throw new BadRequestException("Le billet n'est pas valide. Statut actuel : " + ticket.getStatus());
            }

            ticket.setStatus(TicketStatus.USED);
            ticketRepository.save(ticket);
            log.info("Billet {} validé avec succès par l'utilisateur {}.", ticket.getId(), validator.getEmail());

            ParticipantInfoDto participant = new ParticipantInfoDto();
            participant.setFirstName(ticket.getParticipantFirstName());
            participant.setLastName(ticket.getParticipantLastName());
            participant.setEmail(ticket.getParticipantEmail());

            TicketValidationResponseDto result = new TicketValidationResponseDto(
                    ticket.getId(),
                    ticket.getStatus(),
                    "Billet validé avec succès.",
                    participant
            );

            LoggingUtils.logMethodExit(log, "validateTicket", result);
            return result;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    // Méthode d'aide pour construire les URL complètes des photos
    private TicketResponseDto buildTicketResponseDto(Ticket ticket) {
        TicketResponseDto dto = ticketMapper.toDto(ticket);
        if (dto.getEventSnapshot() != null && dto.getEventSnapshot().getMainPhotoUrl() != null) {
            String fullUrl = fileStorageService.getFileUrl(dto.getEventSnapshot().getMainPhotoUrl(), "events/main");
            dto.getEventSnapshot().setMainPhotoUrl(fullUrl);
        }
        if (dto.getEventSnapshot() != null && dto.getStructureSnapshot().getLogoUrl() != null) {
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
}
