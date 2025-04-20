package edu.cda.project.ticklybackend.exception; // Adaptez le package si nécessaire

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception levée lorsqu'une tentative d'enregistrement d'utilisateur échoue
 * car l'adresse email fournie existe déjà dans le système.
 * <p>
 * L'annotation @ResponseStatus(HttpStatus.CONFLICT) fait en sorte que Spring retourne
 * automatiquement une réponse HTTP 409 lorsque cette exception atteint la couche web
 * sans être gérée explicitement par un @ExceptionHandler.
 */
@ResponseStatus(HttpStatus.CONFLICT) // <<<=== Associe l'exception au statut HTTP 409
public class EmailAlreadyExistsException extends RuntimeException { // Hérite de RuntimeException (non cochée)

    /**
     * Constructeur prenant un message d'erreur détaillé.
     *
     * @param message Le message expliquant l'erreur (ex: quel email est déjà utilisé).
     */
    public EmailAlreadyExistsException(String message) {
        super(message); // Passe le message au constructeur de RuntimeException
    }

    /**
     * Constructeur prenant un message et la cause originale (moins utilisé ici).
     *
     * @param message Le message d'erreur.
     * @param cause   L'exception originale qui a causé ce problème.
     */
    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
