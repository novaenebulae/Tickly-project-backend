package edu.cda.project.ticklybackend.models.user;

import edu.cda.project.ticklybackend.models.structure.Structure;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
public abstract class StaffUser extends User {

    // Constructeur pour les sous-classes
    public StaffUser(Structure structure) {
        super();
        this.setStructure(structure);
    }
}