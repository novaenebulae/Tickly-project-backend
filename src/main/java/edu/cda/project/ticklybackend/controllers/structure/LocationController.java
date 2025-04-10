package edu.cda.project.ticklybackend.controllers.structure;

import edu.cda.project.ticklybackend.daos.structureDao.LocationDao;
import edu.cda.project.ticklybackend.models.structure.Location;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
public class LocationController {

    protected LocationDao locationDao;

    @Autowired
    public LocationController(LocationDao locationDao) {
        this.locationDao = locationDao;
    }

    @GetMapping("/locations")
    public List<Location> getAreas() {
        return locationDao.findAll();
    }

    @PostMapping("/location")
    public ResponseEntity<Location> save(@RequestBody @Valid Location location) {

        location.setId(null);
        locationDao.save(location);
        return new ResponseEntity<>(location, HttpStatus.CREATED);
    }

    @DeleteMapping("/location/{id}")
    public ResponseEntity<Location> delete(@PathVariable int id) {

        Optional<Location> optionalArea = locationDao.findById(id);

        if (optionalArea.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        locationDao.deleteById(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    @PutMapping("/location/{id}")
    public ResponseEntity<Location> update(
            @PathVariable int id,
            @RequestBody @Valid Location location) {

        Optional<Location> optionalArea = locationDao.findById(id);

        if (optionalArea.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        location.setId(id);

        locationDao.save(location);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
