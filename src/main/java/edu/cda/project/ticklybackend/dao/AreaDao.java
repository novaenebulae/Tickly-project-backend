package edu.cda.project.ticklybackend.dao;

import edu.cda.project.ticklybackend.models.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaDao extends JpaRepository<Location, Integer> {

}
