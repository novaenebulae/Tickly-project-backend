package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dao.StructureTypeDao;
import edu.cda.project.ticklybackend.models.StructureType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
public class StructureTypeController {

    protected StructureTypeDao structureTypeDao;

    @Autowired
    public StructureTypeController(StructureTypeDao structureTypeDao) {
        this.structureTypeDao = structureTypeDao;
    }

    @GetMapping("/structure-types")
    public List<StructureType> getStructureTypes() {
        return structureTypeDao.findAll();
    }
}
