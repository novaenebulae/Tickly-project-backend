package edu.cda.project.ticklybackend.dtos.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor // Crée un constructeur avec les champs finaux
@JsonInclude(JsonInclude.Include.NON_NULL) // N'inclut pas les champs nuls dans le JSON
public class ErrorResponseDto {

    private final int statusCode;
    private final String message;
    private final ZonedDateTime timestamp = ZonedDateTime.now(); // Horodatage automatique
    private String path; // Chemin de la requête ayant causé l'erreur
    private List<Map<String, String>> errors; // Pour les erreurs de validation multiples

    // Constructeur pour les erreurs simples
    public ErrorResponseDto(int statusCode, String message, String path) {
        this.statusCode = statusCode;
        this.message = message;
        this.path = path;
    }

    // Constructeur pour les erreurs de validation
    public ErrorResponseDto(int statusCode, String message, String path, List<Map<String, String>> errors) {
        this.statusCode = statusCode;
        this.message = message;
        this.path = path;
        this.errors = errors;
    }
}