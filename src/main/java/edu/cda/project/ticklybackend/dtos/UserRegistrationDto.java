package edu.cda.project.ticklybackend.dtos;

import edu.cda.project.ticklybackend.models.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class UserRegistrationDto {
    @NotBlank @Email
    private String email;

    @NotBlank @Length(min = 8)
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private boolean createStructure = false;
}


