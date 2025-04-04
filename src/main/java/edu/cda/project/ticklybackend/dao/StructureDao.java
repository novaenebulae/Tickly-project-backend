package edu.cda.project.ticklybackend.dao;

import edu.cda.project.ticklybackend.models.Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StructureDao extends JpaRepository<Structure, Integer> {

    public Structure findByName(String name);
}
