package edu.cda.project.ticklybackend.dtos.structure;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

import java.util.List;

/**
 * DTO used to create a new structure with contact details, address, and media links.
 */
@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Schema(description = "Payload to create a new structure.")
public class StructureCreationDto {

    @NotBlank(message = "Le nom de la structure ne peut pas être vide.")
    @Size(max = 255, message = "Le nom de la structure ne doit pas dépasser 255 caractères.")
    @Schema(description = "Structure name.", example = "Le Grand Théâtre de la Ville", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotEmpty(message = "Au moins un type de structure doit être sélectionné.")
    @Schema(description = "IDs of structure types associated with this structure.", example = "[1, 2]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> typeIds;

    @Schema(description = "Detailed textual description (optional).", example = "A historic theatre offering a diverse program.")
    private String description;

    @NotNull(message = "L'adresse de la structure ne peut pas être nulle.")
    @Valid
    @Schema(description = "Physical address of the structure.", requiredMode = Schema.RequiredMode.REQUIRED)
    private AddressDto address;

    @Size(max = 30, message = "Le numéro de téléphone ne doit pas dépasser 30 caractères.")
    @Schema(description = "Contact phone number (optional).", example = "+33 1 23 45 67 89")
    private String phone;

    @Email(message = "Le format de l'email est invalide.")
    @Size(max = 255, message = "L'email ne doit pas dépasser 255 caractères.")
    @Schema(description = "Contact email address (optional).", example = "contact@grandtheatre.com")
    private String email;

    @URL(message = "L'URL du site web doit être une URL valide.")
    @Size(max = 2048, message = "L'URL du site web ne doit pas dépasser 2048 caractères.")
    @Schema(description = "Official website URL (optional).", example = "https://www.grandtheatre.com")
    private String websiteUrl;

    @Schema(description = "Social media profile URLs (optional).")
    private List<@URL(message = "Chaque lien de réseau social doit être une URL valide.") @Size(max = 2048) String> socialMediaLinks;
}