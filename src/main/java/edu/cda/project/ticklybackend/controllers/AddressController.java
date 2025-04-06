package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dao.AddressDao;
import edu.cda.project.ticklybackend.models.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
public class AddressController {

    protected AddressDao addressDao;

    @Autowired
    public AddressController(AddressDao addressDao) {
        this.addressDao = addressDao;
    }

    @GetMapping("/addresses")
    public List<Address> getAddresses() {
        return addressDao.findAll();
    }
}
