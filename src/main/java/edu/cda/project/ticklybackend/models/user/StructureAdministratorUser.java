package edu.cda.project.ticklybackend.models.user;

import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.models.structure.Structure;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("STRUCTURE_ADMINISTRATOR")
public class StructureAdministratorUser extends StaffUser {


    public StructureAdministratorUser(String firstName, String lastName, String email, String password, Structure structure) {
        super();
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setEmail(email);
        this.setPassword(password);
        this.setRole(UserRole.STRUCTURE_ADMINISTRATOR);
        this.setStructure(structure);
    }

    public StructureAdministratorUser() {
        super();
    }
}