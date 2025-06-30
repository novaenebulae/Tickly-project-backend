package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.services.interfaces.MailingService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailingServiceImpl implements MailingService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${tickly.mail.sender}")
    private String senderEmail;

    @Value("${tickly.mail.frontend-base-url}")
    private String frontendBaseUrl;

    @Async
    @Override
    public void sendEmailValidation(String to, String userName, String validationLink) {
        final String subject = "Bienvenue sur Tickly ! Validez votre adresse e-mail";
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("validationLink", frontendBaseUrl + validationLink);
        sendHtmlEmail(to, subject, "emails/email-validation.html", context, null, null);
    }

    @Async
    @Override
    public void sendPasswordReset(String to, String userName, String resetLink) {
        final String subject = "Réinitialisation de votre mot de passe Tickly";
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("resetLink", frontendBaseUrl + resetLink);
        sendHtmlEmail(to, subject, "emails/password-reset-request.html", context, null, null);
    }

    @Async
    @Override
    public void sendAccountDeletionRequest(String to, String userName, String deletionLink) {
        final String subject = "Demande de suppression de votre compte Tickly";
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("deletionLink", frontendBaseUrl + deletionLink);
        sendHtmlEmail(to, subject, "emails/account-deletion-request.html", context, null, null);
    }

    @Async
    @Override
    public void sendTeamInvitation(String to, String inviterName, String structureName, String invitationLink) {
        final String subject = inviterName + " vous invite à rejoindre " + structureName + " sur Tickly";
        Context context = new Context();
        context.setVariable("inviterName", inviterName);
        context.setVariable("structureName", structureName);
        context.setVariable("invitationLink", frontendBaseUrl + invitationLink);
        sendHtmlEmail(to, subject, "emails/team-invitation-request.html", context, null, null);
    }

    @Async
    @Override
    public void sendAccountDeletionConfirmation(String to, String userName) {
        final String subject = "Confirmation de la suppression de votre compte Tickly";
        Context context = new Context();
        context.setVariable("userName", userName);
        sendHtmlEmail(to, subject, "emails/account-deletion-confirmation.html", context, null, null);
    }

    @Async
    @Override
    public void sendEventCancelledNotification(String to, String userName, String eventName) {
        final String subject = "Annulation de l'événement : " + eventName;
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("eventName", eventName);
        sendHtmlEmail(to, subject, "emails/event-cancellation.html", context, null, null);
    }

    @Async
    @Override
    public void sendStructureDeletionConfirmation(String to, String adminName, String structureName) {
        final String subject = "Confirmation de la suppression de votre structure : " + structureName;
        Context context = new Context();
        context.setVariable("adminName", adminName);
        context.setVariable("structureName", structureName);
        sendHtmlEmail(to, subject, "emails/structure-deletion-confirmation.html", context, null, null);
    }

    @Async
    @Override
    public void sendTickets(String to, String userName, String eventName, byte[] pdfAttachment) {
        final String subject = "Vos billets pour l'événement : " + eventName;
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("eventName", eventName);
        String attachmentName = "billets-" + eventName.replaceAll("\\s+", "_").toLowerCase() + ".pdf";
        sendHtmlEmail(to, subject, "emails/ticket-receipt.html", context, pdfAttachment, attachmentName);
    }

    @Async
    @Override
    public void sendIndividualTicket(String to, String participantName, String eventName, byte[] pdfAttachment) {
        final String subject = "Votre billet pour l'événement : " + eventName;
        Context context = new Context();
        context.setVariable("participantName", participantName);
        context.setVariable("eventName", eventName);
        String attachmentName = "billet-" + eventName.replaceAll("\\s+", "_").toLowerCase() + ".pdf";
        sendHtmlEmail(to, subject, "emails/individual-ticket.html", context, pdfAttachment, attachmentName);
    }

    private void sendHtmlEmail(String to, String subject, String templateName, Context context, byte[] attachment, String attachmentName) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

            // Remplir le template HTML avec les variables
            context.setVariable("subject", subject);
            String html = templateEngine.process(templateName, context);

            helper.setTo(to);
            helper.setFrom(senderEmail);
            helper.setSubject(subject);
            helper.setText(html, true); // true indique que le contenu est HTML

            // Ajouter une pièce jointe si elle existe
            if (attachment != null && attachment.length > 0 && attachmentName != null) {
                helper.addAttachment(attachmentName, new ByteArrayResource(attachment));
                log.info("Pièce jointe '{}' ajoutée à l'e-mail pour {}", attachmentName, to);
            }

            mailSender.send(mimeMessage);
            log.info("E-mail '{}' envoyé avec succès à {}", subject, to);

        } catch (MessagingException e) {
            log.error("Échec de l'envoi de l'e-mail à {} avec le sujet '{}'", to, subject, e);
        }
    }
}