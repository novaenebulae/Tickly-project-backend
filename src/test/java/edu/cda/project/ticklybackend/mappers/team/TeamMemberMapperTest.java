package edu.cda.project.ticklybackend.mappers.team;

import edu.cda.project.ticklybackend.dtos.team.TeamMemberDto;
import edu.cda.project.ticklybackend.enums.TeamMemberStatus;
import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.models.team.Team;
import edu.cda.project.ticklybackend.models.team.TeamMember;
import edu.cda.project.ticklybackend.models.user.SpectatorUser;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TeamMemberMapperTest {

    private TeamMemberMapper teamMemberMapper;

    @Mock
    private FileStorageService fileStorageService;

    private User user;
    private Team team;
    private TeamMember teamMember;
    private Instant testInstant;

    @BeforeEach
    void setUp() {
        teamMemberMapper = Mappers.getMapper(TeamMemberMapper.class);

        // Create test user
        user = new SpectatorUser();
        user.setId(1L);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        user.setRole(UserRole.SPECTATOR);
        user.setAvatarPath("test-avatar.jpg");

        // Create test team
        team = new Team();
        team.setId(1L);
        team.setName("Test Team");

        // Create test instant
        testInstant = Instant.parse("2023-01-01T12:00:00Z");

        // Create test team member
        teamMember = new TeamMember();
        teamMember.setId(1L);
        teamMember.setTeam(team);
        teamMember.setUser(user);
        teamMember.setEmail("test@example.com");
        teamMember.setRole(UserRole.SPECTATOR);
        teamMember.setStatus(TeamMemberStatus.ACTIVE);
        teamMember.setInvitedAt(testInstant);
        teamMember.setJoinedAt(testInstant);
    }

    @Test
    void toZonedDateTime_ShouldConvertInstantToZonedDateTime() {
        // Act
        ZonedDateTime result = teamMemberMapper.toZonedDateTime(testInstant);

        // Assert
        assertNotNull(result);
        assertEquals(ZonedDateTime.ofInstant(testInstant, ZoneOffset.UTC), result);
    }

    @Test
    void toDto_ShouldMapCorrectly() {
        // Setup mock for this test only
        when(fileStorageService.getFileUrl(anyString(), eq("avatars")))
                .thenAnswer(invocation -> "http://example.com/avatars/" + invocation.getArgument(0));

        // Act
        TeamMemberDto result = teamMemberMapper.toDto(teamMember, fileStorageService);

        // Assert
        assertNotNull(result);
        assertEquals(teamMember.getId(), result.getId());
        assertEquals(user.getId(), result.getUserId());
        assertEquals(user.getFirstName(), result.getFirstName());
        assertEquals(user.getLastName(), result.getLastName());
        assertEquals(teamMember.getEmail(), result.getEmail());
        assertEquals(teamMember.getRole(), result.getRole());
        assertEquals(teamMember.getStatus(), result.getStatus());
        assertEquals(ZonedDateTime.ofInstant(testInstant, ZoneOffset.UTC), result.getJoinedAt());
        assertEquals("http://example.com/avatars/test-avatar.jpg", result.getAvatarUrl());
    }
}
