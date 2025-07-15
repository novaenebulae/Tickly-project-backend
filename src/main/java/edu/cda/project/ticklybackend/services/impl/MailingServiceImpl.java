package edu.cda.project.ticklybackend.services.impl;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import edu.cda.project.ticklybackend.services.interfaces.MailingService;
import edu.cda.project.ticklybackend.utils.LoggingUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailingServiceImpl implements MailingService {

    // Supprimé : private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${tickly.mail.sender}")
    private String senderEmail;

    @Value("${tickly.mail.frontend-base-url}")
    private String frontendBaseUrl;

    // Ajouté : Propriétés pour les identifiants client OAuth 2.0
    @Value("${tickly.mail.gmail.client-id}")
    private String clientId;

    @Value("${tickly.mail.gmail.client-secret}")
    private String clientSecret;

    @Value("${tickly.mail.gmail.refresh-token}")
    private String refreshToken;


    // Méthodes publiques de l'interface (INCHANGÉES)
    // Elles appellent toutes la méthode privée sendHtmlEmail

    @Async
    @Override
    public void sendEmailValidation(String to, String userName, String validationLink) {
        LoggingUtils.logMethodEntry(log, "sendEmailValidation", "to", to, "userName", userName, "validationLink", validationLink);
        try {
            log.debug("Préparation de l'email de validation pour: {}", to);
            final String subject = "Bienvenue sur Tickly ! Validez votre adresse e-mail";
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("validationLink", frontendBaseUrl + validationLink);
            sendHtmlEmail(to, subject, "emails/email-validation.html", context, null, null);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Échec de la préparation de l'email de validation à " + to, e);
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Async
    @Override
    public void sendPasswordReset(String to, String userName, String resetLink) {
        LoggingUtils.logMethodEntry(log, "sendPasswordReset", "to", to, "userName", userName, "resetLink", resetLink);
        try {
            log.debug("Préparation de l'email de réinitialisation de mot de passe pour: {}", to);
            final String subject = "Réinitialisation de votre mot de passe Tickly";
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("resetLink", frontendBaseUrl + resetLink);
            sendHtmlEmail(to, subject, "emails/password-reset-request.html", context, null, null);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Échec de la préparation de l'email de réinitialisation de mot de passe à " + to, e);
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Async
    @Override
    public void sendAccountDeletionRequest(String to, String userName, String deletionLink) {
        LoggingUtils.logMethodEntry(log, "sendAccountDeletionRequest", "to", to, "userName", userName, "deletionLink", deletionLink);
        try {
            log.debug("Préparation de l'email de demande de suppression de compte pour: {}", to);
            final String subject = "Demande de suppression de votre compte Tickly";
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("deletionLink", frontendBaseUrl + deletionLink);
            sendHtmlEmail(to, subject, "emails/account-deletion-request.html", context, null, null);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Échec de l'envoi de l'email de demande de suppression de compte à " + to, e);
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Async
    @Override
    public void sendTeamInvitation(String to, String inviterName, String structureName, String invitationLink) {
        LoggingUtils.logMethodEntry(log, "sendTeamInvitation", "to", to, "inviterName", inviterName,
                "structureName", structureName, "invitationLink", invitationLink);
        try {
            log.debug("Préparation de l'email d'invitation d'équipe pour: {} (structure: {})", to, structureName);
            final String subject = inviterName + " vous invite à rejoindre " + structureName + " sur Tickly";
            Context context = new Context();
            context.setVariable("inviterName", inviterName);
            context.setVariable("structureName", structureName);
            context.setVariable("invitationLink", frontendBaseUrl + invitationLink);
            sendHtmlEmail(to, subject, "emails/team-invitation-request.html", context, null, null);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Échec de l'envoi de l'email d'invitation d'équipe à " + to +
                    " pour la structure " + structureName, e);
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Async
    @Override
    public void sendAccountDeletionConfirmation(String to, String userName) {
        LoggingUtils.logMethodEntry(log, "sendAccountDeletionConfirmation", "to", to, "userName", userName);
        try {
            log.debug("Préparation de l'email de confirmation de suppression de compte pour: {}", to);
            final String subject = "Confirmation de la suppression de votre compte Tickly";
            Context context = new Context();
            context.setVariable("userName", userName);
            sendHtmlEmail(to, subject, "emails/account-deletion-confirmation.html", context, null, null);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Échec de l'envoi de l'email de confirmation de suppression de compte à " + to, e);
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Async
    @Override
    public void sendEventCancelledNotification(String to, String userName, String eventName) {
        LoggingUtils.logMethodEntry(log, "sendEventCancelledNotification", "to", to, "userName", userName, "eventName", eventName);
        try {
            log.debug("Préparation de l'email de notification d'annulation d'événement pour: {} (événement: {})", to, eventName);
            final String subject = "Annulation de l'événement : " + eventName;
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("eventName", eventName);
            sendHtmlEmail(to, subject, "emails/event-cancellation.html", context, null, null);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Échec de l'envoi de l'email de notification d'annulation d'événement à " + to +
                    " pour l'événement " + eventName, e);
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Async
    @Override
    public void sendStructureDeletionConfirmation(String to, String adminName, String structureName) {
        LoggingUtils.logMethodEntry(log, "sendStructureDeletionConfirmation", "to", to, "adminName", adminName, "structureName", structureName);
        try {
            log.debug("Préparation de l'email de confirmation de suppression de structure pour: {} (structure: {})", to, structureName);
            final String subject = "Confirmation de la suppression de votre structure : " + structureName;
            Context context = new Context();
            context.setVariable("adminName", adminName);
            context.setVariable("structureName", structureName);
            sendHtmlEmail(to, subject, "emails/structure-deletion-confirmation.html", context, null, null);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Échec de l'envoi de l'email de confirmation de suppression de structure à " + to +
                    " pour la structure " + structureName, e);
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Async
    @Override
    public void sendTickets(String to, String userName, String eventName, byte[] pdfAttachment) {
        LoggingUtils.logMethodEntry(log, "sendTickets", "to", to, "userName", userName, "eventName", eventName);
        try {
            log.debug("Préparation de l'email d'envoi de billets pour: {} (événement: {})", to, eventName);
            final String subject = "Vos billets pour l'événement : " + eventName;
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("eventName", eventName);
            String attachmentName = "billets-" + eventName.replaceAll("\\s+", "_").toLowerCase() + ".pdf";
            sendHtmlEmail(to, subject, "emails/ticket-receipt.html", context, pdfAttachment, attachmentName);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Échec de l'envoi de l'email avec billets à " + to +
                    " pour l'événement " + eventName, e);
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Async
    @Override
    public void sendIndividualTicket(String to, String participantName, String eventName, byte[] pdfAttachment) {
        LoggingUtils.logMethodEntry(log, "sendIndividualTicket", "to", to, "participantName", participantName, "eventName", eventName);
        try {
            log.debug("Préparation de l'email d'envoi de billet individuel pour: {} (participant: {}, événement: {})", to, participantName, eventName);
            final String subject = "Votre billet pour l'événement : " + eventName;
            Context context = new Context();
            context.setVariable("participantName", participantName);
            context.setVariable("eventName", eventName);
            String attachmentName = "billet-" + eventName.replaceAll("\\s+", "_").toLowerCase() + ".pdf";
            sendHtmlEmail(to, subject, "emails/individual-ticket.html", context, pdfAttachment, attachmentName);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Échec de l'envoi de l'email avec billet individuel à " + to +
                    " pour l'événement " + eventName, e);
        } finally {
            LoggingUtils.clearContext();
        }
    }


    /**
     * Ajouté : Construit et retourne un service Gmail authentifié via OAuth 2.0.
     */
    private Gmail getGmailService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .setClientSecrets(clientId, clientSecret)
                .build()
                .setRefreshToken(refreshToken);
        return new Gmail.Builder(httpTransport, GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("Tickly Backend")
                .build();
    }


    /**
     * MODIFIÉ : Cette méthode utilise maintenant le client Gmail au lieu de JavaMailSender.
     */
    private void sendHtmlEmail(String to, String subject, String templateName, Context context, byte[] attachment, String attachmentName) {
        LoggingUtils.logMethodEntry(log, "sendHtmlEmail", "to", to, "subject", subject, "templateName", templateName);
        try {
            log.debug("Début de la préparation de l'email '{}' pour: {} avec template: {}", subject, to, templateName);

            Gmail service = getGmailService();
            String htmlContent = templateEngine.process(templateName, context);

            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage mimeMessage = new MimeMessage(session);

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true indique que le contenu est HTML

            if (attachment != null && attachment.length > 0 && attachmentName != null) {
                log.debug("Ajout d'une pièce jointe: {} (taille: {} octets)", attachmentName, attachment.length);
                helper.addAttachment(attachmentName, new ByteArrayResource(attachment));
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            mimeMessage.writeTo(buffer);
            String encodedEmail = Base64.getUrlEncoder().encodeToString(buffer.toByteArray());

            Message message = new Message();
            message.setRaw(encodedEmail);

            service.users().messages().send("me", message).execute();
            log.info("E-mail '{}' envoyé avec succès à {} via Gmail API.", subject, to);

        } catch (MessagingException | IOException | GeneralSecurityException e) {
            LoggingUtils.logException(log, "Échec de l'envoi de l'e-mail à " + to + " avec le sujet '" + subject + "'", e);
            throw new RuntimeException("Échec de l'envoi de l'e-mail: " + e.getMessage(), e);
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur inattendue lors de l'envoi de l'e-mail à " + to + " avec le sujet '" + subject + "'", e);
            throw new RuntimeException("Erreur inattendue lors de l'envoi de l'e-mail: " + e.getMessage(), e);
        } finally {
            LoggingUtils.logMethodExit(log, "sendHtmlEmail");
        }
    }
}