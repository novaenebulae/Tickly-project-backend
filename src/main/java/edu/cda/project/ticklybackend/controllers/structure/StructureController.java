package edu.cda.project.ticklybackend.controllers.structure;

import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.structure.StructureType;
import edu.cda.project.ticklybackend.services.StructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/structures")
public class StructureController {

    private final StructureService structureService;

    @Autowired
    public StructureController(StructureService structureService) {
        this.structureService = structureService;
    }

    @GetMapping
    public ResponseEntity<List<Structure>> getAllStructures() {
        List<Structure> structures = structureService.findAllStructures();
        return new ResponseEntity<>(structures, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Structure> getStructureById(@PathVariable Integer id) {
        Structure structure = structureService.findStructureById(id);
        if (structure != null) {
            return new ResponseEntity<>(structure, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<Structure> createStructure(@RequestBody Structure structure) {
        Structure newStructure = structureService.saveStructure(structure);
        return new ResponseEntity<>(newStructure, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Structure> updateStructure(@PathVariable Integer id, @RequestBody Structure structure) {
        Structure existingStructure = structureService.findStructureById(id);
        if (existingStructure != null) {
            structure.setId(id);
            Structure updatedStructure = structureService.saveStructure(structure);
            return new ResponseEntity<>(updatedStructure, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStructure(@PathVariable Integer id) {
        Structure existingStructure = structureService.findStructureById(id);
        if (existingStructure != null) {
            structureService.deleteStructure(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/types")
    public ResponseEntity<List<StructureType>> getAllStructureTypes() {
        List<StructureType> types = structureService.findAllStructureTypes();
        return new ResponseEntity<>(types, HttpStatus.OK);
    }

    @GetMapping("/by-type/{typeId}")
    public ResponseEntity<List<Structure>> getStructuresByType(@PathVariable Integer typeId) {
        List<Structure> structures = structureService.findStructuresByTypeId(typeId);
        return new ResponseEntity<>(structures, HttpStatus.OK);
    }
}
