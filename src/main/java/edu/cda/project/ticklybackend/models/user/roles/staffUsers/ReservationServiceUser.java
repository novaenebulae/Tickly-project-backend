package edu.cda.project.ticklybackend.models.user.roles.staffUsers;

import com.fasterxml.jackson.annotation.JsonBackReference;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.user.User;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@DiscriminatorValue("RESERVATION_SERVICE")
public class ReservationServiceUser extends StaffUser {
}

