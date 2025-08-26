package edu.cda.project.ticklybackend.dtos.structure;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Address information used for structure creation and responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO pour les informations d'adresse.")
public class AddressDto {

    @NotBlank(message = "La rue ne peut pas être vide.")
    @Size(max = 255, message = "La rue ne doit pas dépasser 255 caractères.")
    @Schema(description = "Numéro et nom de la rue.", example = "123 Rue de la Paix", requiredMode = Schema.RequiredMode.REQUIRED)
    private String street;

    @NotBlank(message = "La ville ne peut pas être vide.")
    @Size(max = 100, message = "La ville ne doit pas dépasser 100 caractères.")
    @Schema(description = "Ville.", example = "Paris", requiredMode = Schema.RequiredMode.REQUIRED)
    private String city;

    @NotBlank
    @Size(max = 20, message = "Le code postal ne doit pas dépasser 20 caractères.")
    @Schema(description = "Code postal.", example = "75001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String zipCode;

    @NotBlank(message = "Le pays ne peut pas être vide.")
    @Size(max = 100, message = "Le pays ne doit pas dépasser 100 caractères.")
    @Schema(description = "Pays.", example = "France", requiredMode = Schema.RequiredMode.REQUIRED)
    private String country;

}