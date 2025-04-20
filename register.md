## Flux avec Connexion Automatique Post-Inscription

---

1.  ### **Étape 1 : Inscription Utilisateur et Connexion Automatique**
    * **Frontend (Angular - `RegisterComponent`)**
        * L'utilisateur soumet le formulaire d'inscription (email, mot de passe, etc.).
        * Envoi d'une requête `POST` vers `/api/auth/register` avec les données.
    * **Backend (Spring - Endpoint `/api/auth/register` - **MODIFIÉ**)
        * **(Dans une transaction)**
            * Valide les données, vérifie l'unicité de l'email, hashe le mot de passe.
            * Crée l'entité `User` (avec `structure_id = null`).
            * Sauvegarde l'entité `User`.
            * **Si la sauvegarde réussit :**
                * **Génère un JWT** pour cet utilisateur nouvellement créé. Les claims incluent `sub` (email), `userId`,
                  `roles`, et crucialement **`needsStructureSetup: true`** (puisque `structure_id` est null à ce stade).
                * **(Fin de la transaction)**
                * **Renvoie une réponse `200 OK` ou `201 Created` contenant le JWT et
                  l'indicateur `needsStructureSetup: true`**.
            * **Si la sauvegarde échoue :**
                * **(Rollback de la transaction)**
                * Renvoie une réponse d'erreur (`400`, `409`, `500`).
    * **Frontend (Angular - `AuthService` / `RegisterComponent`)**
        * Reçoit la réponse du backend.
        * **Si Succès (200/201 avec JWT) :**
            * **Extrait et stocke le JWT** (`localStorage` ou `sessionStorage`).
            * Met à jour l'état d'authentification global (ex: `AuthService.currentUserSubject.next(...)`).
            * **Vérifie l'indicateur `needsStructureSetup` (qui sera `true`).**
            * **Navigue vers l'écran de création de structure (`/create-structure`)**. Il n'est plus nécessaire de
              passer l'`userId` via `history.state`, car l'utilisateur est maintenant authentifié et le backend pourra
              récupérer l'ID depuis le token lors de la prochaine requête.
        * **Si Échec (4xx/5xx) :**
            * Affiche l'erreur. Reste sur l'écran d'inscription.

---

2.  ### **Étape 2 : Création et Liaison de la Structure (Utilisateur déjà connecté)**
    * **Frontend (Angular - `StructureCreationComponent`)**
        * L'utilisateur arrive sur cet écran car il a été redirigé après l'inscription (ou après un login ultérieur si
          `needsStructureSetup` était `true`). Un `AuthGuard` doit protéger cette route pour s'assurer que seul un
          utilisateur authentifié y accède.
        * L'utilisateur remplit le formulaire de structure.
        * Au clic sur "Sauvegarder" :
            * Envoi d'une requête `POST` vers `/api/structures` avec les données de la structure. **L'`HttpInterceptor`
              ajoute automatiquement l'en-tête `Authorization: Bearer `**.
    * **Backend (Spring - Endpoint `/api/structures` - **MODIFIÉ**)
        * **(Dans une transaction `@Transactional`)**
            * Le `JwtRequestFilter` valide le token reçu dans l'en-tête `Authorization`.
            * Le filtre place l'objet `Authentication` dans le `SecurityContextHolder`.
            * Le contrôleur reçoit les données de la structure.
            * **Récupère l'utilisateur authentifié** depuis le `SecurityContextHolder` (ex: via
              `@AuthenticationPrincipal UserDetails userDetails`).
            * Valide les données de la structure.
            * Trouve l'entité `User` correspondante (via l'ID ou l'email de `userDetails`).
            * Crée et sauvegarde l'entité `Structure`.
            * **Met à jour l'entité `User`** en liant la nouvelle `Structure`.
            * Sauvegarde l'entité `User` mise à jour.
            * **(Commit de la transaction)**
            * Renvoie une réponse `200 OK` ou `201 Created`.
    * **Frontend (Angular - `StructureCreationComponent`)**
        * Reçoit la réponse de succès.
        * Affiche un message de succès.
        * **Redirige vers le tableau de bord principal (`/dashboard` ou espace utilisateur)**. Le processus
          d'inscription est terminé.
        * *(Optionnel mais recommandé)* : L'`AuthService` pourrait rafraîchir les informations utilisateur (peut-être
          via un appel à un endpoint `/api/users/me`) pour s'assurer que l'état local (`needsStructureSetup`) est mis à
          jour à `false`, ou attendre la prochaine connexion complète pour obtenir un JWT mis à jour.

3. **Connexion Ultérieure (`POST /api/auth/login`)**
    * Ce flux reste le même que précédemment décrit.
    * **Backend :** Vérifie les identifiants, récupère l'utilisateur, vérifie si `structure_id` est null, génère un JWT
      avec `needsStructureSetup` à `true` ou `false` en conséquence.
    * **Frontend :** Reçoit le JWT et l'indicateur, stocke le token, **redirige vers `/create-structure`
      si `needsStructureSetup` est `true`, sinon vers `/dashboard` (ou l'espace spectateur/utilisateur approprié).**

**Avantages de cette Connexion Automatique :**

* **Expérience Utilisateur Améliorée :** Flux plus fluide, sans interruption par un écran de connexion.
* **Gestion d'État Simplifiée (partiellement) :** Le frontend est immédiatement dans un état "connecté" après
  l'inscription, ce qui peut simplifier la logique de navigation initiale.
* **Cohérence :** L'utilisateur est traité comme "connecté" dès la fin de l'étape 1.

**Inconvénients / Points d'Attention :**

* **Logique Backend `/register` :** L'endpoint d'inscription a désormais une double responsabilité (créer + connecter).
  Il faut bien gérer la transactionnalité.
* **Mise à Jour du JWT/État après Étape 2 :** Le JWT initial contient `needsStructureSetup: true`. Après la création
  réussie de la structure, ce claim devient obsolète. Le frontend doit soit obtenir un nouveau token, soit gérer le fait
  que l'état local peut être légèrement en retard jusqu'à la prochaine connexion complète ou un rafraîchissement manuel
  des données utilisateur. Souvent, naviguer simplement vers le dashboard est acceptable.