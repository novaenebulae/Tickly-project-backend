package edu.cda.project.ticklybackend.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import edu.cda.project.ticklybackend.dao.UserDao;
import edu.cda.project.ticklybackend.models.User;
import edu.cda.project.ticklybackend.views.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @JsonView(Views.User.class)
    public List<User> getUsers() {
        return userDao.findAll();
    }
}
