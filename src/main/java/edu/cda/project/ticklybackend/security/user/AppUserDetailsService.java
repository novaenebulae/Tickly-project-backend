package edu.cda.project.ticklybackend.security.user;

import edu.cda.project.ticklybackend.daos.userDao.UserDao;
import edu.cda.project.ticklybackend.models.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    protected UserDao userDao;

    @Autowired
    public AppUserDetailsService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User optionalUser = userDao.findUserByEmail(email);

        if (optionalUser == null) {
            throw new UsernameNotFoundException(email);
        }

        return new AppUserDetails(optionalUser);
    }
}
