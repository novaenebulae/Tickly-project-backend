package edu.cda.project.ticklybackend.models.structure;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// @Data génère getters, setters, toString, equals, hashCode
@Data
// @NoArgsConstructor génère un constructeur sans arguments (requis par JPA)
@NoArgsConstructor
// @AllArgsConstructor génère un constructeur avec tous les arguments
@AllArgsConstructor
// @Entity marque cette classe comme une entité JPA
@Entity
// @Table spécifie le nom de la table dans la base de données
@Table(name = "structures")
public class Structure {

    // @Id marque ce champ comme la clé primaire
    @Id
    // @GeneratedValue spécifie que la valeur de la clé primaire est générée automatiquement
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Nom de la structure

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    // D'autres champs seront ajoutés à l'Étape 2
}