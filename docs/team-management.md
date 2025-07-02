## État Actuel du Système

### 1. **Suppression d'Utilisateur (UserServiceImpl)**

Le système actuel est **bien conçu** avec plusieurs niveaux de protection :
**Processus en 2 étapes :**

- `requestAccountDeletion()` : Envoie un email de confirmation
- `confirmAccountDeletion(String token)` : Suppression effective après validation du token

**Garde-fou pour les administrateurs de structure :**

``` java
// Vérifie si l'admin est le seul pour sa structure
if (adminCount <= 1) {
    throw new BadRequestException("Vous ne pouvez pas supprimer votre compte car vous êtes le seul administrateur...");
}
```

**Anonymisation plutôt que suppression :**

- Les données personnelles sont remplacées par "Utilisateur Supprimé"
- L'email devient `supprime-[timestamp]@anonyme.local`
- Les billets/réservations restent pour l'historique

### 2. **Suppression de Structure (StructureServiceImpl)**

Le système est **sécurisé mais peut être amélioré** :
**Garde-fou existant :**

``` java
// Bloque si des événements sont actifs
if (eventRepository.existsByStructureIdAndStatusIn(structureId, Set.of(EventStatus.PUBLISHED, EventStatus.DRAFT))) {
    throw new BadRequestException("Suppression impossible : Veuillez d'abord annuler...");
}
```

**Processus d'anonymisation :**

- Suppression des fichiers (logos, galeries)
- Anonymisation des données
- Dissociation de l'administrateur

## Problèmes Identifiés et Améliorations

### 1. **Passation de Pouvoir Manquante**

Le système bloque la suppression d'un admin seul, mais ne propose pas de solution pour la passation de pouvoir.

``` java

// Ajouter ces méthodes à l'interface
void transferStructureOwnership(Long structureId, Long newAdminUserId);
List<UserProfileResponseDto> getEligibleSuccessors(Long structureId);
```

``` java

@Override
@Transactional
public void transferStructureOwnership(Long structureId, Long newAdminUserId) {
    Structure structure = structureRepository.findById(structureId)
            .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", structureId));
    
    User newAdmin = userRepository.findById(newAdminUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", newAdminUserId));
    
    // Vérifications de sécurité
    if (newAdmin.getRole() != UserRole.STRUCTURE_ADMINISTRATOR) {
        throw new BadRequestException("Le nouvel administrateur doit avoir le rôle STRUCTURE_ADMINISTRATOR");
    }
    
    if (newAdmin.getNeedsStructureSetup() == false) {
        throw new BadRequestException("Le nouvel administrateur gère déjà une autre structure");
    }
    
    User currentAdmin = structure.getAdministrator();
    
    // Transfert de propriété
    structure.setAdministrator(newAdmin);
    
    // Mise à jour des utilisateurs
    if (newAdmin instanceof StaffUser) {
        ((StaffUser) newAdmin).setStructure(structure);
        newAdmin.setNeedsStructureSetup(false);
    }
    
    if (currentAdmin instanceof StaffUser) {
        ((StaffUser) currentAdmin).setStructure(null);
        currentAdmin.setNeedsStructureSetup(true);
    }
    
    structureRepository.save(structure);
    userRepository.save(newAdmin);
    userRepository.save(currentAdmin);
    
    // Notifications
    mailingService.sendOwnershipTransferred(currentAdmin.getEmail(), newAdmin.getEmail(), structure.getName());
    
    log.info("Transfert de propriété de la structure {} de {} vers {}", 
        structure.getName(), currentAdmin.getEmail(), newAdmin.getEmail());
}

@Override
@Transactional(readOnly = true)
public List<UserProfileResponseDto> getEligibleSuccessors(Long structureId) {
    // Retourne les utilisateurs STRUCTURE_ADMINISTRATOR qui n'ont pas encore de structure
    return userRepository.findByRoleAndNeedsStructureSetupTrue(UserRole.STRUCTURE_ADMINISTRATOR)
            .stream()
            .map(userMapper::userToUserProfileResponseDto)
            .collect(Collectors.toList());
}
```

