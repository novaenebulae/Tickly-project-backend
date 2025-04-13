package edu.cda.project.ticklybackend.models.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import edu.cda.project.ticklybackend.models.structure.Structure;

import edu.cda.project.ticklybackend.models.user.roles.OrganizationServiceUser;
import edu.cda.project.ticklybackend.models.user.roles.ReservationServiceUser;
import edu.cda.project.ticklybackend.models.user.roles.SpectatorUser;
import edu.cda.project.ticklybackend.models.user.roles.StructureAdministratorUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Date;

@Entity
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
@Table(name = "user")
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotBlank
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "last_name", nullable = false)
    protected String lastName;

    @Column(name = "first_name", nullable = false)
    protected String firstName;

    @Column(name = "last_connection", nullable = false)
    protected Date lastConnection;

    @Column(name = "registration_date", nullable = false)
    protected Date registrationDate;

    @Column(name = "role", nullable = false, insertable = false, updatable = false)
    protected String role;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "structure_id")
    protected Structure structure;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
