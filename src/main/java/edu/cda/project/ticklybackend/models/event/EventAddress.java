package edu.cda.project.ticklybackend.models.event;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Classe embarquable représentant une adresse spécifique à un événement.
 * Permet à un événement d'avoir une adresse différente de celle de sa structure parente
 * (ex: un festival en plein air).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
//TODO:
public class EventAddress {
    /**
     * Numéro et nom de la rue.
     */
    private String street;
    /**
     * Ville.
     */
    private String city;
    /**
     * Code postal.
     */
    private String zipCode;
    /**
     * Pays.
     */
    private String country;

}