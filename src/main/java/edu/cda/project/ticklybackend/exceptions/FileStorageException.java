package edu.cda.project.ticklybackend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there is an error storing or retrieving files.
 */
public class FileStorageException extends BaseException {

    /**
     * Creates a new exception with the specified message.
     *
     * @param message the detail message
     */
    public FileStorageException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Creates a new exception with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
