package edu.cda.project.ticklybackend.models.structure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Classe embarquée représentant l'adresse d'une structure.
 * Les champs de cette classe seront directement intégrés dans la table de l'entité Structure.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class StructureAddress {

    @NotBlank(message = "La rue ne peut pas être vide.")
    @Size(max = 255, message = "La rue ne peut pas dépasser 255 caractères.")
    @Column(nullable = false)
    private String street;

    @NotBlank(message = "La ville ne peut pas être vide.")
    @Size(max = 100, message = "La ville ne peut pas dépasser 100 caractères.")
    @Column(nullable = false)
    private String city;

    @NotBlank(message = "Le code postal ne peut pas être vide.")
    @Size(max = 20, message = "Le code postal ne peut pas dépasser 20 caractères.")
    @Column(nullable = false)
    private String zipCode;

    @NotBlank(message = "Le pays ne peut pas être vide.")
    @Size(max = 100, message = "Le pays ne peut pas dépasser 100 caractères.")
    @Column(nullable = false)
    private String country;

}