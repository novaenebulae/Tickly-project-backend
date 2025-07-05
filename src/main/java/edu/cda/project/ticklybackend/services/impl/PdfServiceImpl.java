package edu.cda.project.ticklybackend.services.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import edu.cda.project.ticklybackend.dtos.ticket.TicketResponseDto;
import edu.cda.project.ticklybackend.models.ticket.Ticket;
import edu.cda.project.ticklybackend.services.interfaces.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class PdfServiceImpl implements PdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    @Override
    public byte[] generateTicketsPdf(List<Ticket> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            log.warn("Tentative de génération de PDF pour une liste de billets vide.");
            return new byte[0];
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            log.info("Génération d'un PDF pour {} billet(s).", tickets.size());

            for (int i = 0; i < tickets.size(); i++) {
                Ticket ticket = tickets.get(i);
                addTicketToDocument(document, ticket, titleFont, headerFont, contentFont);

                // Ajouter une nouvelle page sauf pour le dernier billet
                if (i < tickets.size() - 1) {
                    document.newPage();
                }
            }
        } catch (DocumentException e) {
            log.error("Erreur lors de la génération du PDF pour les billets.", e);
        } finally {
            document.close();
        }

        log.info("PDF généré avec succès. Taille : {} bytes.", baos.size());
        return baos.toByteArray();
    }

    @Override
    public byte[] generateTicketsPdfFromDto(List<TicketResponseDto> ticketDtos) {
        if (ticketDtos == null || ticketDtos.isEmpty()) {
            log.warn("Tentative de génération de PDF pour une liste de billets DTO vide.");
            return new byte[0];
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            log.info("Génération d'un PDF pour {} billet(s) à partir des DTOs.", ticketDtos.size());

            for (int i = 0; i < ticketDtos.size(); i++) {
                TicketResponseDto ticketDto = ticketDtos.get(i);
                addTicketDtoToDocument(document, ticketDto, titleFont, headerFont, contentFont);

                // Ajouter une nouvelle page sauf pour le dernier billet
                if (i < ticketDtos.size() - 1) {
                    document.newPage();
                }
            }
        } catch (DocumentException e) {
            log.error("Erreur lors de la génération du PDF pour les billets DTO.", e);
        } finally {
            document.close();
        }

        log.info("PDF généré avec succès à partir des DTOs. Taille : {} bytes.", baos.size());
        return baos.toByteArray();
    }

    @Override
    public byte[] generateSingleTicketPdfFromDto(TicketResponseDto ticketDto) {
        if (ticketDto == null) {
            log.warn("Tentative de génération de PDF pour un billet DTO null.");
            return new byte[0];
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            log.info("Génération d'un PDF pour un billet unique à partir du DTO.");

            addTicketDtoToDocument(document, ticketDto, titleFont, headerFont, contentFont);

        } catch (DocumentException e) {
            log.error("Erreur lors de la génération du PDF pour le billet DTO.", e);
        } finally {
            document.close();
        }

        log.info("PDF généré avec succès pour un billet unique. Taille : {} bytes.", baos.size());
        return baos.toByteArray();
    }

    private void addTicketToDocument(Document document, Ticket ticket, Font titleFont, Font headerFont, Font contentFont) throws DocumentException {
        // En-tête du billet
        document.add(new Paragraph("🎫 BILLET TICKLY", titleFont));
        document.add(new Paragraph(" ")); // Espace

        // Informations de l'événement
        document.add(new Paragraph("📅 ÉVÉNEMENT", headerFont));
        document.add(new Paragraph("Nom : " + ticket.getEvent().getName(), contentFont));
        document.add(new Paragraph("Lieu : " + ticket.getEvent().getStructure().getName(), contentFont));

        if (ticket.getEvent().getAddress() != null) {
            document.add(new Paragraph("Adresse : " + ticket.getEvent().getAddress().getStreet() + ", " + ticket.getEvent().getAddress().getCity(), contentFont));
        }

        // Conversion Instant vers LocalDateTime pour le formatage
        LocalDateTime eventStartDate = LocalDateTime.ofInstant(ticket.getEvent().getStartDate(), ZoneId.systemDefault());
        document.add(new Paragraph("Date : " + eventStartDate.format(DATE_FORMATTER), contentFont));
        document.add(new Paragraph(" "));

        // Informations du participant
        document.add(new Paragraph("👤 PARTICIPANT", headerFont));
        document.add(new Paragraph("Nom : " + ticket.getParticipantFirstName() + " " + ticket.getParticipantLastName(), contentFont));
        document.add(new Paragraph("Email : " + ticket.getParticipantEmail(), contentFont));
        document.add(new Paragraph(" "));

        // Informations de la zone
        document.add(new Paragraph("🎯 ZONE D'AUDIENCE", headerFont));
        document.add(new Paragraph("Zone : " + ticket.getEventAudienceZone().getTemplate().getName(), contentFont));
        document.add(new Paragraph("Type de placement : " + ticket.getEventAudienceZone().getTemplate().getSeatingType(), contentFont));
        document.add(new Paragraph(" "));

        // Informations du billet
        document.add(new Paragraph("🎟️ INFORMATIONS DU BILLET", headerFont));
        document.add(new Paragraph("ID Billet : " + ticket.getId(), contentFont));
        document.add(new Paragraph("Statut : " + ticket.getStatus(), contentFont));
        document.add(new Paragraph("Date de réservation : " + LocalDateTime.ofInstant(ticket.getReservationDate(), ZoneId.systemDefault()).format(DATE_FORMATTER), contentFont));
        document.add(new Paragraph(" "));

        // QR Code
        document.add(new Paragraph("📱 QR CODE DE VALIDATION", headerFont));
        document.add(new Paragraph("[ Espace réservé pour le QR Code ]", headerFont));
        document.add(new Paragraph("Valeur QR : " + ticket.getQrCodeValue(), contentFont));
        document.add(new Paragraph(" "));

        // Instructions
        document.add(new Paragraph("ℹ️ INSTRUCTIONS", headerFont));
        document.add(new Paragraph("• Présentez ce billet à l'entrée de l'événement", contentFont));
        document.add(new Paragraph("• Le QR code sera scanné pour validation", contentFont));
        document.add(new Paragraph("• Arrivez 30 minutes avant le début de l'événement", contentFont));
    }

    private void addTicketDtoToDocument(Document document, TicketResponseDto ticketDto, Font titleFont, Font headerFont, Font contentFont) throws DocumentException {
        // En-tête du billet
        document.add(new Paragraph("🎫 BILLET TICKLY", titleFont));
        document.add(new Paragraph(" ")); // Espace

        // Informations de l'événement depuis l'EventSnapshot
        if (ticketDto.getEventSnapshot() != null) {
            document.add(new Paragraph("📅 ÉVÉNEMENT", headerFont));
            document.add(new Paragraph("Nom : " + ticketDto.getEventSnapshot().getName(), contentFont));

            if (ticketDto.getEventSnapshot().getAddress() != null) {
                document.add(new Paragraph("Adresse : " +
                        ticketDto.getEventSnapshot().getAddress().getStreet() + ", " +
                        ticketDto.getEventSnapshot().getAddress().getCity(), contentFont));
            }

            if (ticketDto.getEventSnapshot().getStartDate() != null) {
                String formattedDate = formatDateTime(ticketDto.getEventSnapshot().getStartDate());
                document.add(new Paragraph("Date : " + formattedDate, contentFont));
            }
            document.add(new Paragraph(" "));
        }

        // Informations du participant
        if (ticketDto.getParticipant() != null) {
            document.add(new Paragraph("👤 PARTICIPANT", headerFont));
            document.add(new Paragraph("Nom : " + ticketDto.getParticipant().getFirstName() + " " + ticketDto.getParticipant().getLastName(), contentFont));
            document.add(new Paragraph("Email : " + ticketDto.getParticipant().getEmail(), contentFont));
            document.add(new Paragraph(" "));
        }

        // Informations de la zone depuis l'AudienceZoneSnapshot
        if (ticketDto.getAudienceZoneSnapshot() != null) {
            document.add(new Paragraph("🎯 ZONE D'AUDIENCE", headerFont));
            document.add(new Paragraph("Zone : " + ticketDto.getAudienceZoneSnapshot().getName(), contentFont));
            document.add(new Paragraph("Type de placement : " + ticketDto.getAudienceZoneSnapshot().getSeatingType(), contentFont));
            document.add(new Paragraph(" "));
        }

        // Informations du billet
        document.add(new Paragraph("🎟️ INFORMATIONS DU BILLET", headerFont));
        document.add(new Paragraph("ID Billet : " + ticketDto.getId(), contentFont));
        document.add(new Paragraph("Statut : " + ticketDto.getStatus(), contentFont));

        if (ticketDto.getReservation_date_time() != null) {
            String formattedReservationDate = formatDateTime(ticketDto.getReservation_date_time());
            document.add(new Paragraph("Date de réservation : " + formattedReservationDate, contentFont));
        }
        document.add(new Paragraph(" "));

        // QR Code
        document.add(new Paragraph("📱 QR CODE DE VALIDATION", headerFont));
        document.add(new Paragraph("[ Espace réservé pour le QR Code ]", headerFont));
        document.add(new Paragraph("Valeur QR : " + ticketDto.getQrCodeValue(), contentFont));
        document.add(new Paragraph(" "));

        // Instructions
        document.add(new Paragraph("ℹ️ INSTRUCTIONS", headerFont));
        document.add(new Paragraph("• Présentez ce billet à l'entrée de l'événement", contentFont));
        document.add(new Paragraph("• Le QR code sera scanné pour validation", contentFont));
        document.add(new Paragraph("• Arrivez 30 minutes avant le début de l'événement", contentFont));
    }

    /**
     * Formate un objet temporal (LocalDateTime, Instant, etc.) en string lisible.
     * Cette méthode gère les différents types de dates qui peuvent être présents dans les DTOs.
     */
    private String formatDateTime(Object dateTime) {
        if (dateTime == null) {
            return "Date non disponible";
        }

        try {
            if (dateTime instanceof LocalDateTime) {
                return ((LocalDateTime) dateTime).format(DATE_FORMATTER);
            } else if (dateTime instanceof Instant) {
                LocalDateTime localDateTime = LocalDateTime.ofInstant((Instant) dateTime, ZoneId.systemDefault());
                return localDateTime.format(DATE_FORMATTER);
            } else {
                // Fallback pour d'autres types
                return dateTime.toString();
            }
        } catch (Exception e) {
            log.warn("Erreur lors du formatage de la date : {}", dateTime, e);
            return "Format de date invalide";
        }
    }
}
