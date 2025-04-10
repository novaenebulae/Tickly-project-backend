package edu.cda.project.ticklybackend.security.user;

import edu.cda.project.ticklybackend.dao.UserDao;
import edu.cda.project.ticklybackend.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AppUserDetailsService implements UserDetailsService {

    protected UserDao userDao;

    @Autowired
    public AppUserDetailsService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {

        Optional<User> optionalUser = userDao.findByMail(mail);

        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException(mail);
        }

        return new AppUserDetails(optionalUser.get());
    }
}
