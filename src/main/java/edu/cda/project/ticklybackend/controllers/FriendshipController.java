package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.friendship.FriendsDataResponseDto;
import edu.cda.project.ticklybackend.dtos.friendship.SendFriendRequestDto;
import edu.cda.project.ticklybackend.dtos.friendship.UpdateFriendshipStatusDto;
import edu.cda.project.ticklybackend.exceptions.BadRequestException;
import edu.cda.project.ticklybackend.exceptions.InvalidTokenException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.services.interfaces.FriendshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class FriendshipController {

    private final FriendshipService friendshipService;

    @GetMapping
    @Operation(summary = "Récupérer toutes les données d'amitié de l'utilisateur")
    public ResponseEntity<FriendsDataResponseDto> getMyFriendsData() {
        log.info("Récupération des données d'amitié pour l'utilisateur courant");
        try {
            FriendsDataResponseDto friendsData = friendshipService.getFriendsData();
            log.info("Données d'amitié récupérées avec succès: {} amis, {} demandes reçues, {} demandes envoyées", 
                    friendsData.getFriends().size(), 
                    friendsData.getPendingRequests().size(), 
                    friendsData.getSentRequests().size());
            return ResponseEntity.ok(friendsData);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des données d'amitié: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/requests")
    @Operation(summary = "Envoyer une demande d'ami")
    public ResponseEntity<Void> sendFriendRequest(@Valid @RequestBody SendFriendRequestDto requestDto) {
        log.info("Envoi d'une demande d'ami à l'email: {}", requestDto.getEmail());
        try {
            friendshipService.sendFriendRequest(requestDto);
            log.info("Demande d'ami envoyée avec succès à: {}", requestDto.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (ResourceNotFoundException e) {
            log.error("Utilisateur non trouvé lors de l'envoi d'une demande d'ami à: {}: {}", requestDto.getEmail(), e.getMessage());
            throw e;
        } catch (BadRequestException e) {
            log.warn("Requête invalide lors de l'envoi d'une demande d'ami à: {}: {}", requestDto.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'envoi d'une demande d'ami à: {}: {}", requestDto.getEmail(), e.getMessage());
            throw e;
        }
    }

    @PutMapping("/requests/{friendshipId}")
    @Operation(summary = "Mettre à jour le statut d'une demande d'ami (accepter, refuser, annuler)")
    public ResponseEntity<Void> updateFriendRequestStatus(
            @PathVariable Long friendshipId,
            @Valid @RequestBody UpdateFriendshipStatusDto updateDto) {
        log.info("Mise à jour du statut de la demande d'ami ID: {} vers le statut: {}", friendshipId, updateDto.getStatus());
        try {
            friendshipService.updateFriendshipStatus(friendshipId, updateDto);
            log.info("Statut de la demande d'ami ID: {} mis à jour avec succès vers: {}", friendshipId, updateDto.getStatus());
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            log.error("Demande d'ami non trouvée - ID: {}: {}", friendshipId, e.getMessage());
            throw e;
        } catch (InvalidTokenException e) {
            log.warn("Autorisation invalide pour la mise à jour de la demande d'ami ID: {}: {}", friendshipId, e.getMessage());
            throw e;
        } catch (BadRequestException e) {
            log.warn("Requête invalide pour la mise à jour de la demande d'ami ID: {}: {}", friendshipId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la mise à jour de la demande d'ami ID: {}: {}", friendshipId, e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/friends/{friendUserId}")
    @Operation(summary = "Supprimer un ami")
    public ResponseEntity<Void> removeFriend(@PathVariable Long friendUserId) {
        log.info("Suppression de l'ami avec l'ID utilisateur: {}", friendUserId);
        try {
            friendshipService.removeFriend(friendUserId);
            log.info("Ami avec l'ID utilisateur: {} supprimé avec succès", friendUserId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            log.error("Relation d'amitié non trouvée avec l'utilisateur ID: {}: {}", friendUserId, e.getMessage());
            throw e;
        } catch (BadRequestException e) {
            log.warn("Requête invalide lors de la suppression de l'ami avec l'ID: {}: {}", friendUserId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la suppression de l'ami avec l'ID: {}: {}", friendUserId, e.getMessage());
            throw e;
        }
    }
}
