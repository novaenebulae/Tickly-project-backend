package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dao.StructureDao;
import edu.cda.project.ticklybackend.models.Structure;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
public class StructureController {

    protected StructureDao structureDao;

    @Autowired
    public StructureController(StructureDao structureDao) {
        this.structureDao = structureDao;
    }

    @GetMapping("/structures")
    public List<Structure> getStructures() {
        return structureDao.findAll();
    }

    @PostMapping("/structure")
    public ResponseEntity<Structure> save(@RequestBody @Valid Structure structure) {

        structure.setId(null);
        structureDao.save(structure);
        return new ResponseEntity<>(structure, HttpStatus.CREATED);
    }

    @DeleteMapping("/structure/{id}")
    public ResponseEntity<Structure> delete(@PathVariable int id) {

        Optional<Structure> optionalStructure = structureDao.findById(id);

        if (optionalStructure.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        structureDao.deleteById(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    @PutMapping("/structure/{id}")
    public ResponseEntity<Structure> update(
            @PathVariable int id,
            @RequestBody @Valid Structure structure) {

        Optional<Structure> optionalStructure = structureDao.findById(id);

        if (optionalStructure.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        structure.setId(id);

        structureDao.save(structure);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
