package edu.cda.project.ticklybackend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when attempting to register with an email that already exists.
 */
public class EmailAlreadyExistsException extends BaseException {

    /**
     * Creates a new exception with a formatted message.
     *
     * @param email the email address that already exists
     */
    public EmailAlreadyExistsException(String email) {
        super("Un compte existe déjà avec l'adresse e-mail : " + email, HttpStatus.CONFLICT);
    }
}
