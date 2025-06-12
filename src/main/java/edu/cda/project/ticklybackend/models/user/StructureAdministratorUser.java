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
@DiscriminatorValue("STRUCTURE_ADMINISTRATOR")
public class StructureAdministratorUser extends StaffUser {

    public StructureAdministratorUser(String firstName, String lastName, String email, String password, Structure structure, Boolean needsStructureSetup) {
        super(firstName, lastName, email, password, UserRole.STRUCTURE_ADMINISTRATOR, structure, needsStructureSetup);
    }
}