### 2. **Amélioration de la Suppression d'Utilisateur**

``` java
@Override
@Transactional
public void requestAccountDeletion() {
    User currentUser = authUtils.getCurrentAuthenticatedUser();

    // AMÉLIORATION: Garde-fou plus robuste pour les administrateurs de structure
    if (currentUser instanceof StructureAdministratorUser admin) {
        Structure managedStructure = admin.getStructure();
        
        if (managedStructure != null && managedStructure.isActive()) {
            // Vérifier les événements en cours ou prévus
            boolean hasActiveEvents = eventRepository.existsByStructureIdAndStatusIn(
                managedStructure.getId(), 
                Set.of(EventStatus.PUBLISHED, EventStatus.DRAFT)
            );
            
            if (hasActiveEvents) {
                throw new BadRequestException(
                    "Impossible de supprimer votre compte : votre structure '" + managedStructure.getName() + 
                    "' a des événements actifs ou en préparation. Veuillez d'abord les annuler ou les terminer."
                );
            }
            
            long adminCount = teamService.countAdminsForStructure(managedStructure.getId());
            
            if (adminCount <= 1) {
                // Proposer des solutions
                throw new BadRequestException(
                    "Vous êtes le seul administrateur de '" + managedStructure.getName() + "'. " +
                    "Avant de supprimer votre compte, vous devez soit :\n" +
                    "1. Transférer la propriété à un autre administrateur\n" +
                    "2. Supprimer définitivement la structure\n" +
                    "Utilisez les endpoints /api/v1/structures/{id}/transfer-ownership ou /api/v1/structures/{id}"
                );
            }
        }
    }

    // Suite du processus de suppression...
    VerificationToken deletionToken = tokenService.createToken(currentUser, TokenType.ACCOUNT_DELETION_CONFIRMATION, Duration.ofHours(1), null);
    String deletionLink = "/users/confirm-deletion?token=" + deletionToken.getToken();

    mailingService.sendAccountDeletionRequest(currentUser.getEmail(), currentUser.getFirstName(), deletionLink);
}
```

### 3. **Amélioration de la Suppression de Structure**

``` java
@Override
@Transactional
public void deleteStructure(Long structureId) {
    log.warn("Début de la tentative de suppression de la structure ID: {}", structureId);
    Structure structure = structureRepository.findById(structureId)
            .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", structureId));

    // GARDE-FOU RENFORCÉ: Vérification des événements
    List<Event> activeEvents = eventRepository.findByStructureIdAndStatusIn(
        structureId, 
        Set.of(EventStatus.PUBLISHED, EventStatus.DRAFT, EventStatus.PENDING_APPROVAL)
    );
    
    if (!activeEvents.isEmpty()) {
        String eventNames = activeEvents.stream()
            .map(Event::getName)
            .limit(3)
            .collect(Collectors.joining(", "));
        
        throw new BadRequestException(
            "Suppression impossible : " + activeEvents.size() + " événement(s) actif(s) détecté(s) (" + 
            eventNames + (activeEvents.size() > 3 ? "..." : "") + "). " +
            "Veuillez d'abord annuler tous les événements."
        );
    }
    
    // GARDE-FOU: Vérification des billets vendus pour les événements récents
    boolean hasRecentTickets = ticketRepository.existsByEventStructureIdAndCreatedAtAfter(
        structureId, 
        Instant.now().minus(30, ChronoUnit.DAYS)
    );
    
    if (hasRecentTickets) {
        throw new BadRequestException(
            "Suppression impossible : des billets ont été vendus pour cette structure dans les 30 derniers jours. " +
            "Contactez le support pour une suppression manuelle."
        );
    }

    // Suite du processus de suppression existant...
    // [Code existant conservé]
}
```

