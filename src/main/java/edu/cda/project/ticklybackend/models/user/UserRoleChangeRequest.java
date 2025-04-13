package edu.cda.project.ticklybackend.models.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRoleChangeRequest {
    private UserRole newRole;
}
