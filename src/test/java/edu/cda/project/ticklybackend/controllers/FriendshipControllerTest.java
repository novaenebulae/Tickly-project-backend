package edu.cda.project.ticklybackend.controllers;

import edu.cda.project.ticklybackend.dtos.friendship.*;
import edu.cda.project.ticklybackend.dtos.user.UserSummaryDto;
import edu.cda.project.ticklybackend.enums.FriendshipStatus;
import edu.cda.project.ticklybackend.exceptions.BadRequestException;
import edu.cda.project.ticklybackend.exceptions.InvalidTokenException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.services.interfaces.FriendshipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FriendshipControllerTest {

    @Mock
    private FriendshipService friendshipService;

    @InjectMocks
    private FriendshipController friendshipController;

    private Long friendshipId;
    private Long userId;
    private FriendsDataResponseDto friendsDataResponseDto;
    private SendFriendRequestDto sendFriendRequestDto;
    private UpdateFriendshipStatusDto updateFriendshipStatusDto;
    private List<FriendResponseDto> friends;
    private List<ReceivedFriendRequestResponseDto> pendingRequests;
    private List<SentFriendRequestResponseDto> sentRequests;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up common test data
        friendshipId = 1L;
        userId = 1L;

        // Create user summary DTO
        UserSummaryDto userSummaryDto = new UserSummaryDto();
        userSummaryDto.setId(userId);
        userSummaryDto.setFirstName("Test");
        userSummaryDto.setLastName("User");
        userSummaryDto.setAvatarUrl("http://example.com/avatar.jpg");

        // Create friend response DTO
        FriendResponseDto friendResponseDto = new FriendResponseDto();
        friendResponseDto.setFriendshipId(friendshipId);
        friendResponseDto.setFriend(userSummaryDto);
        friendResponseDto.setSince(ZonedDateTime.now());

        // Create received friend request response DTO
        ReceivedFriendRequestResponseDto receivedFriendRequestResponseDto = new ReceivedFriendRequestResponseDto();
        receivedFriendRequestResponseDto.setFriendshipId(friendshipId);
        receivedFriendRequestResponseDto.setSender(userSummaryDto);
        receivedFriendRequestResponseDto.setRequestedAt(ZonedDateTime.now());

        // Create sent friend request response DTO
        SentFriendRequestResponseDto sentFriendRequestResponseDto = new SentFriendRequestResponseDto();
        sentFriendRequestResponseDto.setFriendshipId(friendshipId);
        sentFriendRequestResponseDto.setReceiver(userSummaryDto);
        sentFriendRequestResponseDto.setSentAt(ZonedDateTime.now());

        // Create lists
        friends = new ArrayList<>();
        friends.add(friendResponseDto);

        pendingRequests = new ArrayList<>();
        pendingRequests.add(receivedFriendRequestResponseDto);

        sentRequests = new ArrayList<>();
        sentRequests.add(sentFriendRequestResponseDto);

        // Create friends data response DTO
        friendsDataResponseDto = new FriendsDataResponseDto();
        friendsDataResponseDto.setFriends(friends);
        friendsDataResponseDto.setPendingRequests(pendingRequests);
        friendsDataResponseDto.setSentRequests(sentRequests);

        // Create send friend request DTO
        sendFriendRequestDto = new SendFriendRequestDto();
        sendFriendRequestDto.setEmail("friend@example.com");

        // Create update friendship status DTO
        updateFriendshipStatusDto = new UpdateFriendshipStatusDto();
        updateFriendshipStatusDto.setStatus(FriendshipStatus.ACCEPTED);
    }

    @Test
    void getMyFriendsData_ShouldReturnFriendsData() {
        // Arrange
        when(friendshipService.getFriendsData()).thenReturn(friendsDataResponseDto);

        // Act
        ResponseEntity<FriendsDataResponseDto> response = friendshipController.getMyFriendsData();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getFriends().size());
        assertEquals(1, response.getBody().getPendingRequests().size());
        assertEquals(1, response.getBody().getSentRequests().size());
        assertEquals(friendshipId, response.getBody().getFriends().get(0).getFriendshipId());

        // Verify
        verify(friendshipService, times(1)).getFriendsData();
    }

    @Test
    void sendFriendRequest_ShouldReturnCreated() {
        // Arrange
        doNothing().when(friendshipService).sendFriendRequest(any(SendFriendRequestDto.class));

        // Act
        ResponseEntity<Void> response = friendshipController.sendFriendRequest(sendFriendRequestDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        // Verify
        verify(friendshipService, times(1)).sendFriendRequest(sendFriendRequestDto);
    }

    @Test
    void updateFriendRequestStatus_ShouldReturnOk() {
        // Arrange
        doNothing().when(friendshipService).updateFriendshipStatus(eq(friendshipId), any(UpdateFriendshipStatusDto.class));

        // Act
        ResponseEntity<Void> response = friendshipController.updateFriendRequestStatus(friendshipId, updateFriendshipStatusDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify
        verify(friendshipService, times(1)).updateFriendshipStatus(friendshipId, updateFriendshipStatusDto);
    }

    @Test
    void removeFriend_ShouldReturnNoContent() {
        // Arrange
        doNothing().when(friendshipService).removeFriend(userId);

        // Act
        ResponseEntity<Void> response = friendshipController.removeFriend(userId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify
        verify(friendshipService, times(1)).removeFriend(userId);
    }
}