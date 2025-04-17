package edu.cda.project.ticklybackend.controllers.structureControllers;

import edu.cda.project.ticklybackend.daos.structureDao.StructureTypeDao;
import edu.cda.project.ticklybackend.models.structure.StructureType;
import edu.cda.project.ticklybackend.services.StructureService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/api/structure-types")
public class StructureTypeController {

    protected StructureTypeDao structureTypeDao;
    protected StructureService structureService;

    @Autowired
    public StructureTypeController(StructureTypeDao structureTypeDao, StructureService structureService) {
        this.structureTypeDao = structureTypeDao;
        this.structureService = structureService;
    }

    @GetMapping
    public ResponseEntity<List<StructureType>> getAllStructureTypes() {
        List<StructureType> types = structureService.findAllStructureTypes();
        return new ResponseEntity<>(types, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<StructureType> save(@RequestBody @Valid StructureType structureType) {

        structureType.setId(null);
        structureTypeDao.save(structureType);
        return new ResponseEntity<>(structureType, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<StructureType> delete(@PathVariable int id) {

        Optional<StructureType> optionalArea = structureTypeDao.findById(id);

        if (optionalArea.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        structureTypeDao.deleteById(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    @PutMapping("/{id}")
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
