package edu.cda.project.ticklybackend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception levée lorsqu'un token de vérification est invalide, expiré, déjà utilisé ou non trouvé.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}