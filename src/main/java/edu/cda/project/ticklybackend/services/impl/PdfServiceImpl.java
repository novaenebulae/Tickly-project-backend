package edu.cda.project.ticklybackend.services.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import edu.cda.project.ticklybackend.models.ticket.Ticket;
import edu.cda.project.ticklybackend.services.interfaces.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@Slf4j
public class PdfServiceImpl implements PdfService {

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

            for (Ticket ticket : tickets) {
                document.add(new Paragraph("Billet Tickly", titleFont));
                document.add(new Paragraph(" ")); // Espace

                document.add(new Paragraph("Événement : " + ticket.getEvent().getName(), headerFont));
                document.add(new Paragraph("Lieu : " + ticket.getEvent().getStructure().getName(), contentFont));
                document.add(new Paragraph("Date : " + ticket.getEvent().getStartDate().toString(), contentFont));
                document.add(new Paragraph(" "));

                document.add(new Paragraph("Participant : " + ticket.getParticipantFirstName() + " " + ticket.getParticipantLastName(), headerFont));
                document.add(new Paragraph("Email : " + ticket.getParticipantEmail(), contentFont));
                document.add(new Paragraph("Zone : " + ticket.getEventAudienceZone().getTemplate().getName(), contentFont));
                document.add(new Paragraph(" "));

                document.add(new Paragraph("ID Billet : " + ticket.getId(), contentFont));
                document.add(new Paragraph("Statut : " + ticket.getStatus(), contentFont));
                document.add(new Paragraph(" "));

                // Placeholder pour le QR Code
                document.add(new Paragraph("[ Espace réservé pour le QR Code ]", headerFont));
                document.add(new Paragraph(ticket.getQrCodeValue(), contentFont));

                document.newPage();
            }
        } catch (DocumentException e) {
            log.error("Erreur lors de la génération du PDF pour les billets.", e);
        } finally {
            document.close();
        }

        log.info("PDF généré avec succès. Taille : {} bytes.", baos.size());
        return baos.toByteArray();
    }
}