package edu.cda.project.ticklybackend.mappers.team;

import edu.cda.project.ticklybackend.dtos.team.TeamMemberDto;
import edu.cda.project.ticklybackend.models.team.TeamMember;
import edu.cda.project.ticklybackend.services.interfaces.FileStorageService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TeamMemberMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(target = "avatarUrl", expression = "java(buildAvatarUrl(member, fileStorageService))")
    TeamMemberDto toDto(TeamMember member, @Context FileStorageService fileStorageService);

    List<TeamMemberDto> toDtoList(List<TeamMember> members, @Context FileStorageService fileStorageService);

    default String buildAvatarUrl(TeamMember member, FileStorageService fileStorageService) {
        if (member.getUser() == null || member.getUser().getAvatarPath() == null || fileStorageService == null) {
            return null;
        }
        return fileStorageService.getFileUrl(member.getUser().getAvatarPath(), "avatars");
    }

    /**
     * Convertit un Instant (depuis l'entité) en ZonedDateTime (pour les DTOs), en forçant le fuseau UTC (Z).
     */
    default ZonedDateTime toZonedDateTime(Instant instant) {
        if (instant == null) return null;
        return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
