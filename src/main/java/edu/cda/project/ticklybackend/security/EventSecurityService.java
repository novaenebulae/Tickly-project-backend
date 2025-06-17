package edu.cda.project.ticklybackend.security;

import edu.cda.project.ticklybackend.models.event.Event;
import edu.cda.project.ticklybackend.models.user.StaffUser;
import edu.cda.project.ticklybackend.repositories.event.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("eventSecurityService")
@RequiredArgsConstructor
public class EventSecurityService {

    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public boolean isOwner(Long eventId, UserDetails principal) {
        if (!(principal instanceof StaffUser staffUser)) {
            return false; // Seuls les membres du staff peuvent être propriétaires
        }

        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null
                || event.getStructure() == null
                || staffUser.getStructure() == null) {
            return false;
        }

        // Vérifie si l'ID de la structure de l'événement correspond à l'ID de la structure de l'utilisateur
        return event.getStructure().getId().equals(staffUser.getStructure().getId());
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