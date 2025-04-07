package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dao.AreaDao;
import edu.cda.project.ticklybackend.models.Area;
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
    public List<Area> getAreas() {
        return areaDao.findAll();
    }

    @PostMapping("/area")
    public ResponseEntity<Area> save(@RequestBody @Valid Area area) {

        area.setId(null);
        areaDao.save(area);
        return new ResponseEntity<>(area, HttpStatus.CREATED);
    }

    @DeleteMapping("/area/{id}")
    public ResponseEntity<Area> delete(@PathVariable int id) {

        Optional<Area> optionalArea = areaDao.findById(id);

        if (optionalArea.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        areaDao.deleteById(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    @PutMapping("/area/{id}")
    public ResponseEntity<Area> update(
            @PathVariable int id,
            @RequestBody @Valid Area area) {

        Optional<Area> optionalArea = areaDao.findById(id);

        if (optionalArea.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        area.setId(id);

        areaDao.save(area);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
