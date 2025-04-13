package edu.cda.project.ticklybackend.models.user.roles;

import edu.cda.project.ticklybackend.models.user.User;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("STRUCTURE_ADMINISTRATOR")
public class StructureAdministratorUser extends User {
    // Specific attributes for structure administrators can be added here
}
