package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.team.InvitationAcceptanceResponseDto;
import edu.cda.project.ticklybackend.dtos.team.InviteMemberRequestDto;
import edu.cda.project.ticklybackend.dtos.team.TeamMemberDto;
import edu.cda.project.ticklybackend.dtos.team.UpdateMemberRoleDto;
import edu.cda.project.ticklybackend.enums.TeamMemberStatus;
import edu.cda.project.ticklybackend.enums.TokenType;
import edu.cda.project.ticklybackend.enums.UserRole;
import edu.cda.project.ticklybackend.exceptions.BadRequestException;
import edu.cda.project.ticklybackend.mappers.team.TeamMemberMapper;
import edu.cda.project.ticklybackend.models.mailing.VerificationToken;
import edu.cda.project.ticklybackend.models.structure.Structure;
import edu.cda.project.ticklybackend.models.team.Team;
import edu.cda.project.ticklybackend.models.team.TeamMember;
import edu.cda.project.ticklybackend.models.user.SpectatorUser;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.structure.StructureRepository;
import edu.cda.project.ticklybackend.repositories.team.TeamMemberRepository;
import edu.cda.project.ticklybackend.repositories.team.TeamRepository;
import edu.cda.project.ticklybackend.repositories.user.UserRepository;
import edu.cda.project.ticklybackend.security.JwtTokenProvider;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import edu.cda.project.ticklybackend.services.interfaces.MailingService;
import edu.cda.project.ticklybackend.services.interfaces.VerificationTokenService;
import edu.cda.project.ticklybackend.utils.AuthUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TeamManagementServiceImplTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository memberRepository;

    @Mock
    private StructureRepository structureRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationTokenService verificationTokenService;

    @Mock
    private MailingService mailingService;

    @Mock
    private TeamMemberMapper memberMapper;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private AuthUtils authUtils;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private TeamManagementServiceImpl teamManagementService;

    private Long structureId;
    private Long memberId;
    private Long userId;
    private User testUser;
    private Structure testStructure;
    private Team testTeam;
    private TeamMember testTeamMember;
    private TeamMemberDto teamMemberDto;
    private List<TeamMember> teamMembers;
    private List<TeamMemberDto> teamMemberDtos;
    private InviteMemberRequestDto inviteMemberRequestDto;
    private UpdateMemberRoleDto updateMemberRoleDto;
    private VerificationToken testToken;
    private String invitationToken;

    @BeforeEach
    void setUp() {
        // Set up common test data
        structureId = 1L;
        memberId = 1L;
        userId = 1L;
        invitationToken = "valid-invitation-token";

        // Create test user
        testUser = new SpectatorUser();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(UserRole.SPECTATOR);

        // Create test structure
        testStructure = new Structure();
        testStructure.setId(structureId);
        testStructure.setName("Test Structure");

        // Create test team
        testTeam = new Team();
        testTeam.setId(1L);
        testTeam.setName("Test Team");
        testTeam.setStructure(testStructure);

        // Create test team member
        testTeamMember = new TeamMember();
        testTeamMember.setId(memberId);
        testTeamMember.setTeam(testTeam);
        testTeamMember.setUser(testUser);
        testTeamMember.setEmail("test@example.com");
        testTeamMember.setRole(UserRole.ORGANIZATION_SERVICE);
        testTeamMember.setStatus(TeamMemberStatus.ACTIVE);
        testTeamMember.setJoinedAt(Instant.now());

        // Create team member DTO
        teamMemberDto = new TeamMemberDto();
        teamMemberDto.setId(memberId);
        teamMemberDto.setUserId(userId);
        teamMemberDto.setFirstName("Test");
        teamMemberDto.setLastName("User");
        teamMemberDto.setEmail("test@example.com");
        teamMemberDto.setRole(UserRole.ORGANIZATION_SERVICE);
        teamMemberDto.setStatus(TeamMemberStatus.ACTIVE);

        // Create team members list
        teamMembers = new ArrayList<>();
        teamMembers.add(testTeamMember);

        // Create team member DTOs list
        teamMemberDtos = new ArrayList<>();
        teamMemberDtos.add(teamMemberDto);

        // Create invite member request DTO
        inviteMemberRequestDto = new InviteMemberRequestDto();
        inviteMemberRequestDto.setEmail("new.member@example.com");
        inviteMemberRequestDto.setRole(UserRole.ORGANIZATION_SERVICE);

        // Create update member role DTO
        updateMemberRoleDto = new UpdateMemberRoleDto();
        updateMemberRoleDto.setRole(UserRole.RESERVATION_SERVICE);

        // Create test token
        testToken = new VerificationToken(
                invitationToken,
                testUser,
                TokenType.TEAM_INVITATION,
                Instant.now().plus(Duration.ofDays(1)),
                "{\"memberId\": " + memberId + "}"
        );
    }

    @Test
    void getTeamMembers_ShouldReturnTeamMembersList() {
        // Arrange
        when(memberRepository.findByTeamStructureId(structureId)).thenReturn(teamMembers);
        when(memberMapper.toDtoList(eq(teamMembers), any(FileStorageService.class))).thenReturn(teamMemberDtos);

        // Act
        List<TeamMemberDto> result = teamManagementService.getTeamMembers(structureId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(memberId, result.get(0).getId());
        assertEquals("test@example.com", result.get(0).getEmail());
        assertEquals(UserRole.ORGANIZATION_SERVICE, result.get(0).getRole());

        // Verify
        verify(memberRepository, times(1)).findByTeamStructureId(structureId);
        verify(memberMapper, times(1)).toDtoList(eq(teamMembers), any(FileStorageService.class));
    }

    @Test
    void inviteMember_WithExistingTeam_ShouldCreateInvitation() {
        // Arrange
        User inviter = new SpectatorUser();
        inviter.setId(2L);
        inviter.setEmail("inviter@example.com");
        inviter.setFirstName("Inviter");
        inviter.setLastName("User");

        User invitee = new SpectatorUser();
        invitee.setId(3L);
        invitee.setEmail("new.member@example.com");
        invitee.setFirstName("New");
        invitee.setLastName("Member");
        invitee.setRole(UserRole.SPECTATOR);
        invitee.setEmailValidated(true);

        TeamMember newMember = new TeamMember();
        newMember.setId(2L);
        newMember.setTeam(testTeam);
        newMember.setEmail("new.member@example.com");
        newMember.setRole(UserRole.ORGANIZATION_SERVICE);
        newMember.setStatus(TeamMemberStatus.PENDING_INVITATION);
        newMember.setUser(invitee);

        VerificationToken invitationToken = new VerificationToken(
                "new-invitation-token",
                invitee,
                TokenType.TEAM_INVITATION,
                Instant.now().plus(Duration.ofDays(1)),
                "{\"memberId\": 2}"
        );

        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(inviter);
        when(structureRepository.findById(structureId)).thenReturn(Optional.of(testStructure));
        when(teamRepository.findByStructureId(structureId)).thenReturn(Optional.of(testTeam));
        when(userRepository.existsByEmail("new.member@example.com")).thenReturn(true);
        when(userRepository.findByEmail("new.member@example.com")).thenReturn(Optional.of(invitee));
        when(memberRepository.existsByTeamIdAndEmail(testTeam.getId(), "new.member@example.com")).thenReturn(false);
        when(memberRepository.save(any(TeamMember.class))).thenReturn(newMember);
        when(verificationTokenService.createToken(eq(invitee), eq(TokenType.TEAM_INVITATION), any(Duration.class), anyString())).thenReturn(invitationToken);
        doNothing().when(mailingService).sendTeamInvitation(anyString(), anyString(), anyString(), anyString());

        // Act
        teamManagementService.inviteMember(structureId, inviteMemberRequestDto);

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUser();
        verify(structureRepository, times(1)).findById(structureId);
        verify(teamRepository, times(1)).findByStructureId(structureId);
        verify(userRepository, times(1)).existsByEmail("new.member@example.com");
        verify(userRepository, times(1)).findByEmail("new.member@example.com");
        verify(memberRepository, times(1)).existsByTeamIdAndEmail(testTeam.getId(), "new.member@example.com");
        verify(memberRepository, times(1)).save(any(TeamMember.class));
        verify(verificationTokenService, times(1)).createToken(eq(invitee), eq(TokenType.TEAM_INVITATION), any(Duration.class), anyString());
        verify(mailingService, times(1)).sendTeamInvitation(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void updateMemberRole_ShouldUpdateRoleAndReturnUpdatedMember() {
        // Arrange
        User currentUser = new SpectatorUser();
        currentUser.setId(2L);
        currentUser.setEmail("admin@example.com");

        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(currentUser);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testTeamMember));
        // Remove the unnecessary stubbing for countByTeamStructureIdAndRole
        // when(memberRepository.countByTeamStructureIdAndRole(structureId, UserRole.STRUCTURE_ADMINISTRATOR)).thenReturn(2L);
        when(memberRepository.save(testTeamMember)).thenReturn(testTeamMember);
        when(memberMapper.toDto(eq(testTeamMember), any(FileStorageService.class))).thenReturn(teamMemberDto);

        // Act
        TeamMemberDto result = teamManagementService.updateMemberRole(memberId, updateMemberRoleDto);

        // Assert
        assertNotNull(result);
        assertEquals(memberId, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(UserRole.ORGANIZATION_SERVICE, result.getRole());

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUser();
        verify(memberRepository, times(1)).findById(memberId);
        verify(memberRepository, times(1)).save(testTeamMember);
        verify(memberMapper, times(1)).toDto(eq(testTeamMember), any(FileStorageService.class));
    }

    @Test
    void updateMemberRole_WithLastAdmin_ShouldThrowBadRequestException() {
        // Arrange
        User currentUser = new SpectatorUser();
        currentUser.setId(2L);
        currentUser.setEmail("admin@example.com");

        testTeamMember.setRole(UserRole.STRUCTURE_ADMINISTRATOR);
        updateMemberRoleDto.setRole(UserRole.ORGANIZATION_SERVICE);

        when(authUtils.getCurrentAuthenticatedUser()).thenReturn(currentUser);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testTeamMember));
        when(memberRepository.countByTeamStructureIdAndRole(structureId, UserRole.STRUCTURE_ADMINISTRATOR)).thenReturn(1L);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            teamManagementService.updateMemberRole(memberId, updateMemberRoleDto);
        });

        // Verify
        verify(authUtils, times(1)).getCurrentAuthenticatedUser();
        verify(memberRepository, times(1)).findById(memberId);
        verify(memberRepository, times(1)).countByTeamStructureIdAndRole(structureId, UserRole.STRUCTURE_ADMINISTRATOR);
        verify(memberRepository, never()).save(any(TeamMember.class));
    }

    @Test
    void removeMember_ShouldRemoveMember() {
        // Arrange
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testTeamMember));
        doNothing().when(memberRepository).delete(testTeamMember);

        // Act
        teamManagementService.removeMember(memberId);

        // Verify
        verify(memberRepository, times(1)).findById(memberId);
        verify(memberRepository, times(1)).delete(testTeamMember);
    }

    @Test
    void removeMember_WithLastAdmin_ShouldThrowBadRequestException() {
        // Arrange
        testTeamMember.setRole(UserRole.STRUCTURE_ADMINISTRATOR);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testTeamMember));
        when(memberRepository.countByTeamStructureIdAndRole(structureId, UserRole.STRUCTURE_ADMINISTRATOR)).thenReturn(1L);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            teamManagementService.removeMember(memberId);
        });

        // Verify
        verify(memberRepository, times(1)).findById(memberId);
        verify(memberRepository, times(1)).countByTeamStructureIdAndRole(structureId, UserRole.STRUCTURE_ADMINISTRATOR);
        verify(memberRepository, never()).delete(any(TeamMember.class));
    }

    @Test
    void countAdminsForStructure_ShouldReturnCount() {
        // Arrange
        when(memberRepository.countByTeamStructureIdAndRole(structureId, UserRole.STRUCTURE_ADMINISTRATOR)).thenReturn(2L);

        // Act
        long result = teamManagementService.countAdminsForStructure(structureId);

        // Assert
        assertEquals(2L, result);

        // Verify
        verify(memberRepository, times(1)).countByTeamStructureIdAndRole(structureId, UserRole.STRUCTURE_ADMINISTRATOR);
    }

    @Test
    void acceptInvitation_ShouldReturnInvitationAcceptanceResponse() {
        // Arrange
        // Create a verification token with the proper structure
        VerificationToken invitationToken = new VerificationToken(
                "valid-invitation-token",
                testUser,
                TokenType.TEAM_INVITATION,
                Instant.now().plus(Duration.ofDays(1)),
                "{\"memberId\": " + memberId + "}"
        );

        when(verificationTokenService.validateToken("valid-invitation-token", TokenType.TEAM_INVITATION)).thenReturn(invitationToken);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testTeamMember));

        when(userRepository.updateUserTypeAndStructure(
                eq(userId), anyString(), anyString(), eq(structureId)
        )).thenReturn(1);

        User updatedUser = new SpectatorUser();
        updatedUser.setId(userId);
        updatedUser.setEmail("test@example.com");
        updatedUser.setFirstName("Test");
        updatedUser.setLastName("User");
        updatedUser.setRole(UserRole.ORGANIZATION_SERVICE);
        updatedUser.setStructure(testStructure);

        when(userRepository.findUserWithStructureById(userId)).thenReturn(Optional.of(updatedUser));

        when(jwtTokenProvider.generateAccessToken(updatedUser)).thenReturn("new-jwt-token");
        when(jwtTokenProvider.getExpirationInMillis()).thenReturn(3600000L);

        doNothing().when(verificationTokenService).markTokenAsUsed(invitationToken);

        // Act
        InvitationAcceptanceResponseDto result = teamManagementService.acceptInvitation("valid-invitation-token");

        // Assert
        assertNotNull(result);
        assertEquals("new-jwt-token", result.getAccessToken());
        assertEquals(3600000L, result.getExpiresIn());
        assertEquals(structureId, result.getStructureId());
        assertEquals("Test Structure", result.getStructureName());
        assertEquals("Invitation acceptée avec succès ! Vous êtes maintenant membre de l'équipe.", result.getMessage());

        // Verify
        verify(verificationTokenService, times(1)).validateToken("valid-invitation-token", TokenType.TEAM_INVITATION);
        verify(memberRepository, times(1)).findById(memberId);
        verify(userRepository, times(1)).updateUserTypeAndStructure(
                eq(userId), anyString(), anyString(), eq(structureId)
        );
        verify(userRepository, times(1)).findUserWithStructureById(userId);
        verify(memberRepository, times(1)).save(testTeamMember);
        verify(verificationTokenService, times(1)).markTokenAsUsed(invitationToken);
        verify(jwtTokenProvider, times(1)).generateAccessToken(updatedUser);
    }

    @Test
    void dissolveTeam_ShouldDissolveTeamAndConvertUsers() {
        // Arrange
        when(structureRepository.existsById(structureId)).thenReturn(true);
        when(userRepository.convertAllStructureUsersToSpectator(structureId)).thenReturn(2);
        when(teamRepository.findByStructureId(structureId)).thenReturn(Optional.of(testTeam));
        when(memberRepository.deleteByTeamId(testTeam.getId())).thenReturn(2L);
        doNothing().when(teamRepository).delete(testTeam);

        // Act
        teamManagementService.dissolveTeam(structureId);

        // Verify
        verify(structureRepository, times(1)).existsById(structureId);
        verify(userRepository, times(1)).convertAllStructureUsersToSpectator(structureId);
        verify(userRepository, times(1)).flush();
        verify(teamRepository, times(1)).findByStructureId(structureId);
        verify(memberRepository, times(1)).deleteByTeamId(testTeam.getId());
        verify(teamRepository, times(1)).delete(testTeam);
    }

    @Test
    void dissolveTeam_WithNonExistentStructure_ShouldDoNothing() {
        // Arrange
        when(structureRepository.existsById(structureId)).thenReturn(false);

        // Act
        teamManagementService.dissolveTeam(structureId);

        // Verify
        verify(structureRepository, times(1)).existsById(structureId);
        verify(userRepository, never()).convertAllStructureUsersToSpectator(anyLong());
        verify(teamRepository, never()).findByStructureId(anyLong());
        verify(memberRepository, never()).deleteByTeamId(anyLong());
        verify(teamRepository, never()).delete(any(Team.class));
    }
}
