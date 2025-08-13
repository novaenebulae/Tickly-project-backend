package edu.cda.project.ticklybackend.mappers.user;

import edu.cda.project.ticklybackend.dtos.friendship.FriendResponseDto;
import edu.cda.project.ticklybackend.dtos.friendship.ReceivedFriendRequestResponseDto;
import edu.cda.project.ticklybackend.dtos.friendship.SentFriendRequestResponseDto;
import edu.cda.project.ticklybackend.enums.FriendshipStatus;
import edu.cda.project.ticklybackend.models.user.Friendship;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class FriendshipMapperTest {

    private FriendshipMapper friendshipMapper;

    @Mock
    private FileStorageService fileStorageService;

    private User sender;
    private User receiver;
    private Friendship friendship;
    private Instant testInstant;

    @BeforeEach
    void setUp() {
        friendshipMapper = Mappers.getMapper(FriendshipMapper.class);

        // Create test users
        sender = new User();
        sender.setId(1L);
        sender.setFirstName("Sender");
        sender.setLastName("User");
        sender.setEmail("sender@example.com");
        sender.setAvatarPath("sender-avatar.jpg");

        receiver = new User();
        receiver.setId(2L);
        receiver.setFirstName("Receiver");
        receiver.setLastName("User");
        receiver.setEmail("receiver@example.com");
        receiver.setAvatarPath("receiver-avatar.jpg");

        // Create test instant
        testInstant = Instant.parse("2023-01-01T12:00:00Z");

        // Create test friendship
        friendship = new Friendship();
        friendship.setId(1L);
        friendship.setSender(sender);
        friendship.setReceiver(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendship.setCreatedAt(testInstant);
        friendship.setUpdatedAt(testInstant);

        // Mock file storage service
        lenient().when(fileStorageService.getFileUrl(anyString(), eq("avatars")))
                .thenAnswer(invocation -> "http://example.com/avatars/" + invocation.getArgument(0));
    }

    @Test
    void toZonedDateTime_ShouldConvertInstantToZonedDateTime() {
        // Act
        ZonedDateTime result = friendshipMapper.toZonedDateTime(testInstant);

        // Assert
        assertNotNull(result);
        assertEquals(ZonedDateTime.ofInstant(testInstant, ZoneOffset.UTC), result);
    }

    @Test
    void toFriendResponseDto_ShouldMapCorrectly() {
        // Act
        FriendResponseDto result = friendshipMapper.toFriendResponseDto(friendship, sender.getId(), fileStorageService);

        // Assert
        assertNotNull(result);
        assertEquals(friendship.getId(), result.getFriendshipId());
        assertEquals(ZonedDateTime.ofInstant(testInstant, ZoneOffset.UTC), result.getSince());
        assertNotNull(result.getFriend());
        assertEquals(receiver.getId(), result.getFriend().getId());
        assertEquals(receiver.getFirstName(), result.getFriend().getFirstName());
        assertEquals(receiver.getLastName(), result.getFriend().getLastName());
    }

    @Test
    void toReceivedFriendRequestDto_ShouldMapCorrectly() {
        // Act
        ReceivedFriendRequestResponseDto result = friendshipMapper.toReceivedFriendRequestDto(friendship, fileStorageService);

        // Assert
        assertNotNull(result);
        assertEquals(friendship.getId(), result.getFriendshipId());
        assertEquals(ZonedDateTime.ofInstant(testInstant, ZoneOffset.UTC), result.getRequestedAt());
        assertNotNull(result.getSender());
        assertEquals(sender.getId(), result.getSender().getId());
        assertEquals(sender.getFirstName(), result.getSender().getFirstName());
        assertEquals(sender.getLastName(), result.getSender().getLastName());
    }

    @Test
    void toSentFriendRequestDto_ShouldMapCorrectly() {
        // Act
        SentFriendRequestResponseDto result = friendshipMapper.toSentFriendRequestDto(friendship, fileStorageService);

        // Assert
        assertNotNull(result);
        assertEquals(friendship.getId(), result.getFriendshipId());
        assertEquals(ZonedDateTime.ofInstant(testInstant, ZoneOffset.UTC), result.getSentAt());
        assertNotNull(result.getReceiver());
        assertEquals(receiver.getId(), result.getReceiver().getId());
        assertEquals(receiver.getFirstName(), result.getReceiver().getFirstName());
        assertEquals(receiver.getLastName(), result.getReceiver().getLastName());
    }
}
