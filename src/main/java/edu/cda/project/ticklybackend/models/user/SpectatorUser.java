package edu.cda.project.ticklybackend.models.user;

import edu.cda.project.ticklybackend.enums.UserRole;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("SPECTATOR")
public class SpectatorUser extends User {

    // Constructeur pour création avec paramètres
    public SpectatorUser(String firstName, String lastName, String email, String password) {
        super();
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setEmail(email);
        this.setPassword(password);
        this.setRole(UserRole.SPECTATOR);
    }

    // Constructeur par défaut requis par JPA
    public SpectatorUser() {
        super();
    }
}