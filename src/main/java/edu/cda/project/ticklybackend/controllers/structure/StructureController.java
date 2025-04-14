package edu.cda.project.ticklybackend.controllers.structure;

import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.structure.StructureType;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.models.user.roles.staffUsers.StructureAdministratorUser;
import edu.cda.project.ticklybackend.security.user.IsSpectator;
import edu.cda.project.ticklybackend.security.user.IsStructureAdministrator;
import edu.cda.project.ticklybackend.security.user.IsStructureOwner;
import edu.cda.project.ticklybackend.services.StructureService;
import edu.cda.project.ticklybackend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/structures")
public class StructureController {

    private final StructureService structureService;
    private final UserService userService;

    @Autowired
    public StructureController(StructureService structureService, UserService userService) {
        this.structureService = structureService;
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

    @PostMapping("/structure")
    @IsStructureAdministrator
    public ResponseEntity<?> createStructure(@RequestBody Structure structure) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        StructureAdministratorUser adminUser = (StructureAdministratorUser) userService.findUserByEmail(email);

        if (adminUser.getStructure() != null) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Vous êtes déjà administrateur d'une structure. Vous ne pouvez pas en créer une nouvelle.");
        }

        Structure savedStructure = structureService.saveStructure(structure);

        adminUser.setStructure(savedStructure);
        userService.saveUser(adminUser);

        return ResponseEntity.ok(savedStructure);
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
