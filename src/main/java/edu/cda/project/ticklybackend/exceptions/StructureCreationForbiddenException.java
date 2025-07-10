package edu.cda.project.ticklybackend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user is not allowed to create a structure.
 * This could be due to insufficient permissions, unverified email, or other restrictions.
 */
public class StructureCreationForbiddenException extends BaseException {

    /**
     * Creates a new exception with the specified message.
     *
     * @param message the detail message
     */
    public StructureCreationForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }

    /**
     * Creates a new exception with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public StructureCreationForbiddenException(String message, Throwable cause) {
        super(message, cause, HttpStatus.FORBIDDEN);
    }
}
