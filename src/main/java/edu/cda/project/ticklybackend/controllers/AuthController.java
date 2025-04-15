package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.daos.userDao.UserDao;
import edu.cda.project.ticklybackend.dtos.UserLoginDto;
import edu.cda.project.ticklybackend.dtos.UserRegistrationDto;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.models.user.UserRole;
import edu.cda.project.ticklybackend.models.user.roles.SpectatorUser;
import edu.cda.project.ticklybackend.models.user.roles.staffUsers.StructureAdministratorUser;
import edu.cda.project.ticklybackend.security.jwt.JwtUtils;
import edu.cda.project.ticklybackend.security.user.AppUserDetails;
import edu.cda.project.ticklybackend.services.UserService;
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

@CrossOrigin
@RestController
public class AuthController {


    private final UserService userService;
    protected UserDao userDao;
    protected PasswordEncoder passwordEncoder;
    protected AuthenticationProvider authenticationProvider;
    protected JwtUtils jwtUtils;

    @Autowired
    public AuthController(UserDao userDao, PasswordEncoder passwordEncoder, AuthenticationProvider authenticationProvider, JwtUtils jwtUtils, UserService userService) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.authenticationProvider = authenticationProvider;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody UserRegistrationDto userDto) {
        if (userDto.isCreateStructure()) {

            StructureAdministratorUser adminUser = new StructureAdministratorUser();
            adminUser.setEmail(userDto.getEmail());
            adminUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
            adminUser.setFirstName(userDto.getFirstName());
            adminUser.setLastName(userDto.getLastName());
            adminUser.setRole(UserRole.STRUCTURE_ADMINISTRATOR);
            adminUser.setStructure(null);

            return new ResponseEntity<>(userService.saveUser(adminUser), HttpStatus.CREATED);
        } else {
            SpectatorUser spectator = new SpectatorUser();
            spectator.setEmail(userDto.getEmail());
            spectator.setPassword(passwordEncoder.encode(userDto.getPassword()));
            spectator.setFirstName(userDto.getFirstName());
            spectator.setLastName(userDto.getLastName());

            return new ResponseEntity<>(userService.saveUser(spectator), HttpStatus.CREATED);
        }
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
