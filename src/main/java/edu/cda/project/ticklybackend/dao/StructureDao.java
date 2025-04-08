package edu.cda.project.ticklybackend.dao;

import edu.cda.project.ticklybackend.models.Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StructureDao extends JpaRepository<Structure, Integer> {

    Structure findStructureById(int id);
}
