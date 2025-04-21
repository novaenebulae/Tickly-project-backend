package edu.cda.project.ticklybackend.services;

import edu.cda.project.ticklybackend.daos.structureDao.StructureDao;
import edu.cda.project.ticklybackend.daos.structureDao.StructureTypeDao;
import edu.cda.project.ticklybackend.daos.userDao.StaffUserDao;
import edu.cda.project.ticklybackend.daos.userDao.UserDao;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.structure.StructureType;
import edu.cda.project.ticklybackend.models.user.UserRole;
import edu.cda.project.ticklybackend.models.user.roles.staffUsers.StaffUser;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StructureService {

    private final StructureDao structureDao;
    private final StructureTypeDao structureTypeDao;
    private final StaffUserDao staffUserDao;
    private final UserDao userDao;

    private static final Logger logger = LoggerFactory.getLogger(StructureService.class);

    @Autowired
    public StructureService(StructureDao structureDao, StructureTypeDao structureTypeDao, StaffUserDao staffUserDao, UserDao userDao) {
        this.structureDao = structureDao;
        this.structureTypeDao = structureTypeDao;
        this.staffUserDao = staffUserDao;
        this.userDao = userDao;
    }

    public List<Structure> findAllStructures() {
        return structureDao.findAll();
    }

    public Structure findStructureById(Integer id) {
        return structureDao.findById(id).orElse(null);
    }

    public Structure saveStructure(@Valid Structure structure) {
        return structureDao.save(structure);
    }

    @Transactional
    public void deleteStructure(Integer id) {
        logger.info("Attempting to delete structure with ID: {}", id);

        List<StaffUser> staffUsers = staffUserDao.findByStructureId(id);
        logger.debug("Found {} staff users associated with structure {}", staffUsers.size(), id);

        for (StaffUser user : staffUsers) {
            logger.debug("Processing user ID: {} formerly associated with structure {}", user.getId(), id);

            // 1. Mettre à jour l'entité en mémoire et sauvegarder pour structure=null
            user.setStructure(null);
            staffUserDao.save(user); // Met à jour la FK structure_id dans la table staff_user (si JOINED) ou user (si SINGLE_TABLE avec champs staff)
            logger.debug("User ID {} relationship with structure {} removed.", user.getId(), id);

            // 2. Mettre à jour la colonne discriminante 'role' directement dans la table user
            int updatedRows = userDao.updateUserRoleColumn(user.getId(), UserRole.SPECTATOR);
            if (updatedRows > 0) {
                logger.info("Discriminator column 'role' updated to SPECTATOR for user ID: {}", user.getId());
            } else {
                // Cette situation ne devrait pas arriver si l'utilisateur existe, mais logger au cas où.
                logger.warn("Could not update discriminator column 'role' for user ID: {}. User might not exist?", user.getId());
            }
        }

        // 3. Supprimer la structure
        structureDao.deleteById(id);
        logger.info("Successfully deleted structure with ID: {}", id);
    }

    public List<StructureType> findAllStructureTypes() {
        return structureTypeDao.findAll();
    }

    public List<Structure> findStructuresByTypeId(Integer typeId) {
        return structureDao.findByTypesId(typeId);
    }


}
