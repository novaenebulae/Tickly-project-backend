package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.daos.StructureDao;
import edu.cda.project.ticklybackend.models.structure.Structure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
