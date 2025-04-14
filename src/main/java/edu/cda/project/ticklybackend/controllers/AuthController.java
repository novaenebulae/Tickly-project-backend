package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.daos.userDao.UserDao;
import edu.cda.project.ticklybackend.dtos.UserLoginDto;
import edu.cda.project.ticklybackend.dtos.UserRegistrationDto;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.models.user.roles.OrganizationServiceUser;
import edu.cda.project.ticklybackend.models.user.roles.ReservationServiceUser;
import edu.cda.project.ticklybackend.models.user.roles.SpectatorUser;
import edu.cda.project.ticklybackend.models.user.roles.StructureAdministratorUser;
import edu.cda.project.ticklybackend.security.user.AppUserDetails;
import edu.cda.project.ticklybackend.security.jwt.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@CrossOrigin
@RestController
public class AuthController {


    protected UserDao userDao;
    protected PasswordEncoder passwordEncoder;
    protected AuthenticationProvider authenticationProvider;
    protected JwtUtils jwtUtils;

    @Autowired
    public AuthController(UserDao userDao, PasswordEncoder passwordEncoder, AuthenticationProvider authenticationProvider, JwtUtils jwtUtils) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.authenticationProvider = authenticationProvider;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<User> signUp(@RequestBody @Valid UserRegistrationDto userDTO) {

        User user = new SpectatorUser();

        // Copier les propriétés
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setRegistrationDate(new Date().toInstant());
        user.setLastConnectionDate(new Date().toInstant());

        userDao.save(user);
        user.setPassword(null); // Pour ne pas renvoyer le mot de passe

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserLoginDto loginDto) {
        try {
            AppUserDetails userDetails = (AppUserDetails) authenticationProvider.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    loginDto.getEmail(),
                                    loginDto.getPassword()))
                    .getPrincipal();
            return new ResponseEntity<>(jwtUtils.generateJwtToken(userDetails), HttpStatus.OK);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

}
