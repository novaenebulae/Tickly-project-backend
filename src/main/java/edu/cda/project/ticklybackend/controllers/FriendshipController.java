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

/**
 * Authenticated endpoints for managing friendships: list, send requests,
 * update request status, and remove friends.
 */
@RestController
@RequestMapping("/api/v1/friendship")
@RequiredArgsConstructor
@Tag(name = "Friendship Management", description = "API for managing friendship relationships between users.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
@Slf4j
public class FriendshipController {

    private final FriendshipService friendshipService;

    @GetMapping
    @Operation(summary = "Retrieve all friendship data for the user")
    public ResponseEntity<FriendsDataResponseDto> getMyFriendsData() {
        log.info("Retrieving friendship data for current user");
        try {
            FriendsDataResponseDto friendsData = friendshipService.getFriendsData();
            log.info("Friendship data successfully retrieved: {} friends, {} received requests, {} sent requests", 
                    friendsData.getFriends().size(), 
                    friendsData.getPendingRequests().size(), 
                    friendsData.getSentRequests().size());
            return ResponseEntity.ok(friendsData);
        } catch (Exception e) {
            log.error("Error retrieving friendship data: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/requests")
    @Operation(summary = "Send a friend request")
    public ResponseEntity<Void> sendFriendRequest(@Valid @RequestBody SendFriendRequestDto requestDto) {
        log.info("Sending friend request to email: {}", requestDto.getEmail());
        try {
            friendshipService.sendFriendRequest(requestDto);
            log.info("Friend request successfully sent to: {}", requestDto.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (ResourceNotFoundException e) {
            log.error("User not found when sending friend request to: {}: {}", requestDto.getEmail(), e.getMessage());
            throw e;
        } catch (BadRequestException e) {
            log.warn("Invalid request when sending friend request to: {}: {}", requestDto.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when sending friend request to: {}: {}", requestDto.getEmail(), e.getMessage());
            throw e;
        }
    }

    @PutMapping("/requests/{friendshipId}")
    @Operation(summary = "Update the status of a friend request (accept, reject, cancel)")
    public ResponseEntity<Void> updateFriendRequestStatus(
            @PathVariable Long friendshipId,
            @Valid @RequestBody UpdateFriendshipStatusDto updateDto) {
        log.info("Updating friend request status ID: {} to status: {}", friendshipId, updateDto.getStatus());
        try {
            friendshipService.updateFriendshipStatus(friendshipId, updateDto);
            log.info("Friend request status ID: {} successfully updated to: {}", friendshipId, updateDto.getStatus());
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            log.error("Friend request not found - ID: {}: {}", friendshipId, e.getMessage());
            throw e;
        } catch (InvalidTokenException e) {
            log.warn("Invalid authorization for updating friend request ID: {}: {}", friendshipId, e.getMessage());
            throw e;
        } catch (BadRequestException e) {
            log.warn("Invalid request for updating friend request ID: {}: {}", friendshipId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when updating friend request ID: {}: {}", friendshipId, e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/friends/{friendUserId}")
    @Operation(summary = "Remove a friend")
    public ResponseEntity<Void> removeFriend(@PathVariable Long friendUserId) {
        log.info("Removing friend with user ID: {}", friendUserId);
        try {
            friendshipService.removeFriend(friendUserId);
            log.info("Friend with user ID: {} successfully removed", friendUserId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            log.error("Friendship relationship not found with user ID: {}: {}", friendUserId, e.getMessage());
            throw e;
        } catch (BadRequestException e) {
            log.warn("Invalid request when removing friend with ID: {}: {}", friendUserId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when removing friend with ID: {}: {}", friendUserId, e.getMessage());
            throw e;
        }
    }
}
