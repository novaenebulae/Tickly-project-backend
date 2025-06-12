package edu.cda.project.ticklybackend.enums;

public enum EventStatus {
    DRAFT, // Événement en préparation, non visible publiquement
    PUBLISHED, // Événement publié et visible
    PENDING_APPROVAL, // En attente d'approbation (si un workflow de validation est mis en place)
    CANCELLED, // Événement annulé
    COMPLETED, // Événement terminé
    ARCHIVED // Événement archivé, non visible dans les listes actives
}