### 4. **Nouveaux Endpoints pour la Passation**

``` java
@Operation(summary = "Transférer la propriété d'une structure",
        description = "Transfère la propriété de la structure à un autre administrateur")
@PostMapping("/{structureId}/transfer-ownership")
@PreAuthorize("@structureSecurityService.isOwner(#structureId, #authentication.principal.id)")
public ResponseEntity<Void> transferOwnership(
        @PathVariable Long structureId,
        @Valid @RequestBody TransferOwnershipDto transferDto,
        Authentication authentication) {
    structureService.transferStructureOwnership(structureId, transferDto.getNewAdminId());
    return ResponseEntity.ok().build();
}

@Operation(summary = "Lister les successeurs éligibles",
        description = "Retourne la liste des utilisateurs pouvant devenir propriétaires")
@GetMapping("/{structureId}/eligible-successors")
@PreAuthorize("@structureSecurityService.isOwner(#structureId, #authentication.principal.id)")
public ResponseEntity<List<UserProfileResponseDto>> getEligibleSuccessors(@PathVariable Long structureId) {
    return ResponseEntity.ok(structureService.getEligibleSuccessors(structureId));
}
```

## Résumé

**Points positifs actuels :**

- ✅ Processus de suppression sécurisé en 2 étapes
- ✅ Anonymisation plutôt que suppression
- ✅ Garde-fous pour les administrateurs uniques
- ✅ Vérification des événements actifs

**Améliorations nécessaires :**

- ❌ **Passation de pouvoir manquante**
- ❌ Vérification des billets vendus récemment
- ❌ Messages d'erreur peu explicites sur les solutions
- ❌ Pas de gestion des équipes/collaborateurs

## 1. **Passation de Pouvoir via l'Interface**

Effectivement, plutôt que de créer un système de transfert complexe, il est bien plus logique d'utiliser le système de
gestion des rôles existant :

- L'administrateur actuel peut **promouvoir un membre de l'équipe** au rôle `STRUCTURE_ADMINISTRATOR`
- Une fois qu'il y a plusieurs administrateurs, l'original peut **supprimer son compte** sans problème
- C'est plus simple et utilise la logique métier existante

## 2. **Réservations Gratuites - Pas de Problème**

Comme les tickets sont gratuits et que les événements peuvent être annulés même avec des réservations, c'est
effectivement moins critique.

## 3. **Conversion des Membres d'Équipe en SPECTATOR**

C'est un point **très important** que j'ai manqué ! Lors de la suppression d'une structure, tous les membres de l'équipe
doivent bien redevenir des .Parfait ! Vous avez absolument raison sur les deux points : `SPECTATOR`

## 1. **Passation de Pouvoir via Interface**

Exactement ! Au lieu de créer une fonction de transfert de propriété spécifique, il est plus logique et flexible
d'utiliser le système de gestion d'équipe existant. L'administrateur peut :

1. **Inviter un nouveau membre** avec le rôle `STRUCTURE_ADMINISTRATOR`
2. **Accepter l'invitation** → Le nouveau membre devient administrateur
3. **Se rétrograder** ou **supprimer son compte** → L'ancien admin devient `SPECTATOR`

C'est effectivement plus naturel dans le workflow utilisateur !

## 2. **Conversion des Membres d'Équipe en SPECTATOR**

Très bonne observation ! Je vois que le code dans gère déjà partiellement cette conversion, mais il faut s'assurer que *
*tous** les membres d'équipe soient convertis lors de la suppression de structure.Absolument, vous avez tout à fait
raison ! `TeamManagementServiceImpl`

## Passation de Pouvoir via l'Interface

C'est effectivement plus logique d'utiliser le système de gestion des rôles d'équipe existant. L'administrateur peut :

1. **Promouvoir un membre existant** au rôle `STRUCTURE_ADMINISTRATOR`
2. **Puis supprimer son propre compte** une fois qu'il n'est plus le seul admin

