package edu.cda.project.ticklybackend.services.interfaces;

import edu.cda.project.ticklybackend.dtos.ticket.TicketResponseDto;
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

    /**
     * Génère un fichier PDF contenant un ou plusieurs billets à partir des DTOs.
     *
     * @param ticketDtos La liste des DTOs de billets à inclure dans le PDF.
     * @return un tableau de bytes représentant le fichier PDF.
     */
    byte[] generateTicketsPdfFromDto(List<TicketResponseDto> ticketDtos);

    /**
     * Génère un fichier PDF pour un seul billet à partir du DTO.
     *
     * @param ticketDto Le DTO du billet à inclure dans le PDF.
     * @return un tableau de bytes représentant le fichier PDF.
     */
    byte[] generateSingleTicketPdfFromDto(TicketResponseDto ticketDto);
}