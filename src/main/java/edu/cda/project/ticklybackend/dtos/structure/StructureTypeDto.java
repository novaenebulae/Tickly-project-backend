package edu.cda.project.ticklybackend.dtos.structure; // Adaptez le package si nécessaire

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StructureTypeDto {
    private Long id;
    private String name;
    private String icon; // Optionnel
}