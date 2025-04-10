package edu.cda.project.ticklybackend.controllers.userControllers;

import edu.cda.project.ticklybackend.daos.userDao.UserDao;
import edu.cda.project.ticklybackend.models.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
public class UserController {

    protected UserDao userDao;

    @Autowired
    public UserController(UserDao userDao) {
        this.userDao = userDao;
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        return userDao.findAll();
    }

    @GetMapping("/user/{id}")
    public User getUser (@PathVariable Integer id) {
        return userDao.findUserById(id);
    }

    @PostMapping("/user")
    public ResponseEntity<User> addUser (@RequestBody User user) {
        userDao.save(user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<User> deleteUser (@PathVariable Integer id) {
        userDao.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<User> updateUser (@PathVariable Integer id, @RequestBody User user) {
        userDao.save(user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
