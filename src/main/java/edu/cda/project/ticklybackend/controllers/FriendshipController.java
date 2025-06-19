package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.friendship.FriendsDataResponseDto;
import edu.cda.project.ticklybackend.dtos.friendship.SendFriendRequestDto;
import edu.cda.project.ticklybackend.dtos.friendship.UpdateFriendshipStatusDto;
import edu.cda.project.ticklybackend.services.interfaces.FriendshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/friendship")
@RequiredArgsConstructor
@Tag(name = "Gestion des Amitiés", description = "API pour gérer les relations d'amitié entre utilisateurs.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
public class FriendshipController {

    private final FriendshipService friendshipService;

    @GetMapping
    @Operation(summary = "Récupérer toutes les données d'amitié de l'utilisateur")
    public ResponseEntity<FriendsDataResponseDto> getMyFriendsData() {
        return ResponseEntity.ok(friendshipService.getFriendsData());
    }

    @PostMapping("/requests")
    @Operation(summary = "Envoyer une demande d'ami")
    public ResponseEntity<Void> sendFriendRequest(@Valid @RequestBody SendFriendRequestDto requestDto) {
        friendshipService.sendFriendRequest(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/requests/{friendshipId}")
    @Operation(summary = "Mettre à jour le statut d'une demande d'ami (accepter, refuser, annuler)")
    public ResponseEntity<Void> updateFriendRequestStatus(
            @PathVariable Long friendshipId,
            @Valid @RequestBody UpdateFriendshipStatusDto updateDto) {
        friendshipService.updateFriendshipStatus(friendshipId, updateDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/friends/{friendUserId}")
    @Operation(summary = "Supprimer un ami")
    public ResponseEntity<Void> removeFriend(@PathVariable Long friendUserId) {
        friendshipService.removeFriend(friendUserId);
        return ResponseEntity.noContent().build();
    }
}