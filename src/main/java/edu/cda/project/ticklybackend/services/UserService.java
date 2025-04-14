package edu.cda.project.ticklybackend.services;

import edu.cda.project.ticklybackend.models.user.roles.staffUsers.OrganizationServiceUser;
import edu.cda.project.ticklybackend.models.user.roles.staffUsers.ReservationServiceUser;
import edu.cda.project.ticklybackend.models.user.roles.SpectatorUser;
import edu.cda.project.ticklybackend.models.user.roles.staffUsers.StructureAdministratorUser;
import edu.cda.project.ticklybackend.daos.userDao.UserDao;
import edu.cda.project.ticklybackend.models.user.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserDao userDao;

    @Autowired
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public List<User> findAllUsers() {
        return userDao.findAll();
    }

    public User findUserById(Integer id) {
        return userDao.findUserById(id);
    }

    public User updateUser(Integer id, User updatedUser) {
        User existingUser = userDao.findUserById(id);
        if (existingUser != null) {
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setFirstName(updatedUser.getFirstName());
            existingUser.setLastName(updatedUser.getLastName());
            return userDao.save(existingUser);
        }
        return null;
    }

    public User saveUser(User user) {
        return userDao.save(user);
    }

    public void deleteUser(Integer id) {
        userDao.deleteById(id);
    }

    public User updateUserLastConnection(Integer id) {
        User user = userDao.findUserById(id);
        if (user != null) {
            user.setLastConnectionDate(new Date().toInstant());
            return userDao.save(user);
        }
        return null;
    }

    public User changeUserRole(Integer userId, UserRole newRole) {
        User existingUser = userDao.findUserById(userId);
        if (existingUser == null) {
            return null;
        }

        User newUser;

        switch (newRole) {
            case SPECTATOR:
                newUser = new SpectatorUser();
                break;
            case RESERVATION_SERVICE:
                newUser = new ReservationServiceUser();
                break;
            case ORGANIZATION_SERVICE:
                newUser = new OrganizationServiceUser();
                break;
            case STRUCTURE_ADMINISTRATOR:
                newUser = new StructureAdministratorUser();
                break;
            default:
                throw new IllegalArgumentException("Invalid role: " + newRole);
        }

        // Copy user details from existing user
        newUser.setId(existingUser.getId());
        newUser.setEmail(existingUser.getEmail());
        newUser.setPassword(existingUser.getPassword()); // Keep the hashed password
        newUser.setFirstName(existingUser.getFirstName());
        newUser.setLastName(existingUser.getLastName());
        newUser.setRegistrationDate(existingUser.getRegistrationDate());
        newUser.setLastConnectionDate(new Date().toInstant()); // Update last connection

        // Save the new user with the updated role
        return userDao.save(newUser);
    }

    private void copyUserDetails(User source, User target) {
        target.setEmail(source.getEmail());
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
    }


    // Ajoutez cette méthode si elle n'existe pas déjà
    public User findUserByEmail(String email) {
        return userDao.findUserByEmail(email).orElse(null);
    }

    public boolean hasRole(User user, UserRole role) {
        if (user == null) {
            return false;
        }
        return user.getRole() == role;
    }

}
