package edu.cda.project.ticklybackend.DAO.structureDAO;

import edu.cda.project.ticklybackend.models.structure.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressDao extends JpaRepository<Address, Integer> {

}
