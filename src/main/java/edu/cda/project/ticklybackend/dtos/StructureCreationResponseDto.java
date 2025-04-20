package edu.cda.project.ticklybackend.dtos; // Adaptez le package

import edu.cda.project.ticklybackend.models.structure.Structure;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StructureCreationResponseDto {

    private String newToken; // Le nouveau JWT sans le claim needsStructureSetup
    private Structure createdStructure;
}
