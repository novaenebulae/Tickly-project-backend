package edu.cda.project.ticklybackend.models.user;

import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.models.structure.Structure;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@DiscriminatorValue("RESERVATION_SERVICE")
public class ReservationServiceUser extends StaffUser {

    public ReservationServiceUser(String firstName, String lastName, String email, String password, Structure structure) {
        super(structure);
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setEmail(email);
        this.setPassword(password);
        this.setRole(UserRole.RESERVATION_SERVICE);
        this.setNeedsStructureSetup(false);
    }
}