package edu.cda.project.ticklybackend.controllers.structureControllers;

import edu.cda.project.ticklybackend.daos.structureDao.StructureTypeDao;
import edu.cda.project.ticklybackend.models.structure.StructureType;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
public class StructureTypeController {

    protected StructureTypeDao structureTypeDao;

    @Autowired
    public StructureTypeController(StructureTypeDao structureTypeDao) {
        this.structureTypeDao = structureTypeDao;
    }

    @GetMapping("/structure-type")
    public List<StructureType> getStructureTypes() {
        return structureTypeDao.findAll();
    }

    @PostMapping("/structure-type")
    public ResponseEntity<StructureType> save(@RequestBody @Valid StructureType structureType) {

        structureType.setId(null);
        structureTypeDao.save(structureType);
        return new ResponseEntity<>(structureType, HttpStatus.CREATED);
    }

    @DeleteMapping("/structure-type/{id}")
    public ResponseEntity<StructureType> delete(@PathVariable int id) {

        Optional<StructureType> optionalArea = structureTypeDao.findById(id);

        if (optionalArea.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        structureTypeDao.deleteById(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    @PutMapping("/structure-type/{id}")
    public ResponseEntity<StructureType> update(
            @PathVariable int id,
            @RequestBody @Valid StructureType structureType) {

        Optional<StructureType> optionalArea = structureTypeDao.findById(id);

        if (optionalArea.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        structureType.setId(id);

        structureTypeDao.save(structureType);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
