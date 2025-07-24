package edu.cda.project.ticklybackend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

/**
 * Exception thrown when there is an issue with refresh token operations.
 * This could be due to an expired token, a revoked token, or a token reuse attempt.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class TokenRefreshException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new TokenRefreshException with the specified token and message.
     *
     * @param token   the token that caused the exception
     * @param message the error message
     */
    public TokenRefreshException(String token, String message) {
        super(String.format("Failed for [%s]: %s", token, message));
    }

    /**
     * Creates a new TokenRefreshException with the specified message.
     *
     * @param message the error message
     */
    public TokenRefreshException(String message) {
        super(message);
    }
}