package edu.cda.project.ticklybackend.services.interfaces;

import edu.cda.project.ticklybackend.models.ticket.Ticket;

import java.util.List;

/**
 * Service pour la génération de documents PDF.
 */
public interface PdfService {

    /**
     * Génère un fichier PDF contenant un ou plusieurs billets.
     *
     * @param tickets La liste des billets à inclure dans le PDF.
     * @return un tableau de bytes représentant le fichier PDF.
     */
    byte[] generateTicketsPdf(List<Ticket> tickets);
}