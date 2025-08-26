package edu.cda.project.ticklybackend.dtos.structure;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Detailed DTO for a structure, including contact, media, and metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO pour les détails complets d'une structure.")
public class StructureDetailResponseDto {

    @Schema(description = "ID unique de la structure.", example = "1")
    private Long id;

    @Schema(description = "Nom de la structure.", example = "Le Grand Théâtre de la Ville")
    private String name;

    @Schema(description = "Liste des types auxquels la structure appartient.")
    private List<StructureTypeDto> types;

    @Schema(description = "Description textuelle détaillée de la structure.", example = "Un théâtre historique offrant une programmation variée.")
    private String description;

    @Schema(description = "Adresse physique complète de la structure.")
    private AddressDto address;

    @Schema(description = "Numéro de téléphone de contact de la structure.", example = "+33 1 23 45 67 89")
    private String phone;

    @Schema(description = "Adresse e-mail de contact de la structure.", example = "contact@grandtheatre.com")
    private String email;

    @Schema(description = "URL du site web officiel de la structure.", example = "https://www.grandtheatre.com")
    private String websiteUrl;

    @Schema(description = "Liste des URLs pointant vers les profils de la structure sur les réseaux sociaux.")
    private List<String> socialMediaLinks;

    @Schema(description = "URL complète du logo de la structure.", example = "http://localhost/static/structures/logos/uuid-logo.jpg")
    private String logoUrl;

    @Schema(description = "URL complète de l'image de couverture de la structure.", example = "http://localhost/static/structures/covers/uuid-cover.jpg")
    private String coverUrl;

    @Schema(description = "Liste des URLs complètes des images composant la galerie de la structure.")
    private List<String> galleryImageUrls;

    @Schema(description = "Indicateur du statut d'activité de la structure.", example = "true")
    private boolean isActive;

    @Schema(description = "Date et heure de création de l'enregistrement de la structure.", example = "2024-07-15T10:30:00Z")
    private ZonedDateTime createdAt;

    @Schema(description = "Date et heure de la dernière modification de l'enregistrement de la structure.", example = "2024-07-16T14:45:00Z")
    private ZonedDateTime updatedAt;
}