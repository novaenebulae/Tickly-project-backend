package edu.cda.project.ticklybackend.services.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import edu.cda.project.ticklybackend.dtos.ticket.TicketResponseDto;
import edu.cda.project.ticklybackend.models.ticket.Ticket;
import edu.cda.project.ticklybackend.services.interfaces.PdfService;
import edu.cda.project.ticklybackend.utils.LoggingUtils;
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
        LoggingUtils.logMethodEntry(log, "generateTicketsPdf", "tickets.size", tickets != null ? tickets.size() : 0);

        try {
            log.debug("Début de la génération de PDF pour une liste de billets");
            if (tickets == null || tickets.isEmpty()) {
                log.warn("Tentative de génération de PDF pour une liste de billets vide ou null");
                LoggingUtils.logMethodExit(log, "generateTicketsPdf", "empty byte array");
                return new byte[0];
            }

            log.debug("Préparation du document PDF pour {} billet(s)", tickets.size());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);

            try {
                PdfWriter.getInstance(document, baos);
                document.open();
                log.debug("Document PDF ouvert avec succès");

                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
                Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

                log.info("Génération d'un PDF pour {} billet(s)", tickets.size());

                for (int i = 0; i < tickets.size(); i++) {
                    Ticket ticket = tickets.get(i);
                    log.debug("Ajout du billet ID: {} au document PDF (page {})", ticket.getId(), i + 1);
                    try {
                        addTicketToDocument(document, ticket, titleFont, headerFont, contentFont);
                        log.debug("Billet ID: {} ajouté avec succès", ticket.getId());
                    } catch (DocumentException e) {
                        LoggingUtils.logException(log, "Erreur lors de l'ajout du billet ID: " + ticket.getId() + " au document PDF", e);
                    }

                    // Ajouter une nouvelle page sauf pour le dernier billet
                    if (i < tickets.size() - 1) {
                        document.newPage();
                        log.debug("Nouvelle page créée dans le document PDF");
                    }
                }
            } catch (DocumentException e) {
                LoggingUtils.logException(log, "Erreur lors de la génération du PDF pour les billets", e);
            } catch (Exception e) {
                LoggingUtils.logException(log, "Erreur inattendue lors de la génération du PDF pour les billets", e);
            } finally {
                try {
                    document.close();
                    log.debug("Document PDF fermé avec succès");
                } catch (Exception e) {
                    log.warn("Erreur lors de la fermeture du document PDF: {}", e.getMessage());
                }
            }

            int pdfSize = baos.size();
            if (pdfSize > 0) {
                log.info("PDF généré avec succès. Taille: {} bytes", pdfSize);
            } else {
                log.warn("Le PDF généré est vide (0 bytes)");
            }

            byte[] result = baos.toByteArray();
            LoggingUtils.logMethodExit(log, "generateTicketsPdf", "byte array of size " + result.length);
            return result;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public byte[] generateTicketsPdfFromDto(List<TicketResponseDto> ticketDtos) {
        LoggingUtils.logMethodEntry(log, "generateTicketsPdfFromDto", "ticketDtos.size", ticketDtos != null ? ticketDtos.size() : 0);

        try {
            log.debug("Début de la génération de PDF pour une liste de billets DTO");
            if (ticketDtos == null || ticketDtos.isEmpty()) {
                log.warn("Tentative de génération de PDF pour une liste de billets DTO vide ou null");
                LoggingUtils.logMethodExit(log, "generateTicketsPdfFromDto", "empty byte array");
                return new byte[0];
            }

            log.debug("Préparation du document PDF pour {} billet(s) DTO", ticketDtos.size());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);

            try {
                PdfWriter.getInstance(document, baos);
                document.open();
                log.debug("Document PDF ouvert avec succès");

                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
                Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

                log.info("Génération d'un PDF pour {} billet(s) à partir des DTOs", ticketDtos.size());

                for (int i = 0; i < ticketDtos.size(); i++) {
                    TicketResponseDto ticketDto = ticketDtos.get(i);
                    log.debug("Ajout du billet DTO ID: {} au document PDF (page {})", ticketDto.getId(), i + 1);
                    try {
                        addTicketDtoToDocument(document, ticketDto, titleFont, headerFont, contentFont);
                        log.debug("Billet DTO ID: {} ajouté avec succès", ticketDto.getId());
                    } catch (DocumentException e) {
                        LoggingUtils.logException(log, "Erreur lors de l'ajout du billet DTO ID: " + ticketDto.getId() + " au document PDF", e);
                    }

                    // Ajouter une nouvelle page sauf pour le dernier billet
                    if (i < ticketDtos.size() - 1) {
                        document.newPage();
                        log.debug("Nouvelle page créée dans le document PDF");
                    }
                }
            } catch (DocumentException e) {
                LoggingUtils.logException(log, "Erreur lors de la génération du PDF pour les billets DTO", e);
            } catch (Exception e) {
                LoggingUtils.logException(log, "Erreur inattendue lors de la génération du PDF pour les billets DTO", e);
            } finally {
                try {
                    document.close();
                    log.debug("Document PDF fermé avec succès");
                } catch (Exception e) {
                    log.warn("Erreur lors de la fermeture du document PDF: {}", e.getMessage());
                }
            }

            int pdfSize = baos.size();
            if (pdfSize > 0) {
                log.info("PDF généré avec succès à partir des DTOs. Taille: {} bytes", pdfSize);
            } else {
                log.warn("Le PDF généré à partir des DTOs est vide (0 bytes)");
            }

            byte[] result = baos.toByteArray();
            LoggingUtils.logMethodExit(log, "generateTicketsPdfFromDto", "byte array of size " + result.length);
            return result;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    public byte[] generateSingleTicketPdfFromDto(TicketResponseDto ticketDto) {
        LoggingUtils.logMethodEntry(log, "generateSingleTicketPdfFromDto", "ticketDto", ticketDto != null ? ticketDto.getId() : "null");

        try {
            log.debug("Début de la génération de PDF pour un billet DTO unique");
            if (ticketDto == null) {
                log.warn("Tentative de génération de PDF pour un billet DTO null");
                LoggingUtils.logMethodExit(log, "generateSingleTicketPdfFromDto", "empty byte array");
                return new byte[0];
            }

            log.debug("Préparation du document PDF pour le billet DTO ID: {}", ticketDto.getId());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);

            try {
                PdfWriter.getInstance(document, baos);
                document.open();
                log.debug("Document PDF ouvert avec succès");

                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
                Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

                log.info("Génération d'un PDF pour un billet unique à partir du DTO ID: {}", ticketDto.getId());

                try {
                    addTicketDtoToDocument(document, ticketDto, titleFont, headerFont, contentFont);
                    log.debug("Billet DTO ID: {} ajouté avec succès au document PDF", ticketDto.getId());
                } catch (DocumentException e) {
                    LoggingUtils.logException(log, "Erreur lors de l'ajout du billet DTO ID: " + ticketDto.getId() + " au document PDF", e);
                }

            } catch (DocumentException e) {
                LoggingUtils.logException(log, "Erreur lors de la génération du PDF pour le billet DTO ID: " + ticketDto.getId(), e);
            } catch (Exception e) {
                LoggingUtils.logException(log, "Erreur inattendue lors de la génération du PDF pour le billet DTO ID: " + ticketDto.getId(), e);
            } finally {
                try {
                    document.close();
                    log.debug("Document PDF fermé avec succès");
                } catch (Exception e) {
                    log.warn("Erreur lors de la fermeture du document PDF: {}", e.getMessage());
                }
            }

            int pdfSize = baos.size();
            if (pdfSize > 0) {
                log.info("PDF généré avec succès pour un billet unique ID: {}. Taille: {} bytes", ticketDto.getId(), pdfSize);
            } else {
                log.warn("Le PDF généré pour le billet unique ID: {} est vide (0 bytes)", ticketDto.getId());
            }

            byte[] result = baos.toByteArray();
            LoggingUtils.logMethodExit(log, "generateSingleTicketPdfFromDto", "byte array of size " + result.length);
            return result;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    private void addTicketToDocument(Document document, Ticket ticket, Font titleFont, Font headerFont, Font contentFont) throws DocumentException {
        LoggingUtils.logMethodEntry(log, "addTicketToDocument", "ticket.id", ticket.getId());

        try {
            // En-tête du billet
            document.add(new Paragraph("BILLET TICKLY", titleFont));
            document.add(new Paragraph(" ")); // Espace

            // Informations de l'événement
            document.add(new Paragraph("ÉVÉNEMENT", headerFont));
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
            document.add(new Paragraph("PARTICIPANT", headerFont));
            document.add(new Paragraph("Nom : " + ticket.getParticipantFirstName() + " " + ticket.getParticipantLastName(), contentFont));
            document.add(new Paragraph("Email : " + ticket.getParticipantEmail(), contentFont));
            document.add(new Paragraph(" "));

            // Informations de la zone
            document.add(new Paragraph("ZONE D'AUDIENCE", headerFont));
            document.add(new Paragraph("Zone : " + ticket.getEventAudienceZone().getTemplate().getName(), contentFont));
            document.add(new Paragraph("Type de placement : " + ticket.getEventAudienceZone().getTemplate().getSeatingType(), contentFont));
            document.add(new Paragraph(" "));

            // Informations du billet
            document.add(new Paragraph("INFORMATIONS DU BILLET", headerFont));
            document.add(new Paragraph("ID Billet : " + ticket.getId(), contentFont));
            document.add(new Paragraph("Statut : " + ticket.getStatus(), contentFont));
            document.add(new Paragraph("Date de réservation : " + LocalDateTime.ofInstant(ticket.getReservationDate(), ZoneId.systemDefault()).format(DATE_FORMATTER), contentFont));
            document.add(new Paragraph(" "));

            // QR Code
            document.add(new Paragraph("QR CODE DE VALIDATION", headerFont));
            document.add(new Paragraph("[ Espace réservé pour le QR Code ]", headerFont));
            document.add(new Paragraph("Valeur QR : " + ticket.getQrCodeValue(), contentFont));
            document.add(new Paragraph(" "));

            // Instructions
            document.add(new Paragraph("INSTRUCTIONS", headerFont));
            document.add(new Paragraph("• Présentez ce billet à l'entrée de l'événement", contentFont));
            document.add(new Paragraph("• Le QR code sera scanné pour validation", contentFont));
            document.add(new Paragraph("• Arrivez 30 minutes avant le début de l'événement", contentFont));

            LoggingUtils.logMethodExit(log, "addTicketToDocument");
        } finally {
            // No need to clear context for private methods called by public methods that already handle context
        }
    }

    private void addTicketDtoToDocument(Document document, TicketResponseDto ticketDto, Font titleFont, Font headerFont, Font contentFont) throws DocumentException {
        LoggingUtils.logMethodEntry(log, "addTicketDtoToDocument", "ticketDto.id", ticketDto.getId());

        try {
            // En-tête du billet
            document.add(new Paragraph("BILLET TICKLY", titleFont));
            document.add(new Paragraph(" ")); // Espace

            // Informations de l'événement depuis l'EventSnapshot
            if (ticketDto.getEventSnapshot() != null) {
                document.add(new Paragraph("ÉVÉNEMENT", headerFont));
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
                document.add(new Paragraph("PARTICIPANT", headerFont));
                document.add(new Paragraph("Nom : " + ticketDto.getParticipant().getFirstName() + " " + ticketDto.getParticipant().getLastName(), contentFont));
                document.add(new Paragraph("Email : " + ticketDto.getParticipant().getEmail(), contentFont));
                document.add(new Paragraph(" "));
            }

            // Informations de la zone depuis l'AudienceZoneSnapshot
            if (ticketDto.getAudienceZoneSnapshot() != null) {
                document.add(new Paragraph("ZONE D'AUDIENCE", headerFont));
                document.add(new Paragraph("Zone : " + ticketDto.getAudienceZoneSnapshot().getName(), contentFont));
                document.add(new Paragraph("Type de placement : " + ticketDto.getAudienceZoneSnapshot().getSeatingType(), contentFont));
                document.add(new Paragraph(" "));
            }

            // Informations du billet
            document.add(new Paragraph("INFORMATIONS DU BILLET", headerFont));
            document.add(new Paragraph("ID Billet : " + ticketDto.getId(), contentFont));
            document.add(new Paragraph("Statut : " + ticketDto.getStatus(), contentFont));

            if (ticketDto.getReservation_date_time() != null) {
                String formattedReservationDate = formatDateTime(ticketDto.getReservation_date_time());
                document.add(new Paragraph("Date de réservation : " + formattedReservationDate, contentFont));
            }
            document.add(new Paragraph(" "));

            // QR Code
            document.add(new Paragraph("QR CODE DE VALIDATION", headerFont));
            document.add(new Paragraph("[ Espace réservé pour le QR Code ]", headerFont));
            document.add(new Paragraph("Valeur QR : " + ticketDto.getQrCodeValue(), contentFont));
            document.add(new Paragraph(" "));

            // Instructions
            document.add(new Paragraph("INSTRUCTIONS", headerFont));
            document.add(new Paragraph("• Présentez ce billet à l'entrée de l'événement", contentFont));
            document.add(new Paragraph("• Le QR code sera scanné pour validation", contentFont));
            document.add(new Paragraph("• Arrivez 30 minutes avant le début de l'événement", contentFont));

            LoggingUtils.logMethodExit(log, "addTicketDtoToDocument");
        } finally {
            // No need to clear context for private methods called by public methods that already handle context
        }
    }

    /**
     * Formate un objet temporal (LocalDateTime, Instant, etc.) en string lisible.
     * Cette méthode gère les différents types de dates qui peuvent être présents dans les DTOs.
     *
     * @param dateTime L'objet date/heure à formater
     * @return Une chaîne formatée représentant la date/heure
     */
    private String formatDateTime(Object dateTime) {
        LoggingUtils.logMethodEntry(log, "formatDateTime", "dateTime", dateTime);

        try {
            log.debug("Tentative de formatage d'un objet date/heure: {}", dateTime);
            if (dateTime == null) {
                log.debug("Objet date/heure null, retour de la valeur par défaut");
                LoggingUtils.logMethodExit(log, "formatDateTime", "Date non disponible");
                return "Date non disponible";
            }

            try {
                String formattedDate;
                if (dateTime instanceof LocalDateTime) {
                    log.debug("Formatage d'un LocalDateTime");
                    formattedDate = ((LocalDateTime) dateTime).format(DATE_FORMATTER);
                } else if (dateTime instanceof Instant) {
                    log.debug("Conversion d'un Instant en LocalDateTime pour formatage");
                    LocalDateTime localDateTime = LocalDateTime.ofInstant((Instant) dateTime, ZoneId.systemDefault());
                    formattedDate = localDateTime.format(DATE_FORMATTER);
                } else {
                    log.debug("Type de date non reconnu ({}), utilisation de toString()", dateTime.getClass().getSimpleName());
                    formattedDate = dateTime.toString();
                }
                log.debug("Date formatée avec succès: {}", formattedDate);
                LoggingUtils.logMethodExit(log, "formatDateTime", formattedDate);
                return formattedDate;
            } catch (Exception e) {
                LoggingUtils.logException(log, "Erreur lors du formatage de la date '" + dateTime + "'", e);
                LoggingUtils.logMethodExit(log, "formatDateTime", "Format de date invalide");
                return "Format de date invalide";
            }
        } finally {
            // No need to clear context for private methods called by public methods that already handle context
        }
    }
}
