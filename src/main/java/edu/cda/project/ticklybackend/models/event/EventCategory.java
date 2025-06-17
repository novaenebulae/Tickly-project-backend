package edu.cda.project.ticklybackend.models.event;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Représente une catégorie d'événement (ex: "Concert", "Théâtre", "Sport").
 * Cette entité permet de classer et de filtrer les événements.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "event_categories")
public class EventCategory {

    /**
     * Identifiant unique de la catégorie.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom de la catégorie. Doit être unique.
     */
    @Column(nullable = false, unique = true)
    private String name;
}