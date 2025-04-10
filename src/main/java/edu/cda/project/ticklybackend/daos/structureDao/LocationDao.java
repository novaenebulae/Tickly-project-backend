package edu.cda.project.ticklybackend.daos.structureDao;

import edu.cda.project.ticklybackend.models.structure.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationDao extends JpaRepository<Location, Integer> {

}
