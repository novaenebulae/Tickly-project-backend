package security;

import edu.cda.project.ticklybackend.dao.UserDao;
import edu.cda.project.ticklybackend.models.User;
import edu.cda.project.ticklybackend.security.AppUserDetails;
import edu.cda.project.ticklybackend.security.JwtUtils;
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
    public ResponseEntity<User> signUp(@RequestBody @Valid User user) {
        user.setAdmin(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userDao.save(user);

        user.setPassword(null);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid User user) {

        try {
            AppUserDetails userDetails = (AppUserDetails) authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(
                            user.getMail(),
                            user.getPassword()))
                    .getPrincipal();

            return new ResponseEntity<>(jwtUtils.generateJwtToken(userDetails), HttpStatus.OK);

        } catch (AuthenticationException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
