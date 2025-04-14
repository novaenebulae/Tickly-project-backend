package edu.cda.project.ticklybackend.services;

import edu.cda.project.ticklybackend.daos.structureDao.StructureDao;
import edu.cda.project.ticklybackend.daos.structureDao.StructureTypeDao;
import edu.cda.project.ticklybackend.daos.userDao.UserDao;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.structure.StructureType;
import edu.cda.project.ticklybackend.models.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StructureService {

    private final StructureDao structureDao;
    private final StructureTypeDao structureTypeDao;

    @Autowired
    public StructureService(StructureDao structureDao, StructureTypeDao structureTypeDao) {
        this.structureDao = structureDao;
        this.structureTypeDao = structureTypeDao;
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

    @Autowired
    private UserDao userDao; // Assurez-vous d'injecter ce repository

    public void deleteStructure(Integer id) {
        // 1. Récupérer tous les utilisateurs associés à cette structure
        List<User> usersWithStructure = userDao.findByStructureId(id);

        // 2. Détacher les utilisateurs de la structure (mettre structure_id à NULL)
        for (User user : usersWithStructure) {
            user.setStructure(null);
            userDao.save(user);
        }

        // 3. Maintenant que les utilisateurs sont détachés, supprimer la structure
        structureDao.deleteById(id);
    }

    public List<StructureType> findAllStructureTypes() {
        return structureTypeDao.findAll();
    }

    public List<Structure> findStructuresByTypeId(Integer typeId) {
        return structureDao.findByTypesId(typeId);
    }
}
