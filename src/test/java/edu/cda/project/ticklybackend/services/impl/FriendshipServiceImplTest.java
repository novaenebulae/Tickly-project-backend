package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.friendship.FriendsDataResponseDto;
import edu.cda.project.ticklybackend.dtos.friendship.SendFriendRequestDto;
import edu.cda.project.ticklybackend.dtos.friendship.UpdateFriendshipStatusDto;
import edu.cda.project.ticklybackend.enums.FriendshipStatus;
import edu.cda.project.ticklybackend.exceptions.BadRequestException;
import edu.cda.project.ticklybackend.exceptions.InvalidTokenException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.mappers.user.FriendshipMapper;
import edu.cda.project.ticklybackend.models.user.Friendship;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.models.user.SpectatorUser;
import edu.cda.project.ticklybackend.repositories.user.FriendshipRepository;
import edu.cda.project.ticklybackend.repositories.user.UserRepository;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import edu.cda.project.ticklybackend.utils.AuthUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FriendshipServiceImplTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendshipMapper friendshipMapper;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private AuthUtils authUtils;

    @InjectMocks
    private FriendshipServiceImpl friendshipService;

    private Long currentUserId;
    private Long friendUserId;
    private Long friendshipId;
    private User currentUser;
    private User friendUser;
    private Friendship friendship;
    private List<Friendship> acceptedFriendships;
    private List<Friendship> pendingRequests;
    private List<Friendship> sentRequests;
    private SendFriendRequestDto sendFriendRequestDto;
    private UpdateFriendshipStatusDto updateFriendshipStatusDto;
    private FriendsDataResponseDto friendsDataResponseDto;

    @BeforeEach
    void setUp() {
        // Set up common test data
        currentUserId = 1L;
        friendUserId = 2L;
        friendshipId = 1L;

        // Create users
        currentUser = new SpectatorUser();
        currentUser.setId(currentUserId);
        currentUser.setEmail("current@example.com");
        currentUser.setFirstName("Current");
        currentUser.setLastName("User");

        friendUser = new SpectatorUser();
        friendUser.setId(friendUserId);
        friendUser.setEmail("friend@example.com");
        friendUser.setFirstName("Friend");
        friendUser.setLastName("User");

        // Create friendship
        friendship = new Friendship();
        friendship.setId(friendshipId);
        friendship.setSender(currentUser);
        friendship.setReceiver(friendUser);
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setCreatedAt(Instant.now());
        friendship.setUpdatedAt(Instant.now());

        // Create lists
        acceptedFriendships = new ArrayList<>();
        acceptedFriendships.add(friendship);

        pendingRequests = new ArrayList<>();
        sentRequests = new ArrayList<>();

        // Create DTOs
        sendFriendRequestDto = new SendFriendRequestDto();
        sendFriendRequestDto.setEmail("friend@example.com");

        updateFriendshipStatusDto = new UpdateFriendshipStatusDto();
        updateFriendshipStatusDto.setStatus(FriendshipStatus.ACCEPTED);

        friendsDataResponseDto = new FriendsDataResponseDto();
    }

    @Test
    void getFriendsData_ShouldReturnFriendsData() {
        // Arrange
        when(authUtils.getCurrentAuthenticatedUserId()).thenReturn(currentUserId);
        when(friendshipRepository.findAcceptedFriends(currentUserId)).thenReturn(acceptedFriendships);
        when(friendshipRepository.findByReceiverIdAndStatus(currentUserId, FriendshipStatus.PENDING)).thenReturn(pendingRequests);
        when(friendshipRepository.findBySenderIdAndStatus(currentUserId, FriendshipStatus.PENDING)).thenReturn(sentRequests);
        when(friendshipMapper.toFriendResponseDtoList(eq(acceptedFriendships), eq(currentUserId), any(FileStorageService.class))).thenReturn(new ArrayList<>());
        when(friendshipMapper.toReceivedFriendRequestDtoList(eq(pendingRequests), any(FileStorageService.class))).thenReturn(new ArrayList<>());
        when(friendshipMapper.toSentFriendRequestDtoList(eq(sentRequests), any(FileStorageService.class))).thenReturn(new ArrayList<>());

        // Act
        FriendsDataResponseDto result = friendshipService.getFriendsData();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getFriends());
        assertNotNull(result.getPendingRequests());
        assertNotNull(result.getSentRequests());

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUserId();
        verify(friendshipRepository, times(1)).findAcceptedFriends(currentUserId);
        verify(friendshipRepository, times(1)).findByReceiverIdAndStatus(currentUserId, FriendshipStatus.PENDING);
        verify(friendshipRepository, times(1)).findBySenderIdAndStatus(currentUserId, FriendshipStatus.PENDING);
        verify(friendshipMapper, times(1)).toFriendResponseDtoList(eq(acceptedFriendships), eq(currentUserId), any(FileStorageService.class));
        verify(friendshipMapper, times(1)).toReceivedFriendRequestDtoList(eq(pendingRequests), any(FileStorageService.class));
        verify(friendshipMapper, times(1)).toSentFriendRequestDtoList(eq(sentRequests), any(FileStorageService.class));
    }

    @Test
    void sendFriendRequest_ShouldCreateFriendship() {
        // Arrange
        when(authUtils.getCurrentAuthenticatedUserId()).thenReturn(currentUserId);
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByEmail("friend@example.com")).thenReturn(Optional.of(friendUser));
        when(friendshipRepository.findFriendshipBetweenUsers(currentUserId, friendUserId)).thenReturn(Optional.empty());
        when(friendshipRepository.save(any(Friendship.class))).thenReturn(friendship);

        // Act
        friendshipService.sendFriendRequest(sendFriendRequestDto);

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUserId();
        verify(userRepository, times(1)).findById(currentUserId);
        verify(userRepository, times(1)).findByEmail("friend@example.com");
        verify(friendshipRepository, times(1)).findFriendshipBetweenUsers(currentUserId, friendUserId);
        verify(friendshipRepository, times(1)).save(any(Friendship.class));
    }

    @Test
    void sendFriendRequest_WithSameUser_ShouldThrowBadRequestException() {
        // Arrange
        sendFriendRequestDto.setEmail("current@example.com");
        when(authUtils.getCurrentAuthenticatedUserId()).thenReturn(currentUserId);
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            friendshipService.sendFriendRequest(sendFriendRequestDto);
        });

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUserId();
        verify(userRepository, times(1)).findById(currentUserId);
        verify(friendshipRepository, never()).save(any(Friendship.class));
    }

    @Test
    void sendFriendRequest_WithExistingFriendship_ShouldThrowBadRequestException() {
        // Arrange
        when(authUtils.getCurrentAuthenticatedUserId()).thenReturn(currentUserId);
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByEmail("friend@example.com")).thenReturn(Optional.of(friendUser));
        when(friendshipRepository.findFriendshipBetweenUsers(currentUserId, friendUserId)).thenReturn(Optional.of(friendship));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            friendshipService.sendFriendRequest(sendFriendRequestDto);
        });

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUserId();
        verify(userRepository, times(1)).findById(currentUserId);
        verify(userRepository, times(1)).findByEmail("friend@example.com");
        verify(friendshipRepository, times(1)).findFriendshipBetweenUsers(currentUserId, friendUserId);
        verify(friendshipRepository, never()).save(any(Friendship.class));
    }

    @Test
    void updateFriendshipStatus_AsReceiver_ShouldUpdateStatus() {
        // Arrange
        friendship.setStatus(FriendshipStatus.PENDING);
        updateFriendshipStatusDto.setStatus(FriendshipStatus.ACCEPTED);

        when(authUtils.getCurrentAuthenticatedUserId()).thenReturn(friendUserId);
        when(friendshipRepository.findById(friendshipId)).thenReturn(Optional.of(friendship));

        // Act
        friendshipService.updateFriendshipStatus(friendshipId, updateFriendshipStatusDto);

        // Assert
        assertEquals(FriendshipStatus.ACCEPTED, friendship.getStatus());

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUserId();
        verify(friendshipRepository, times(1)).findById(friendshipId);
        verify(friendshipRepository, times(1)).save(friendship);
    }

    @Test
    void updateFriendshipStatus_AsSender_WithCancelledStatus_ShouldDeleteFriendship() {
        // Arrange
        friendship.setStatus(FriendshipStatus.PENDING);
        updateFriendshipStatusDto.setStatus(FriendshipStatus.CANCELLED_BY_SENDER);

        when(authUtils.getCurrentAuthenticatedUserId()).thenReturn(currentUserId);
        when(friendshipRepository.findById(friendshipId)).thenReturn(Optional.of(friendship));

        // Act
        friendshipService.updateFriendshipStatus(friendshipId, updateFriendshipStatusDto);

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUserId();
        verify(friendshipRepository, times(1)).findById(friendshipId);
        verify(friendshipRepository, times(1)).delete(friendship);
        verify(friendshipRepository, never()).save(any(Friendship.class));
    }

    @Test
    void updateFriendshipStatus_AsReceiver_WithInvalidPermission_ShouldThrowInvalidTokenException() {
        // Arrange
        friendship.setStatus(FriendshipStatus.PENDING);
        updateFriendshipStatusDto.setStatus(FriendshipStatus.CANCELLED_BY_SENDER);

        when(authUtils.getCurrentAuthenticatedUserId()).thenReturn(friendUserId);
        when(friendshipRepository.findById(friendshipId)).thenReturn(Optional.of(friendship));

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> {
            friendshipService.updateFriendshipStatus(friendshipId, updateFriendshipStatusDto);
        });

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUserId();
        verify(friendshipRepository, times(1)).findById(friendshipId);
        verify(friendshipRepository, never()).save(any(Friendship.class));
        verify(friendshipRepository, never()).delete(any(Friendship.class));
    }

    @Test
    void removeFriend_WithAcceptedFriendship_ShouldDeleteFriendship() {
        // Arrange
        when(authUtils.getCurrentAuthenticatedUserId()).thenReturn(currentUserId);
        when(friendshipRepository.findFriendshipBetweenUsers(currentUserId, friendUserId)).thenReturn(Optional.of(friendship));

        // Act
        friendshipService.removeFriend(friendUserId);

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUserId();
        verify(friendshipRepository, times(1)).findFriendshipBetweenUsers(currentUserId, friendUserId);
        verify(friendshipRepository, times(1)).delete(friendship);
    }

    @Test
    void removeFriend_WithNonAcceptedFriendship_ShouldThrowBadRequestException() {
        // Arrange
        friendship.setStatus(FriendshipStatus.PENDING);
        when(authUtils.getCurrentAuthenticatedUserId()).thenReturn(currentUserId);
        when(friendshipRepository.findFriendshipBetweenUsers(currentUserId, friendUserId)).thenReturn(Optional.of(friendship));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            friendshipService.removeFriend(friendUserId);
        });

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUserId();
        verify(friendshipRepository, times(1)).findFriendshipBetweenUsers(currentUserId, friendUserId);
        verify(friendshipRepository, never()).delete(any(Friendship.class));
    }

    @Test
    void removeFriend_WithNonExistentFriendship_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(authUtils.getCurrentAuthenticatedUserId()).thenReturn(currentUserId);
        when(friendshipRepository.findFriendshipBetweenUsers(currentUserId, friendUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            friendshipService.removeFriend(friendUserId);
        });

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUserId();
        verify(friendshipRepository, times(1)).findFriendshipBetweenUsers(currentUserId, friendUserId);
        verify(friendshipRepository, never()).delete(any(Friendship.class));
    }
}