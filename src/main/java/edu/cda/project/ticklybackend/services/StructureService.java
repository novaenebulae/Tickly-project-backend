package edu.cda.project.ticklybackend.services;

import edu.cda.project.ticklybackend.daos.structureDao.StructureDao;
import edu.cda.project.ticklybackend.daos.structureDao.StructureTypeDao;
import edu.cda.project.ticklybackend.daos.userDao.StaffUserDao;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.structure.StructureType;
import edu.cda.project.ticklybackend.models.user.roles.staffUsers.StaffUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StructureService {

    private final StructureDao structureDao;
    private final StructureTypeDao structureTypeDao;
    private final StaffUserDao staffUserDao;

    @Autowired
    public StructureService(StructureDao structureDao, StructureTypeDao structureTypeDao, StaffUserDao staffUserDao) {
        this.structureDao = structureDao;
        this.structureTypeDao = structureTypeDao;
        this.staffUserDao = staffUserDao;
    }

    public List<Structure> findAllStructures() {
        return structureDao.findAll();
    }

    public Structure findStructureById(Integer id) {
        return structureDao.findById(id).orElse(null);
    }

    public Structure saveStructure(Structure structure) {
        return structureDao.save(structure);
    }

    public void deleteStructure(Integer id) {
        List<StaffUser> StaffUsers = staffUserDao.findByStructureId(id);

        for (StaffUser user : StaffUsers) {
            user.setStructure(null);
            staffUserDao.save(user);
        }

        structureDao.deleteById(id);
    }

    public List<StructureType> findAllStructureTypes() {
        return structureTypeDao.findAll();
    }

    public List<Structure> findStructuresByTypeId(Integer typeId) {
        return structureDao.findByTypesId(typeId);
    }

}
