package edu.cda.project.ticklybackend.models.user.roles.staffUsers;

import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Getter
@Setter
public abstract class StaffUser extends User {
    @ManyToOne
    @JoinColumn(name = "structure_id")
    private Structure structure;
}
