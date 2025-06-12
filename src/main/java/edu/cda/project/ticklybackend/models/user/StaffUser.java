package edu.cda.project.ticklybackend.models.user;

import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.models.structure.Structure;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
// Valeur du discriminateur pour ce type d'utilisateur (peut être omis si StaffUser est abstrait
// et que seules ses sous-classes concrètes ont un DiscriminatorValue)
// Si StaffUser peut être instancié directement (ce qui n'est pas le cas ici car il est parent),
// il faudrait un @DiscriminatorValue. Pour l'instant, on le laisse comme classe de base pour d'autres.
public abstract class StaffUser extends User { // Classe abstraite

    // Relation ManyToOne avec l'entité Structure
    @ManyToOne(fetch = FetchType.LAZY) // LAZY pour ne pas charger la structure systématiquement
    @JoinColumn(name = "structure_id") // Nom de la colonne de clé étrangère dans la table users
    private Structure structure;

    // Constructeur pour les sous-classes
    public StaffUser(String firstName, String lastName, String email, String password, UserRole role, Structure structure, Boolean needsStructureSetup) {
        super(null, firstName, lastName, email, password, role, needsStructureSetup, null, null, null);
        this.structure = structure;
    }
}