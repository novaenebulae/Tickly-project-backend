package edu.cda.project.ticklybackend.controllers.structure;

import edu.cda.project.ticklybackend.DAO.structureDAO.AddressDao;
import edu.cda.project.ticklybackend.models.structure.Address;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    @PostMapping("/address")
    public ResponseEntity<Address> save(@RequestBody @Valid Address address) {

        address.setId(null);
        addressDao.save(address);
        return new ResponseEntity<>(address, HttpStatus.CREATED);
    }

    @DeleteMapping("/address/{id}")
    public ResponseEntity<Address> delete(@PathVariable int id) {

        Optional<Address> optionalAddress = addressDao.findById(id);

        if (optionalAddress.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        addressDao.deleteById(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    @PutMapping("/address/{id}")
    public ResponseEntity<Address> update(
            @PathVariable int id,
            @RequestBody @Valid Address address) {

        Optional<Address> optionalAddress = addressDao.findById(id);

        if (optionalAddress.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        address.setId(id);

        addressDao.save(address);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
