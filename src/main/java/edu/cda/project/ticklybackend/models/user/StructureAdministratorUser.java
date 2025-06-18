package edu.cda.project.ticklybackend.models.user;

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

    // Constructeur avec structure - appelé explicitement
    public StructureAdministratorUser(Structure structure) {
        super(structure);
    }

    // Constructeur par défaut requis par JPA
    public StructureAdministratorUser() {
        super();
    }
}