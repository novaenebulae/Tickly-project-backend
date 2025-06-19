package edu.cda.project.ticklybackend.dtos.friendship;

import edu.cda.project.ticklybackend.dtos.user.UserSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Schema(description = "DTO représentant un ami confirmé.")
public class FriendResponseDto extends UserSummaryDto {
    @Schema(description = "ID de l'enregistrement de la relation d'amitié.")
    private Long friendshipId;

    @Schema(description = "Date à laquelle l'amitié a été confirmée.")
    private Instant since;

    public FriendResponseDto(Long id, String firstName, String lastName, String avatarUrl, Long friendshipId, Instant since) {
        super(id, firstName, lastName, avatarUrl);
        this.friendshipId = friendshipId;
        this.since = since;
    }
}