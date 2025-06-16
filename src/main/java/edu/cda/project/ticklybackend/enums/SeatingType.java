package edu.cda.project.ticklybackend.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Énumération des types de placement pour les zones d'audience.
 */
@Schema(description = "Type de placement pour une zone d'audience.")
public enum SeatingType {
    @Schema(description = "Places assises numérotées ou non.")
    SEATED,
    @Schema(description = "Placement libre debout.")
    STANDING,
    @Schema(description = "Combinaison de places assises et debout, ou non spécifié.")
    MIXED
}