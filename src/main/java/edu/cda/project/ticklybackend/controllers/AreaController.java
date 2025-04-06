package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dao.AreaDao;
import edu.cda.project.ticklybackend.models.Area;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
