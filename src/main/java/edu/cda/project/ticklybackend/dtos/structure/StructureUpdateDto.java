package edu.cda.project.ticklybackend.dtos.structure;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.util.List;

/**
 * Partial update (PATCH) payload for an existing structure.
 * All fields are optional.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Partial update (PATCH) payload for an existing structure.")
public class StructureUpdateDto {

    @Size(max = 255, message = "Le nom de la structure ne doit pas dépasser 255 caractères.")
    @Schema(description = "New structure name (optional).", example = "Le Grand Théâtre Municipal")
    private String name;

    @Schema(description = "New list of structure type IDs (optional). If provided, replaces the existing list.", example = "[3, 4]")
    private List<Long> typeIds;

    @Schema(description = "New textual description (optional).", example = "A historic theatre recently renovated.")
    private String description;

    @Valid
    @Schema(description = "New physical address (optional).")
    private AddressDto address;

    @Size(max = 30, message = "Le numéro de téléphone ne doit pas dépasser 30 caractères.")
    @Schema(description = "New contact phone number (optional).", example = "+33 1 98 76 54 32")
    private String phone;

    @Email(message = "Le format de l'email est invalide.")
    @Size(max = 255, message = "L'email ne doit pas dépasser 255 caractères.")
    @Schema(description = "New contact email address (optional).", example = "info@grandtheatre-ville.com")
    private String email;

    @URL(message = "L'URL du site web doit être une URL valide.")
    @Size(max = 2048, message = "L'URL du site web ne doit pas dépasser 2048 caractères.")
    @Schema(description = "New official website URL (optional).", example = "https://www.grandtheatre-ville.com")
    private String websiteUrl;

    @Schema(description = "New list of social media URLs (optional). If provided, replaces the existing list.")
    private List<@URL(message = "Chaque lien de réseau social doit être une URL valide.") @Size(max = 2048) String> socialMediaLinks;

    @Schema(description = "New active status (optional).", example = "false")
    private Boolean isActive;
}