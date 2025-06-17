package edu.cda.project.ticklybackend.services.interfaces;

import edu.cda.project.ticklybackend.dtos.common.PaginatedResponseDto;
import edu.cda.project.ticklybackend.dtos.event.*;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Interface définissant les opérations de la logique métier pour la gestion des événements.
 */
public interface EventService {

    /**
     * Crée un nouvel événement.
     *
     * @param creationDto DTO contenant les informations de création.
     * @return Le DTO détaillé de l'événement créé.
     */
    EventDetailResponseDto createEvent(EventCreationDto creationDto);

    /**
     * Recherche et filtre les événements de manière paginée.
     *
     * @param params   DTO contenant les paramètres de recherche.
     * @param pageable Informations de pagination et de tri.
     * @return Une réponse paginée contenant des DTOs résumés des événements.
     */
    PaginatedResponseDto<EventSummaryDto> searchEvents(EventSearchParamsDto params, Pageable pageable);

    /**
     * Récupère les détails complets d'un événement par son ID.
     *
     * @param eventId ID de l'événement.
     * @return Le DTO détaillé de l'événement.
     * @throws edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException si l'événement n'est pas trouvé.
     */
    EventDetailResponseDto getEventById(Long eventId);

    /**
     * Met à jour un événement existant.
     *
     * @param eventId   ID de l'événement à mettre à jour.
     * @param updateDto DTO contenant les champs à mettre à jour.
     * @return Le DTO détaillé de l'événement mis à jour.
     */
    EventDetailResponseDto updateEvent(Long eventId, EventUpdateDto updateDto);

    /**
     * Supprime un événement et tous les fichiers associés.
     *
     * @param eventId ID de l'événement à supprimer.
     */
    void deleteEvent(Long eventId);

    /**
     * Met à jour le statut d'un événement.
     *
     * @param eventId         ID de l'événement.
     * @param statusUpdateDto DTO contenant le nouveau statut.
     * @return Le DTO détaillé de l'événement mis à jour.
     */
    EventDetailResponseDto updateEventStatus(Long eventId, EventStatusUpdateDto statusUpdateDto);

    /**
     * Met à jour la photo principale d'un événement.
     * Supprime l'ancienne photo si elle existe.
     *
     * @param eventId ID de l'événement.
     * @param file    Le nouveau fichier image.
     * @return L'URL complète de la nouvelle photo.
     */
    String updateEventMainPhoto(Long eventId, MultipartFile file);

    /**
     * Ajoute une image à la galerie d'un événement.
     *
     * @param eventId ID de l'événement.
     * @param file    Le fichier image à ajouter.
     * @return L'URL complète de l'image ajoutée.
     */
    String addEventGalleryImage(Long eventId, MultipartFile file);

    /**
     * Supprime une image de la galerie d'un événement.
     *
     * @param eventId   ID de l'événement.
     * @param imagePath Le chemin/nom du fichier à supprimer.
     */
    void removeEventGalleryImage(Long eventId, String imagePath);

    /**
     * Récupère toutes les catégories d'événements disponibles.
     *
     * @return Une liste de DTOs de catégories.
     */
    List<EventCategoryDto> getAllCategories();
}
