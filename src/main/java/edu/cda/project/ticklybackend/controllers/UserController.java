package edu.cda.project.ticklybackend.controllers; // Adaptez si vous avez un sous-package 'user'

import edu.cda.project.ticklybackend.dtos.file.FileUploadResponseDto;
import edu.cda.project.ticklybackend.dtos.user.FavoriteStructureRequestDto;
import edu.cda.project.ticklybackend.dtos.user.UserFavoriteStructureDto;
import edu.cda.project.ticklybackend.dtos.user.UserProfileResponseDto;
import edu.cda.project.ticklybackend.dtos.user.UserProfileUpdateDto;
import edu.cda.project.ticklybackend.services.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Gestion des Utilisateurs", description = "Endpoints pour gérer les profils utilisateurs et les favoris.")
@SecurityRequirement(name = "bearerAuth") // Indique que les endpoints ici nécessitent une authentification Bearer
public class UserController {

    private final UserService userService;

    @Operation(summary = "Récupérer le profil de l'utilisateur authentifié")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserProfileResponseDto.class)))
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponseDto> getMyProfile() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @Operation(summary = "Mettre à jour le profil de l'utilisateur authentifié")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserProfileResponseDto.class)))
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponseDto> updateMyProfile(@Valid @RequestBody UserProfileUpdateDto updateDto) {
        return ResponseEntity.ok(userService.updateCurrentUserProfile(updateDto));
    }

    @Operation(summary = "Uploader ou mettre à jour l'avatar de l'utilisateur authentifié")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = FileUploadResponseDto.class)))
    @PostMapping(value = "/me/avatar", consumes = {"multipart/form-data"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileUploadResponseDto> uploadMyAvatar(@RequestPart("file") MultipartFile file) {
        String fileUrl = userService.updateCurrentUserAvatar(file);
        return ResponseEntity.ok(new FileUploadResponseDto(file.getOriginalFilename(), fileUrl, "Avatar mis à jour avec succès."));
    }

//    @Operation(summary = "Récupérer le profil d'un utilisateur par ID (Admin)",
//            description = "Nécessite le rôle SYSTEM_ADMINISTRATOR.")
//    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserProfileResponseDto.class)))
//    @GetMapping("/{userId}")
//    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
//    public ResponseEntity<UserProfileResponseDto> getUserProfileById(@PathVariable Long userId) {
//        return ResponseEntity.ok(userService.getUserProfile(userId));
//    }
//
//    @Operation(summary = "Rechercher des utilisateurs")
//    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Page.class)))
//    // Page<UserSearchResponseDto>
//    @GetMapping("/search")
//    @PreAuthorize("isAuthenticated()")
//    public ResponseEntity<Page<UserSearchResponseDto>> searchUsers(
//            @RequestParam(required = false) String query,
//            @ParameterObject Pageable pageable) {
//        return ResponseEntity.ok(userService.searchUsers(query, pageable));
//    }

    @Operation(summary = "Lister les structures favorites de l'utilisateur authentifié")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = List.class)))
    // List<UserFavoriteStructureDto>
    @GetMapping("/me/favorites/structures")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserFavoriteStructureDto>> getMyFavoriteStructures() {
        return ResponseEntity.ok(userService.getCurrentUserFavoriteStructures());
    }

    @Operation(summary = "Ajouter une structure aux favoris de l'utilisateur authentifié")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = UserFavoriteStructureDto.class)))
    @PostMapping("/me/favorites/structures")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserFavoriteStructureDto> addMyFavoriteStructure(
            @Valid @RequestBody FavoriteStructureRequestDto requestDto) {
        UserFavoriteStructureDto favoriteDto = userService.addCurrentUserFavoriteStructure(requestDto.getStructureId());
        return new ResponseEntity<>(favoriteDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Retirer une structure des favoris de l'utilisateur authentifié")
    @ApiResponse(responseCode = "204")
    @DeleteMapping("/me/favorites/structures/{structureId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeMyFavoriteStructure(
            @Parameter(description = "ID de la structure à retirer des favoris") @PathVariable Long structureId) {
        userService.removeCurrentUserFavoriteStructure(structureId);
        return ResponseEntity.noContent().build();
    }
}