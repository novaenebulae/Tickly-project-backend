package edu.cda.project.ticklybackend.services.interfaces;

import edu.cda.project.ticklybackend.dtos.auth.AuthResponseDto;
import edu.cda.project.ticklybackend.dtos.auth.UserLoginDto;
import edu.cda.project.ticklybackend.dtos.auth.UserRegistrationDto;


public interface AuthService {
    AuthResponseDto registerAndLogin(UserRegistrationDto registrationDto);

    AuthResponseDto login(UserLoginDto loginDto);
}