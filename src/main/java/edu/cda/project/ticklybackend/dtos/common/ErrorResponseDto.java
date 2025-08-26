package edu.cda.project.ticklybackend.dtos.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standardized error payload returned by API endpoints.
 * Includes HTTP status code, message, request path, timestamp, and optional validation errors.
 */
@Getter
@Setter
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto {

    private final int statusCode;
    private final String message;
    private final ZonedDateTime timestamp = ZonedDateTime.now();
    private String path;
    private List<Map<String, String>> errors;

    /**
     * Creates a simple error response without field-level validation errors.
     *
     * @param statusCode HTTP status code
     * @param message    human-readable error message
     * @param path       request path that caused the error
     */
    public ErrorResponseDto(int statusCode, String message, String path) {
        this.statusCode = statusCode;
        this.message = message;
        this.path = path;
    }

    /**
     * Creates an error response including validation details.
     *
     * @param statusCode HTTP status code
     * @param message    human-readable error message
     * @param path       request path that caused the error
     * @param errors     list of validation errors (e.g., field -> message)
     */
    public ErrorResponseDto(int statusCode, String message, String path, List<Map<String, String>> errors) {
        this.statusCode = statusCode;
        this.message = message;
        this.path = path;
        this.errors = errors;
    }
}