package edu.cda.project.ticklybackend.dtos.structure;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

import java.util.List;

/**
 * DTO pour la création d'une nouvelle structure.
 */
@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Schema(description = "DTO pour la création d'une nouvelle structure.")
public class StructureCreationDto {

    @NotBlank(message = "Le nom de la structure ne peut pas être vide.")
    @Size(max = 255, message = "Le nom de la structure ne doit pas dépasser 255 caractères.")
    @Schema(description = "Nom de la structure.", example = "Le Grand Théâtre de la Ville", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotEmpty(message = "Au moins un type de structure doit être sélectionné.")
    @Schema(description = "Liste des IDs des types de structure auxquels cette structure appartient.", example = "[1, 2]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> typeIds;

    @Schema(description = "Description textuelle détaillée de la structure (optionnel).", example = "Un théâtre historique offrant une programmation variée.")
    private String description;

    @NotNull(message = "L'adresse de la structure ne peut pas être nulle.")
    @Valid
    @Schema(description = "Adresse physique de la structure.", requiredMode = Schema.RequiredMode.REQUIRED)
    private AddressDto address;

    @Size(max = 30, message = "Le numéro de téléphone ne doit pas dépasser 30 caractères.")
    @Schema(description = "Numéro de téléphone de contact de la structure (optionnel).", example = "+33 1 23 45 67 89")
    private String phone;

    @Email(message = "Le format de l'email est invalide.")
    @Size(max = 255, message = "L'email ne doit pas dépasser 255 caractères.")
    @Schema(description = "Adresse e-mail de contact de la structure (optionnel).", example = "contact@grandtheatre.com")
    private String email;

    @URL(message = "L'URL du site web doit être une URL valide.")
    @Size(max = 2048, message = "L'URL du site web ne doit pas dépasser 2048 caractères.")
    @Schema(description = "URL du site web officiel de la structure (optionnel).", example = "https://www.grandtheatre.com")
    private String websiteUrl;

    @Schema(description = "Liste des URLs pointant vers les profils de la structure sur les réseaux sociaux (optionnel).")
    private List<@URL(message = "Chaque lien de réseau social doit être une URL valide.") @Size(max = 2048) String> socialMediaLinks;
}