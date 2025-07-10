package edu.cda.project.ticklybackend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a verification token is invalid, expired, already used, or not found.
 */
public class InvalidTokenException extends BaseException {

    /**
     * Creates a new exception with the specified message.
     *
     * @param message the detail message
     */
    public InvalidTokenException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }

    /**
     * Creates a new exception with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause, HttpStatus.FORBIDDEN);
    }
}
