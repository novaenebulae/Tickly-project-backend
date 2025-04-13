package edu.cda.project.ticklybackend.models.user.roles;

import edu.cda.project.ticklybackend.models.user.User;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ORGANIZATION_SERVICE")
public class OrganizationServiceUser extends User {
}

