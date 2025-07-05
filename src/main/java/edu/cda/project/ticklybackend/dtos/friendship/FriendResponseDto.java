package edu.cda.project.ticklybackend.dtos.friendship;

import edu.cda.project.ticklybackend.dtos.user.UserSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO représentant un ami accepté.")
public class FriendResponseDto {

    @Schema(description = "ID de la relation d'amitié.", example = "42")
    private Long friendshipId;

    @Schema(description = "Informations sur l'utilisateur qui est l'ami.")
    private UserSummaryDto friend;

    @Schema(description = "Date à laquelle l'amitié a été acceptée (devenue effective).", example = "2025-06-25T10:30:00Z")
    private ZonedDateTime since;
}