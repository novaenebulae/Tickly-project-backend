package edu.cda.project.ticklybackend.controllers.structureControllers;

import edu.cda.project.ticklybackend.daos.structureDao.StructureTypeDao;
import edu.cda.project.ticklybackend.dtos.StructureCreationDto;
import edu.cda.project.ticklybackend.models.structure.Address;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.structure.StructureType;
import edu.cda.project.ticklybackend.models.user.roles.staffUsers.StructureAdministratorUser;
import edu.cda.project.ticklybackend.security.user.annotations.IsStructureAdministrator;
import edu.cda.project.ticklybackend.security.user.annotations.IsStructureOwner;
import edu.cda.project.ticklybackend.services.AddressService;
import edu.cda.project.ticklybackend.services.StructureService;
import edu.cda.project.ticklybackend.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/api/structures")
public class StructureController {

    private final StructureService structureService;
    private final StructureTypeDao structureTypeDao;
    private final AddressService addressService;
    private final UserService userService;

    @Autowired
    public StructureController(StructureService structureService, StructureTypeDao structureTypeDao, AddressService addressService, UserService userService) {
        this.structureService = structureService;
        this.structureTypeDao = structureTypeDao;
        this.addressService = addressService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Structure>> getAllStructures() {
        List<Structure> structures = structureService.findAllStructures();
        return new ResponseEntity<>(structures, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Structure> getStructureById(@PathVariable Integer id) {
        Structure structure = structureService.findStructureById(id);
        if (structure != null) {
            return new ResponseEntity<>(structure, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(path = "/structure", consumes = MediaType.APPLICATION_JSON_VALUE)
    @IsStructureAdministrator
    public ResponseEntity<Structure> createStructure(@RequestBody @Valid StructureCreationDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        StructureAdministratorUser adminUser = (StructureAdministratorUser) userService.findUserByEmail(email);

        if (adminUser.getStructure() != null) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(null);
        }

        Address address = addressService.convertToAddress(dto.getAddress());

        Structure structure = new Structure();
        structure.setName(dto.getName());
        structure.setDescription(dto.getDescription());
        structure.setAddress(address);

        List<StructureType> types = dto.getTypeIds().stream()
                .map(id -> structureTypeDao.findStructureTypeById(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        structure.setTypes(types);

        Structure savedStructure = structureService.saveStructure(structure);

        adminUser.setStructure(savedStructure);
        userService.saveUser(adminUser);

        return new ResponseEntity<>(savedStructure, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @IsStructureAdministrator
    @IsStructureOwner
    public ResponseEntity<Structure> updateStructure(@PathVariable Integer id, @RequestBody Structure structure) {
        Structure existingStructure = structureService.findStructureById(id);
        if (existingStructure != null) {
            structure.setId(id);
            Structure updatedStructure = structureService.saveStructure(structure);
            return new ResponseEntity<>(updatedStructure, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    @IsStructureAdministrator
    @IsStructureOwner
    public ResponseEntity<?> deleteStructure(@PathVariable Integer id) {
        // La vérification des autorisations est maintenant gérée par l'aspect
        structureService.deleteStructure(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/types")
    public ResponseEntity<List<StructureType>> getAllStructureTypes() {
        List<StructureType> types = structureService.findAllStructureTypes();
        return new ResponseEntity<>(types, HttpStatus.OK);
    }

    @GetMapping("/by-type/{typeId}")
    public ResponseEntity<List<Structure>> getStructuresByType(@PathVariable Integer typeId) {
        List<Structure> structures = structureService.findStructuresByTypeId(typeId);
        return new ResponseEntity<>(structures, HttpStatus.OK);
    }
}