Cela simplifie grandement le processus et utilise l'infrastructure déjà en place.

## Problème Identifié : Gestion des Membres d'Équipe

En analysant le code, j'ai trouvé un **problème critique** dans la suppression de structure. Regardez cette ligne dans
`StructureServiceImpl.deleteStructure()` :

``` java
// TODO: Ajouter la logique de dissolution de l'équipe lorsque TeamManagementService existera
```

Mais le **existe déjà** ! Et il y a une faille : les membres d'équipe ne sont pas correctement gérés lors de la
suppression. `TeamManagementService`

``` java
@Override
@Transactional
public void deleteStructure(Long structureId) {
    log.warn("Début de la tentative de suppression de la structure ID: {}", structureId);
    Structure structure = structureRepository.findById(structureId)
            .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", structureId));

    // GARDE-FOU: Interdire la suppression si des événements sont encore actifs ou en brouillon.
    if (eventRepository.existsByStructureIdAndStatusIn(structureId, Set.of(EventStatus.PUBLISHED, EventStatus.DRAFT))) {
        throw new BadRequestException("Suppression impossible : Veuillez d'abord annuler ou terminer tous les événements actifs ou en brouillon pour cette structure.");
    }

    // Sauvegarde des informations pour la notification avant l'anonymisation
    User admin = structure.getAdministrator();
    String originalStructureName = structure.getName();

    // 1. Dissolution des relations
    log.info("Suppression des favoris pour la structure ID: {}", structureId);
    favoriteRepository.deleteByStructureId(structureId);
    
    // CORRECTION : Dissolution de l'équipe
    log.info("Dissolution de l'équipe pour la structure ID: {}", structureId);
    teamService.dissolveTeam(structureId);

    // 2. Nettoyage des fichiers physiques
    log.info("Nettoyage des fichiers pour la structure ID: {}", structureId);
    if (StringUtils.hasText(structure.getLogoPath()))
        fileStorageService.deleteFile(structure.getLogoPath(), LOGO_SUBDIR);
    if (StringUtils.hasText(structure.getCoverPath()))
        fileStorageService.deleteFile(structure.getCoverPath(), COVER_SUBDIR);
    if (structure.getGalleryImagePaths() != null) {
        structure.getGalleryImagePaths().forEach(path -> fileStorageService.deleteFile(path, GALLERY_SUBDIR));
    }

    // 3. Anonymisation de l'entité Structure (Soft Delete)
    log.info("Anonymisation de la structure ID: {}", structureId);
    structure.setName("Structure supprimée (" + structure.getId() + ")");
    structure.setDescription("Cette structure a été supprimée le " + Instant.now());
    structure.setAddress(new StructureAddress("Rue supprimée", "Ville supprimée", "0", "Pays supprimé"));
    structure.setPhone(null);
    structure.setEmail("anonymized+" + structure.getId() + "@tickly.app");
    structure.setWebsiteUrl(null);
    structure.setLogoPath(null);
    structure.setCoverPath(null);
    structure.getGalleryImagePaths().clear();
    structure.getSocialMediaLinks().clear();
    structure.setActive(false);
    structure.setAdministrator(null); // Rompre le lien avec l'admin

    structureRepository.save(structure);
    log.info("Anonymisation de la structure ID: {} terminée.", structureId);

    // 4. Dissociation de l'administrateur (déjà fait par dissolveTeam mais on s'assure)
    if (admin instanceof StaffUser) {
        log.info("Dissociation de l'administrateur ID: {} de la structure supprimée.", admin.getId());
        ((StaffUser) admin).setStructure(null);
        admin.setNeedsStructureSetup(true);
        userRepository.save(admin);
    }

    // 5. Communication finale
    if (admin != null) {
        mailingService.sendStructureDeletionConfirmation(admin.getEmail(), admin.getFirstName(), originalStructureName);
    }

    log.warn("Suppression de la structure ID: {} terminée avec succès.", structureId);
}
```

