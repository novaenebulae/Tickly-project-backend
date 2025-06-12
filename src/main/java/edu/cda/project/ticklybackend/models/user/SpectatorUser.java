package edu.cda.project.ticklybackend.models.user;

import edu.cda.project.ticklybackend.enums.UserRole;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true) // Important pour inclure les champs de la classe parente dans equals/hashCode
@NoArgsConstructor
@Entity
// Valeur du discriminateur pour ce type d'utilisateur
@DiscriminatorValue("SPECTATOR")
public class SpectatorUser extends User {

    // Constructeur spécifique si nécessaire, ou pour initialiser des champs
    public SpectatorUser(String firstName, String lastName, String email, String password) {
        super(null, firstName, lastName, email, password, UserRole.SPECTATOR, false, null, null, null);
    }
}