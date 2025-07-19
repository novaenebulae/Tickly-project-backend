package edu.cda.project.ticklybackend.services.interfaces;

import java.util.List;
import java.util.UUID;

/**
 * Service pour gérer l'envoi de tous les e-mails transactionnels de l'application.
 */
public interface MailingService {


    /**
     * Envoie un e-mail de validation de compte à un nouvel utilisateur.
     *
     * @param to             L'adresse e-mail du destinataire.
     * @param validationLink Le lien sur lequel l'utilisateur doit cliquer pour valider son compte.
     */
    void sendEmailValidation(String to, String userName, String validationLink);

    /**
     * Envoie un e-mail avec un lien pour la réinitialisation du mot de passe.
     *
     * @param to        L'adresse e-mail du destinataire.
     * @param userName  Le nom de l'utilisateur.
     * @param resetLink Le lien pour réinitialiser le mot de passe.
     */
    void sendPasswordReset(String to, String userName, String resetLink);

    /**
     * Envoie un e-mail pour confirmer une demande de suppression de compte.
     *
     * @param to           L'adresse e-mail du destinataire.
     * @param userName     Le nom de l'utilisateur.
     * @param deletionLink Le lien pour confirmer la suppression.
     */
    void sendAccountDeletionRequest(String to, String userName, String deletionLink);

    /**
     * Envoie un e-mail d'invitation à rejoindre une équipe.
     *
     * @param to             L'adresse e-mail de la personne invitée.
     * @param inviterName    Le nom de la personne qui envoie l'invitation.
     * @param structureName  Le nom de la structure ou de l'équipe rejointe.
     * @param invitationLink Le lien pour accepter l'invitation.
     */
    void sendTeamInvitation(String to, String inviterName, String structureName, String invitationLink);

    /**
     * Envoie une confirmation de suppression de compte.
     *
     * @param to       L'adresse e-mail du destinataire.
     * @param userName Le nom de l'utilisateur dont le compte a été supprimé.
     */
    void sendAccountDeletionConfirmation(String to, String userName);

    /**
     * Envoie un ou plusieurs billets d'événement sous forme de liens vers l'application frontend.
     *
     * @param to        L'adresse e-mail du destinataire.
     * @param userName  Le nom du destinataire.
     * @param eventName Le nom de l'événement.
     * @param ticketIds Liste des UUIDs des billets à inclure dans l'email.
     * @param frontendBaseUrl URL de base de l'application frontend.
     */
    void sendTickets(String to, String userName, String eventName, List<UUID> ticketIds, String frontendBaseUrl);

    void sendEventCancelledNotification(String to, String userName, String eventName);

    void sendStructureDeletionConfirmation(String to, String adminName, String structureName);

    /**
     * Envoie un billet individuel par email à un participant sous forme de lien vers l'application frontend.
     *
     * @param to              Email du destinataire
     * @param participantName Nom complet du participant
     * @param eventName       Nom de l'événement
     * @param ticketId        UUID du billet à inclure dans l'email
     * @param frontendBaseUrl URL de base de l'application frontend
     */
    void sendIndividualTicket(String to, String participantName, String eventName, UUID ticketId, String frontendBaseUrl);

}