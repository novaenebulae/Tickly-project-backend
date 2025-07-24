package edu.cda.project.ticklybackend.enums;

public enum EventStatus {
    DRAFT, // Événement en préparation, non visible publiquement
    PUBLISHED, // Événement publié et visible
    CANCELLED, // Événement annulé
    COMPLETED, // Événement terminé
    ARCHIVED // Événement archivé, non visible dans les listes actives
}