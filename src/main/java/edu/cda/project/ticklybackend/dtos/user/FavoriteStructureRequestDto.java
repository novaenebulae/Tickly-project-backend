package edu.cda.project.ticklybackend.dtos.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteStructureRequestDto {
    @NotNull(message = "L'ID de la structure ne peut pas être nul.")
    private Long structureId;
}