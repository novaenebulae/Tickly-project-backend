package edu.cda.project.ticklybackend.dao;

import edu.cda.project.ticklybackend.models.Area;
import edu.cda.project.ticklybackend.models.StructureType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaDao extends JpaRepository<Area, Integer> {

}
