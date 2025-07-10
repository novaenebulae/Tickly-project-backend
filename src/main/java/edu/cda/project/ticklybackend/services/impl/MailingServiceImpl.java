package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.services.interfaces.MailingService;
import edu.cda.project.ticklybackend.utils.LoggingUtils;
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
        LoggingUtils.logMethodEntry(log, "sendEmailValidation", "to", to, "userName", userName, "validationLink", validationLink);

        try {
            log.debug("Préparation de l'email de validation pour: {}", to);
            final String subject = "Bienvenue sur Tickly ! Validez votre adresse e-mail";
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("validationLink", frontendBaseUrl + validationLink);
            try {
                sendHtmlEmail(to, subject, "emails/email-validation.html", context, null, null);
                log.debug("Email de validation envoyé avec succès à: {}", to);
            } catch (Exception e) {
                LoggingUtils.logException(log, "Échec de l'envoi de l'email de validation à " + to, e);
            }

            LoggingUtils.logMethodExit(log, "sendEmailValidation");
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
            try {
                sendHtmlEmail(to, subject, "emails/password-reset-request.html", context, null, null);
                log.debug("Email de réinitialisation de mot de passe envoyé avec succès à: {}", to);
            } catch (Exception e) {
                LoggingUtils.logException(log, "Échec de l'envoi de l'email de réinitialisation de mot de passe à " + to, e);
            }

            LoggingUtils.logMethodExit(log, "sendPasswordReset");
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
            try {
                sendHtmlEmail(to, subject, "emails/account-deletion-request.html", context, null, null);
                log.debug("Email de demande de suppression de compte envoyé avec succès à: {}", to);
            } catch (Exception e) {
                LoggingUtils.logException(log, "Échec de l'envoi de l'email de demande de suppression de compte à " + to, e);
            }

            LoggingUtils.logMethodExit(log, "sendAccountDeletionRequest");
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
            try {
                sendHtmlEmail(to, subject, "emails/team-invitation-request.html", context, null, null);
                log.debug("Email d'invitation d'équipe envoyé avec succès à: {} pour la structure: {}", to, structureName);
            } catch (Exception e) {
                LoggingUtils.logException(log, "Échec de l'envoi de l'email d'invitation d'équipe à " + to + 
                        " pour la structure " + structureName, e);
            }

            LoggingUtils.logMethodExit(log, "sendTeamInvitation");
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
            try {
                sendHtmlEmail(to, subject, "emails/account-deletion-confirmation.html", context, null, null);
                log.debug("Email de confirmation de suppression de compte envoyé avec succès à: {}", to);
            } catch (Exception e) {
                LoggingUtils.logException(log, "Échec de l'envoi de l'email de confirmation de suppression de compte à " + to, e);
            }

            LoggingUtils.logMethodExit(log, "sendAccountDeletionConfirmation");
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
            try {
                sendHtmlEmail(to, subject, "emails/event-cancellation.html", context, null, null);
                log.debug("Email de notification d'annulation d'événement envoyé avec succès à: {} pour l'événement: {}", to, eventName);
            } catch (Exception e) {
                LoggingUtils.logException(log, "Échec de l'envoi de l'email de notification d'annulation d'événement à " + to + 
                        " pour l'événement " + eventName, e);
            }

            LoggingUtils.logMethodExit(log, "sendEventCancelledNotification");
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
            try {
                sendHtmlEmail(to, subject, "emails/structure-deletion-confirmation.html", context, null, null);
                log.debug("Email de confirmation de suppression de structure envoyé avec succès à: {} pour la structure: {}", to, structureName);
            } catch (Exception e) {
                LoggingUtils.logException(log, "Échec de l'envoi de l'email de confirmation de suppression de structure à " + to + 
                        " pour la structure " + structureName, e);
            }

            LoggingUtils.logMethodExit(log, "sendStructureDeletionConfirmation");
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
            try {
                sendHtmlEmail(to, subject, "emails/ticket-receipt.html", context, pdfAttachment, attachmentName);
                log.debug("Email avec billets envoyé avec succès à: {} pour l'événement: {}", to, eventName);
            } catch (Exception e) {
                LoggingUtils.logException(log, "Échec de l'envoi de l'email avec billets à " + to + 
                        " pour l'événement " + eventName, e);
            }

            LoggingUtils.logMethodExit(log, "sendTickets");
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
            try {
                sendHtmlEmail(to, subject, "emails/individual-ticket.html", context, pdfAttachment, attachmentName);
                log.debug("Email avec billet individuel envoyé avec succès à: {} pour l'événement: {}", to, eventName);
            } catch (Exception e) {
                LoggingUtils.logException(log, "Échec de l'envoi de l'email avec billet individuel à " + to + 
                        " pour l'événement " + eventName, e);
            }

            LoggingUtils.logMethodExit(log, "sendIndividualTicket");
        } finally {
            LoggingUtils.clearContext();
        }
    }

    private void sendHtmlEmail(String to, String subject, String templateName, Context context, byte[] attachment, String attachmentName) {
        LoggingUtils.logMethodEntry(log, "sendHtmlEmail", "to", to, "subject", subject, "templateName", templateName);

        try {
            log.debug("Début de la préparation de l'email '{}' pour: {} avec template: {}", subject, to, templateName);
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

                // Remplir le template HTML avec les variables
                context.setVariable("subject", subject);
                log.debug("Traitement du template HTML: {}", templateName);
                String html = templateEngine.process(templateName, context);
                if (html == null || html.isEmpty()) {
                    throw new MessagingException("Le template HTML généré est vide ou null");
                }

                helper.setTo(to);
                helper.setFrom(senderEmail);
                helper.setSubject(subject);
                helper.setText(html, true); // true indique que le contenu est HTML
                log.debug("Contenu HTML et métadonnées de l'email configurés");

                // Ajouter une pièce jointe si elle existe
                if (attachment != null && attachment.length > 0 && attachmentName != null) {
                    log.debug("Ajout d'une pièce jointe: {} (taille: {} octets)", attachmentName, attachment.length);
                    helper.addAttachment(attachmentName, new ByteArrayResource(attachment));
                    log.info("Pièce jointe '{}' ajoutée à l'e-mail pour {}", attachmentName, to);
                } else if (attachment != null && attachment.length == 0) {
                    log.warn("Tentative d'ajout d'une pièce jointe vide pour l'email à {}", to);
                }

                log.debug("Envoi de l'email à {}", to);
                mailSender.send(mimeMessage);
                log.info("E-mail '{}' envoyé avec succès à {}", subject, to);

            } catch (MessagingException e) {
                LoggingUtils.logException(log, "Échec de l'envoi de l'e-mail à " + to + " avec le sujet '" + subject + "'", e);
                throw new RuntimeException("Échec de l'envoi de l'e-mail: " + e.getMessage(), e);
            } catch (Exception e) {
                LoggingUtils.logException(log, "Erreur inattendue lors de l'envoi de l'e-mail à " + to + " avec le sujet '" + subject + "'", e);
                throw new RuntimeException("Erreur inattendue lors de l'envoi de l'e-mail: " + e.getMessage(), e);
            }

            LoggingUtils.logMethodExit(log, "sendHtmlEmail");
        } finally {
            // No need to clear context for private methods called by public methods that already handle context
        }
    }
}
