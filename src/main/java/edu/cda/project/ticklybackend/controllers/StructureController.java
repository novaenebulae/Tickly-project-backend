package edu.cda.project.ticklybackend.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import edu.cda.project.ticklybackend.dao.StructureDao;
import edu.cda.project.ticklybackend.models.Structure;
import edu.cda.project.ticklybackend.security.roles.IsAdmin;
import edu.cda.project.ticklybackend.security.roles.IsUser;
import edu.cda.project.ticklybackend.views.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    @JsonView(Views.Structure.class)
    @IsUser
    public List<Structure> getStructures() {
        return structureDao.findAll();
    }

    @GetMapping("/structure/{id}")
    @JsonView(Views.Structure.class)
    @IsAdmin
    public Structure getStructure(@PathVariable int id) {
        return structureDao.findStructureById(id);
    }
}