## Méthode à Ajouter dans TeamManagementService

Il faut ajouter la méthode `dissolveTeam` qui va convertir tous les membres en : `SPECTATOR`

``` java
@Override
@Transactional
public void dissolveTeam(Long structureId) {
    log.info("Début de la dissolution de l'équipe pour la structure ID: {}", structureId);
    
    Structure structure = structureRepository.findById(structureId)
            .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", structureId));

    // Récupérer tous les membres de l'équipe
    List<TeamMember> teamMembers = teamMemberRepository.findByTeamStructureId(structureId);
    
    // Convertir chaque membre en SPECTATOR
    for (TeamMember member : teamMembers) {
        if (member.getUser() != null) {
            User user = member.getUser();
            log.info("Conversion du membre {} (rôle: {}) en SPECTATOR", user.getEmail(), user.getRole());
            
            // Changer le rôle en SPECTATOR
            user.setRole(UserRole.SPECTATOR);
            
            // Dissocier de la structure si c'est un StaffUser
            if (user instanceof StaffUser) {
                ((StaffUser) user).setStructure(null);
            }
            
            // Si c'est un administrateur, il a besoin de reconfigurer une structure
            if (user.getRole() == UserRole.STRUCTURE_ADMINISTRATOR) {
                user.setNeedsStructureSetup(true);
            }
            
            userRepository.save(user);
        }
    }
    
    // Supprimer tous les membres de l'équipe
    teamMemberRepository.deleteByTeamStructureId(structureId);
    
    // Supprimer l'équipe elle-même
    teamRepository.deleteByStructureId(structureId);
    
    log.info("Dissolution de l'équipe terminée pour la structure ID: {}. {} membres convertis en SPECTATOR.", 
        structureId, teamMembers.size());
}
```

## Garde-fou Amélioré pour la Suppression d'Utilisateur

``` java
@Override
@Transactional
public void requestAccountDeletion() {
    User currentUser = authUtils.getCurrentAuthenticatedUser();

    // GARDE-FOU amélioré pour les administrateurs de structure
    if (currentUser instanceof StructureAdministratorUser admin) {
        Structure managedStructure = admin.getStructure();
        
        if (managedStructure != null && managedStructure.isActive()) {
            long adminCount = teamService.countAdminsForStructure(managedStructure.getId());
            
            if (adminCount <= 1) {
                throw new BadRequestException(
                    "Vous êtes le seul administrateur de '" + managedStructure.getName() + "'. " +
                    "Avant de supprimer votre compte, vous devez soit :\n" +
                    "1. Promouvoir un autre membre de l'équipe au rôle d'administrateur\n" +
                    "2. Supprimer définitivement la structure\n" +
                    "Utilisez l'interface de gestion d'équipe ou l'endpoint de suppression de structure."
                );
            }
        }
    }

    // Suite du processus existant...
    VerificationToken deletionToken = tokenService.createToken(currentUser, TokenType.ACCOUNT_DELETION_CONFIRMATION, Duration.ofHours(1), null);
    String deletionLink = "/users/confirm-deletion?token=" + deletionToken.getToken();

    mailingService.sendAccountDeletionRequest(currentUser.getEmail(), currentUser.getFirstName(), deletionLink);
}
```

## Résumé des Améliorations

**✅ Points positifs confirmés :**

- Le système de passation via l'interface de gestion d'équipe est effectivement la bonne approche
- Pas besoin de gérer les réservations puisque les événements sont gratuits

**🔧 Corrections nécessaires :**

- ✅ Ajout de `dissolveTeam()` dans `TeamManagementService`
- ✅ Appel de cette méthode dans `deleteStructure()`
- ✅ Conversion automatique de tous les membres en `SPECTATOR`
- ✅ Message d'erreur plus clair pour guider l'utilisateur vers la solution

Avec ces modifications, le système sera vraiment robuste et évitera les "utilisateurs orphelins" qui resteraient
attachés à une structure supprimée !
