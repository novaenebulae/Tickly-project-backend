package edu.cda.project.ticklybackend.security;

import edu.cda.project.ticklybackend.enums.EventStatus;
import edu.cda.project.ticklybackend.models.event.Event;
import edu.cda.project.ticklybackend.models.user.StaffUser;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.event.EventRepository;
import edu.cda.project.ticklybackend.utils.AuthUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service("eventSecurityService")
@RequiredArgsConstructor
public class EventSecurityService {

    private final EventRepository eventRepository;
    private final AuthUtils authUtils;

    /**
     * Ajoute les filtres de sécurité pour la recherche d'événements selon le rôle de l'utilisateur
     */
    public <T> Specification<T> addSecurityFilters(Specification<T> existingSpec) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Appliquer la spécification existante
            if (existingSpec != null) {
                Predicate existingPredicate = existingSpec.toPredicate(root, query, criteriaBuilder);
                if (existingPredicate != null) {
                    predicates.add(existingPredicate);
                }
            }

            // Ajouter les filtres de sécurité
            Predicate securityPredicate = buildSecurityPredicate(root, criteriaBuilder);
            if (securityPredicate != null) {
                predicates.add(securityPredicate);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Construit le prédicat de sécurité basé sur le rôle de l'utilisateur
     */
    private <T> Predicate buildSecurityPredicate(jakarta.persistence.criteria.Root<T> root,
                                                 jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder) {
        // Exclure les événements supprimés (soft deleted)
        Predicate notDeletedPredicate = criteriaBuilder.equal(root.get("deleted"), false);

        try {
            User currentUser = authUtils.getCurrentAuthenticatedUser();
            log.debug("Construction du prédicat de sécurité pour l'utilisateur {} (type: {}, role: {})",
                    currentUser.getEmail(), currentUser.getClass().getSimpleName(), currentUser.getRole());

            // Vérifier si l'utilisateur est un administrateur de structure
            if (currentUser.getRole() != null && currentUser.getRole().name().equals("STRUCTURE_ADMINISTRATOR")) {
                if (currentUser.getStructure() != null) {
                    Long structureId = currentUser.getStructure().getId();
                    log.debug("Administrateur de structure {} : peut voir tous les événements de sa structure + événements publiés des autres", structureId);

                    // Peut voir tous les événements de sa structure + événements publiés des autres
                    Predicate rolePredicate = criteriaBuilder.or(
                            criteriaBuilder.equal(root.get("structure").get("id"), structureId),
                            criteriaBuilder.equal(root.get("status"), EventStatus.PUBLISHED)
                    );

                    // Combiner avec le prédicat "non supprimé"
                    return criteriaBuilder.and(notDeletedPredicate, rolePredicate);
                }
            }

            // Si l'utilisateur est staff d'une structure, il peut voir tous les événements de sa structure
            if (isStructureStaff(currentUser)) {
                StaffUser staffUser = (StaffUser) currentUser;
                Long structureId = staffUser.getStructure().getId();
                log.debug("Staff de structure {} : peut voir tous les événements de sa structure + événements publiés des autres", structureId);

                // Peut voir tous les événements de sa structure + événements publiés des autres
                Predicate rolePredicate = criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("structure").get("id"), structureId),
                        criteriaBuilder.equal(root.get("status"), EventStatus.PUBLISHED)
                );

                // Combiner avec le prédicat "non supprimé"
                return criteriaBuilder.and(notDeletedPredicate, rolePredicate);
            }

            // Pour les utilisateurs non-staff : seulement les événements publiés
            Predicate publishedPredicate = criteriaBuilder.equal(root.get("status"), EventStatus.PUBLISHED);
            return criteriaBuilder.and(notDeletedPredicate, publishedPredicate);

        } catch (Exception e) {
            // Si pas d'utilisateur connecté (endpoint public), seulement les événements publiés
            log.debug("Aucun utilisateur authentifié, application du filtre public");
            Predicate publishedPredicate = criteriaBuilder.equal(root.get("status"), EventStatus.PUBLISHED);
            return criteriaBuilder.and(notDeletedPredicate, publishedPredicate);
        }
    }

    /**
     * Vérifie si l'utilisateur peut accéder aux détails d'un événement spécifique
     *
     * @param eventId     ID de l'événement
     * @param eventStatus Statut de l'événement
     * @param structureId ID de la structure associée à l'événement
     * @param isDeleted   Indique si l'événement est marqué comme supprimé
     * @return true si l'accès est autorisé, false sinon
     */
    public boolean canAccessEventDetails(Long eventId, EventStatus eventStatus, Long structureId, boolean isDeleted) {
        // Les événements supprimés ne sont pas accessibles
        if (isDeleted) {
            log.debug("Accès refusé : Événement {} est marqué comme supprimé", eventId);
            return false;
        }

        try {
            User currentUser = authUtils.getCurrentAuthenticatedUser();
            log.debug("Vérification d'accès pour l'utilisateur {} (type: {}, role: {}) à l'événement {} (statut: {}, structure: {})",
                    currentUser.getEmail(), currentUser.getClass().getSimpleName(), currentUser.getRole(), 
                    eventId, eventStatus, structureId);

            // Vérifier si l'utilisateur est un administrateur de structure
            if (currentUser.getRole() != null && currentUser.getRole().name().equals("STRUCTURE_ADMINISTRATOR")) {
                if (currentUser.getStructure() != null && currentUser.getStructure().getId().equals(structureId)) {
                    log.debug("Accès autorisé : Administrateur de la structure {} pour l'événement {} (statut: {})", 
                            structureId, eventId, eventStatus);
                    return true;
                }
            }

            // Staff de la structure : accès total à ses propres événements
            if (isStructureStaff(currentUser)) {
                log.debug("Utilisateur {} est un staff avec structure {}", 
                        currentUser.getEmail(), 
                        currentUser.getStructure() != null ? currentUser.getStructure().getId() : "null");

                if (currentUser.getStructure() != null && currentUser.getStructure().getId().equals(structureId)) {
                    log.debug("Accès autorisé : Staff de la structure {} pour l'événement {} (statut: {})", 
                            structureId, eventId, eventStatus);
                    return true;
                }

                // Staff d'une autre structure : seulement PUBLISHED
                boolean canAccess = eventStatus == EventStatus.PUBLISHED;
                log.debug("Accès {} pour staff d'une autre structure à l'événement {} (statut: {})",
                        canAccess ? "autorisé" : "refusé", eventId, eventStatus);
                return canAccess;
            }

            // Utilisateurs non-staff : seulement PUBLISHED et CANCELLED
            boolean canAccess = eventStatus == EventStatus.PUBLISHED || eventStatus == EventStatus.CANCELLED;
            log.debug("Accès {} pour utilisateur non-staff à l'événement {} (statut: {})",
                    canAccess ? "autorisé" : "refusé", eventId, eventStatus);
            return canAccess;

        } catch (Exception e) {
            // Utilisateur non connecté : seulement PUBLISHED
            boolean canAccess = eventStatus == EventStatus.PUBLISHED;
            log.debug("Accès {} pour utilisateur anonyme à l'événement {} (statut: {})",
                    canAccess ? "autorisé" : "refusé", eventId, eventStatus);
            return canAccess;
        }
    }

    /**
     * Surcharge de la méthode pour la compatibilité avec le code existant
     */
    public boolean canAccessEventDetails(Long eventId, EventStatus eventStatus, Long structureId) {
        return canAccessEventDetails(eventId, eventStatus, structureId, false);
    }

    @Transactional(readOnly = true)
    public boolean isOwner(Long eventId, UserDetails principal) {
        // Vérifier si l'utilisateur est un administrateur de structure
        if (principal instanceof User) {
            User user = (User) principal;
            if (user.getRole() != null && user.getRole().name().equals("STRUCTURE_ADMINISTRATOR")) {
                log.debug("Utilisateur {} est un administrateur de structure", user.getUsername());

                // Try to find the event with the given ID
                Optional<Event> eventOptional = eventRepository.findById(eventId);

                // If not found, try with ID-1 (to handle potential ID shift after deletion)
                if (eventOptional.isEmpty() && eventId > 1) {
                    log.debug("Événement avec ID {} non trouvé pour vérification propriétaire, tentative avec ID {}", eventId, eventId - 1);
                    eventOptional = eventRepository.findById(eventId - 1);
                }

                // Get the event if found
                Event event = eventOptional.orElse(null);

                // Vérifications de base
                if (event == null || event.getStructure() == null || user.getStructure() == null || event.isDeleted()) {
                    log.debug("Événement {} non valide ou supprimé, accès refusé", eventId);
                    return false;
                }

                // Vérifie si l'ID de la structure de l'événement correspond à l'ID de la structure de l'utilisateur
                boolean isOwner = event.getStructure().getId().equals(user.getStructure().getId());
                log.debug("Vérification propriétaire pour l'administrateur de structure: structure de l'événement = {}, structure de l'utilisateur = {}, résultat = {}",
                        event.getStructure().getId(), user.getStructure().getId(), isOwner ? "autorisé" : "refusé");

                return isOwner;
            }
        }

        if (!(principal instanceof StaffUser staffUser)) {
            log.debug("Utilisateur {} n'est pas un staff, accès refusé", principal.getUsername());
            return false; // Seuls les membres du staff peuvent être propriétaires
        }

        log.debug("Vérification si l'utilisateur {} (type: {}) est propriétaire de l'événement {}",
                staffUser.getUsername(), staffUser.getClass().getSimpleName(), eventId);

        // Try to find the event with the given ID
        Optional<Event> eventOptional = eventRepository.findById(eventId);

        // If not found, try with ID-1 (to handle potential ID shift after deletion)
        if (eventOptional.isEmpty() && eventId > 1) {
            log.debug("Événement avec ID {} non trouvé pour vérification propriétaire, tentative avec ID {}", eventId, eventId - 1);
            eventOptional = eventRepository.findById(eventId - 1);
        }

        // Get the event if found
        Event event = eventOptional.orElse(null);

        // Vérifications de base
        if (event == null) {
            log.debug("Événement {} non trouvé, accès refusé", eventId);
            return false;
        }

        if (event.getStructure() == null) {
            log.debug("Événement {} n'a pas de structure associée, accès refusé", eventId);
            return false;
        }

        if (staffUser.getStructure() == null) {
            log.debug("Utilisateur {} n'a pas de structure associée, accès refusé", staffUser.getUsername());
            return false;
        }

        if (event.isDeleted()) {
            log.debug("Événement {} est marqué comme supprimé, accès refusé", eventId);
            return false;
        }

        // Vérifie si l'ID de la structure de l'événement correspond à l'ID de la structure de l'utilisateur
        boolean isOwner = event.getStructure().getId().equals(staffUser.getStructure().getId());
        log.debug("Vérification propriétaire pour l'événement {}: structure de l'événement = {}, structure de l'utilisateur = {}, résultat = {}",
                eventId, event.getStructure().getId(), staffUser.getStructure().getId(), isOwner ? "autorisé" : "refusé");

        return isOwner;
    }

    /**
     * Vérifie si l'utilisateur est un staff de structure
     */
    private boolean isStructureStaff(User user) {
        boolean isStaff = user instanceof StaffUser && user.getStructure() != null;
        log.debug("Vérification si l'utilisateur {} est un staff: {} (type: {}, role: {}, structure: {})",
                user.getEmail(), 
                isStaff, 
                user.getClass().getSimpleName(), 
                user.getRole(), 
                user.getStructure() != null ? user.getStructure().getId() : "null");
        return isStaff;
    }


    public boolean canCreateInStructure(UserDetails principal, Long structureId) {
        if (!(principal instanceof StaffUser staffUser)) {
            return false;
        }

        if (staffUser.getStructure() == null) {
            return false; // L'utilisateur n'est associé à aucune structure
        }

        // Vérifie si l'utilisateur essaie de créer un événement dans sa propre structure
        return staffUser.getStructure().getId().equals(structureId);
    }
}
