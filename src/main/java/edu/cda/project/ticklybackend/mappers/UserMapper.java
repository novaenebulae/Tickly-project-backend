package edu.cda.project.ticklybackend.mappers;


import edu.cda.project.ticklybackend.dtos.auth.AuthResponseDto;
import edu.cda.project.ticklybackend.models.user.StaffUser;
import edu.cda.project.ticklybackend.models.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Value;

// @Mapper indique à MapStruct de générer une implémentation pour cette interface.
// componentModel = "spring" permet d'injecter ce mapper comme un bean Spring.
@Mapper(componentModel = "spring", imports = {StaffUser.class})
public abstract class UserMapper {

    // Juste pour qu'il reste dans les imports car utilisé dans l'expression du mapping StructureId
    protected StaffUser staffUser;

    // Injection de la base URL pour les fichiers statiques (définie dans application.properties)
    // Cette approche nécessite que le mapper soit un bean Spring.
    @Value("${file.static-base-url:}") // : pour une valeur par défaut si non trouvée
    protected String staticBaseUrl;


    // Convertit une entité User en AuthResponseDto
    // Le mapping de 'expiresIn' et 'accessToken' se fera manuellement dans le service
    // car ils ne proviennent pas directement de l'entité User.
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "expiresIn", ignore = true)
    @Mapping(target = "tokenType", ignore = true) // Sera "Bearer" par défaut
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "avatarUrl", source = "avatarPath", qualifiedByName = "buildAvatarUrl")
    @Mapping(target = "structureId", expression = "java(user instanceof StaffUser? ((StaffUser) user).getStructure()!= null? ((StaffUser) user).getStructure().getId() : null : null)")
    public abstract AuthResponseDto userToAuthResponseDto(User user);

    // Méthode qualifiée pour construire l'URL de l'avatar
    @Named("buildAvatarUrl")
    protected String buildAvatarUrl(String avatarPath) {
        if (avatarPath == null
                || avatarPath.isBlank()
                || staticBaseUrl == null
                || staticBaseUrl.isBlank()) {
            return null;
        }
        // Assure qu'il n'y a pas de double slash entre staticBaseUrl et avatarPath
        String baseUrl = staticBaseUrl.endsWith("/") ? staticBaseUrl : staticBaseUrl + "/";
        String path = avatarPath.startsWith("/") ? avatarPath.substring(1) : avatarPath;
        return baseUrl + path;
    }
}