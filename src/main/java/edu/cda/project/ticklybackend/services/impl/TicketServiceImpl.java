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
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import edu.cda.project.ticklybackend.services.interfaces.MailingService;
import edu.cda.project.ticklybackend.services.interfaces.PdfService;
import edu.cda.project.ticklybackend.services.interfaces.TicketService;
import edu.cda.project.ticklybackend.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final PdfService pdfService;
    private final MailingService mailingService;

    @Override
    @Transactional
    public ReservationConfirmationDto createReservation(ReservationRequestDto requestDto) {
        User currentUser = authUtils.getCurrentAuthenticatedUser();
        log.info("L'utilisateur {} crée une réservation pour l'événement {}", currentUser.getEmail(), requestDto.getEventId());

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
        reservation.setTotalAmount(BigDecimal.ZERO); // Événements gratuits pour l'instant

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
            // Envoi du PDF complet à l'acheteur principal
            byte[] pdfAllTickets = pdfService.generateTicketsPdfFromDto(ticketDtos);
            if (pdfAllTickets.length > 0) {
                mailingService.sendTickets(
                        currentUser.getEmail(),
                        currentUser.getFirstName(),
                        event.getName(),
                        pdfAllTickets
                );
                log.info("PDF de tous les billets envoyé à l'acheteur principal : {}", currentUser.getEmail());
            }

            // Envoi des billets individuels aux participants qui le souhaitent
            for (int i = 0; i < requestDto.getParticipants().size(); i++) {
                ParticipantInfoDto participant = requestDto.getParticipants().get(i);

                // Vérifier si le participant veut recevoir son billet par email
                if (Boolean.TRUE.equals(participant.getSendTicketByEmail()) &&
                        !participant.getEmail().equals(currentUser.getEmail())) {

                    TicketResponseDto individualTicket = ticketDtos.get(i);
                    byte[] individualPdf = pdfService.generateSingleTicketPdfFromDto(individualTicket);

                    if (individualPdf.length > 0) {
                        String participantName = participant.getFirstName() + " " + participant.getLastName();
                        mailingService.sendIndividualTicket(
                                participant.getEmail(),
                                participantName,
                                event.getName(),
                                individualPdf
                        );
                        log.info("Billet individuel envoyé à : {} pour l'événement {}",
                                participant.getEmail(), event.getName());
                    }
                }
            }

        } catch (Exception e) {
            log.error("Erreur lors de la génération ou de l'envoi des PDFs pour la réservation {}. " +
                    "La réservation est confirmée mais l'envoi des emails a échoué.", savedReservation.getId(), e);
        }

        ReservationConfirmationDto confirmationDto = new ReservationConfirmationDto();
        confirmationDto.setReservationId(savedReservation.getId());
        confirmationDto.setReservationDate(ZonedDateTime.ofInstant(savedReservation.getReservationDate(), ZoneOffset.UTC));
        confirmationDto.setTickets(ticketDtos);

        return confirmationDto;
    }

    @Override
    public List<TicketResponseDto> getMyTickets() {
        User currentUser = authUtils.getCurrentAuthenticatedUser();
        List<Ticket> tickets = ticketRepository.findByUserId(currentUser.getId());
        return buildTicketResponseDtoList(tickets);
    }

    @Override
    public TicketResponseDto getTicketDetails(UUID ticketId) {
        User currentUser = authUtils.getCurrentAuthenticatedUser();
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Billet avec ID " + ticketId + " non trouvé."));

        // Vérification de sécurité : l'utilisateur doit être le propriétaire
        boolean isOwner = ticket.getUser().getId().equals(currentUser.getId());
        if (!isOwner) {
            throw new BadRequestException("Vous n'avez pas la permission de voir ce billet.");
        }

        return buildTicketResponseDto(ticket);
    }

    @Override
    @Transactional
    public TicketValidationResponseDto validateTicket(TicketValidationRequestDto validationDto) {
        User validator = authUtils.getCurrentAuthenticatedUser();
        log.info("L'utilisateur {} tente de valider le billet avec la valeur QR : {}", validator.getEmail(), validationDto.getScannedQrCodeValue());

        Ticket ticket = ticketRepository.findByQrCodeValue(validationDto.getScannedQrCodeValue())
                .orElseThrow(() -> new ResourceNotFoundException("Billet avec QR code " + validationDto.getScannedQrCodeValue() + " non trouvé."));

        // Vérification de sécurité : le validateur doit avoir les droits sur la structure de l'événement.
        log.warn("RISQUE DE SÉCURITÉ : La vérification des permissions pour la validation des billets est actuellement désactivée.");

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

        return new TicketValidationResponseDto(
                ticket.getId(),
                ticket.getStatus(),
                "Billet validé avec succès.",
                participant
        );
    }

    // Méthode d'aide pour construire les URL complètes des photos
    private TicketResponseDto buildTicketResponseDto(Ticket ticket) {
        TicketResponseDto dto = ticketMapper.toDto(ticket);
        if (dto.getEventSnapshot() != null && dto.getEventSnapshot().getMainPhotoUrl() != null) {
            String fullUrl = fileStorageService.getFileUrl(dto.getEventSnapshot().getMainPhotoUrl(), "events/main");
            dto.getEventSnapshot().setMainPhotoUrl(fullUrl);
        }
        return dto;
    }

    private List<TicketResponseDto> buildTicketResponseDtoList(List<Ticket> tickets) {
        return tickets.stream()
                .map(this::buildTicketResponseDto)
                .collect(Collectors.toList());
    }
}
