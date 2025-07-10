package edu.cda.project.ticklybackend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a request is invalid or malformed.
 */
public class BadRequestException extends BaseException {

    /**
     * Creates a new exception with the specified message.
     *
     * @param message the detail message
     */
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Creates a new exception with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST);
    }
}
