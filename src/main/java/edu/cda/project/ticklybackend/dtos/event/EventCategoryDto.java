package edu.cda.project.ticklybackend.dtos.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCategoryDto {
    private Long id;
    private String name;
    // private String icon; // Si vous ajoutez une icône à l'entité
}