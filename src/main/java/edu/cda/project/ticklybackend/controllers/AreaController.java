package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dao.AreaDao;
import edu.cda.project.ticklybackend.models.Location;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
public class AreaController {

    protected AreaDao areaDao;

    @Autowired
    public AreaController(AreaDao areaDao) {
        this.areaDao = areaDao;
    }

    @GetMapping("/areas")
    public List<Location> getAreas() {
        return areaDao.findAll();
    }

    @PostMapping("/area")
    public ResponseEntity<Location> save(@RequestBody @Valid Location location) {

        location.setId(null);
        areaDao.save(location);
        return new ResponseEntity<>(location, HttpStatus.CREATED);
    }

    @DeleteMapping("/area/{id}")
    public ResponseEntity<Location> delete(@PathVariable int id) {

        Optional<Location> optionalArea = areaDao.findById(id);

        if (optionalArea.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        areaDao.deleteById(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    @PutMapping("/area/{id}")
    public ResponseEntity<Location> update(
            @PathVariable int id,
            @RequestBody @Valid Location location) {

        Optional<Location> optionalArea = areaDao.findById(id);

        if (optionalArea.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        location.setId(id);

        areaDao.save(location);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
