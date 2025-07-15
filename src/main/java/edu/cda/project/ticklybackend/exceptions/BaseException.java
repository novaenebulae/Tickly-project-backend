package edu.cda.project.ticklybackend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Base exception class for all application-specific exceptions.
 * Provides a consistent structure and behavior for all exceptions.
 */
public abstract class BaseException extends RuntimeException {
    
    private final HttpStatus status;
    
    /**
     * Creates a new exception with the specified message and HTTP status.
     *
     * @param message the detail message
     * @param status the HTTP status to return
     */
    protected BaseException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
    
    /**
     * Creates a new exception with the specified message, cause, and HTTP status.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     * @param status the HTTP status to return
     */
    protected BaseException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }
    
    /**
     * Returns the HTTP status associated with this exception.
     *
     * @return the HTTP status
     */
    public HttpStatus getStatus() {
        return status;
    }
}