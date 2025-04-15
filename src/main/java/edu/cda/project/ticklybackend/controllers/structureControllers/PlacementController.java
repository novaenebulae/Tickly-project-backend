package edu.cda.project.ticklybackend.controllers.structureControllers;

import edu.cda.project.ticklybackend.daos.structureDao.PlacementDao;
import edu.cda.project.ticklybackend.models.structure.Placement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
public class PlacementController {

    protected PlacementDao placementDao;

    @Autowired
    public PlacementController(PlacementDao placementDao) {
        this.placementDao = placementDao;
    }

    @GetMapping("/placements")
    public List<Placement> getAreas() {
        return placementDao.findAll();
    }

    @PostMapping("/placement")
    public ResponseEntity<Placement> save(@RequestBody @Valid Placement placement) {

        placement.setId(null);
        placementDao.save(placement);
        return new ResponseEntity<>(placement, HttpStatus.CREATED);
    }

    @DeleteMapping("/placement/{id}")
    public ResponseEntity<Placement> delete(@PathVariable int id) {

        Optional<Placement> optionalArea = placementDao.findById(id);

        if (optionalArea.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        placementDao.deleteById(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    @PutMapping("/placement/{id}")
    public ResponseEntity<Placement> update(
            @PathVariable int id,
            @RequestBody @Valid Placement placement) {

        Optional<Placement> optionalArea = placementDao.findById(id);

        if (optionalArea.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        placement.setId(id);

        placementDao.save(placement);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
