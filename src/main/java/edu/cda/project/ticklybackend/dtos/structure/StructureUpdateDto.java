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
 * DTO pour la mise à jour (PATCH) d'une structure existante.
 * Tous les champs sont optionnels.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor // Manque peut être .lombok
@Schema(description = "DTO pour la mise à jour (PATCH) d'une structure existante.")
public class StructureUpdateDto {

    @Size(max = 255, message = "Le nom de la structure ne doit pas dépasser 255 caractères.")
    @Schema(description = "Nouveau nom de la structure (optionnel).", example = "Le Grand Théâtre Municipal")
    private String name;

    @Schema(description = "Nouvelle liste des IDs des types de structure (optionnel). Si fournie, remplace l'existante.", example = "[3, 4]")
    private List<Long> typeIds;

    @Schema(description = "Nouvelle description textuelle de la structure (optionnel).", example = "Un théâtre historique récemment rénové.")
    private String description;

    @Valid
    @Schema(description = "Nouvelle adresse physique de la structure (optionnel).")
    private AddressDto address;

    @Size(max = 30, message = "Le numéro de téléphone ne doit pas dépasser 30 caractères.")
    @Schema(description = "Nouveau numéro de téléphone de contact (optionnel).", example = "+33 1 98 76 54 32")
    private String phone;

    @Email(message = "Le format de l'email est invalide.")
    @Size(max = 255, message = "L'email ne doit pas dépasser 255 caractères.")
    @Schema(description = "Nouvelle adresse e-mail de contact (optionnel).", example = "info@grandtheatre-ville.com")
    private String email;

    @URL(message = "L'URL du site web doit être une URL valide.")
    @Size(max = 2048, message = "L'URL du site web ne doit pas dépasser 2048 caractères.")
    @Schema(description = "Nouvelle URL du site web officiel (optionnel).", example = "https://www.grandtheatre-ville.com")
    private String websiteUrl;

    @Schema(description = "Nouvelle liste des URLs des réseaux sociaux (optionnel). Si fournie, remplace l'existante.")
    private List<@URL(message = "Chaque lien de réseau social doit être une URL valide.") @Size(max = 2048) String> socialMediaLinks;

    @Schema(description = "Nouveau statut d'activité de la structure (optionnel).", example = "false")
    private Boolean isActive;
}