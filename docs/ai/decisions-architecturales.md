<!-- Fiche Memory Bank — point d'entrée : [AI_MEMORY.md](../../AI_MEMORY.md) -->

## Décisions Architecturales
1. **Injection de Dépendances** :
   - [FAIT] **Constructor Injection** partout — **constructeurs explicites** (pas de Lombok, pas de génération de constructeur par annotation) ✅
   - Exemples : `SecurityConfiguration`, `AuthenticationController`, `JwtAuthenticationFilter`, `ApplicationSecurityConfig`, `AuthenticationServiceImpl`, `RefreshTokenServiceImpl`, tous les `*Controller` et `*ServiceImpl` ci-dessous exposent un constructeur prenant leurs dépendances `final`.
   - `SejourServiceImpl` (8 deps : SejourRepository, UtilisateurRepository, AuthenticationService, RefreshTokenRepository, SejourEquipeRepository, GroupeRepository, ActiviteRepository, **TypeActiviteService**)
   - `EnfantServiceImpl` (5 deps : EnfantRepository, SejourRepository, SejourEnfantRepository, GroupeRepository, DossierEnfantRepository)
   - `GroupeServiceImpl` (5 deps : GroupeRepository, SejourRepository, EnfantRepository, UtilisateurRepository, SejourEnfantRepository)
   - `SejourController`, `ActiviteController`, `TypeActiviteController`, `MomentController`, `LieuController`, **`HoraireController`**, **`PlanningGrilleController`**, `UtilisateurController` : **1** service injecté chacun
   - `UtilisateurServiceImpl` (4 deps)
   - `ActiviteServiceImpl` (8 deps : ActiviteRepository, SejourRepository, UtilisateurRepository, SejourEquipeRepository, GroupeRepository, LieuRepository, **MomentRepository**, **TypeActiviteRepository**) ; `@SuppressWarnings("null")` au niveau classe
   - `TypeActiviteServiceImpl` (3 deps : **TypeActiviteRepository**, **ActiviteRepository**, **SejourRepository**)
   - `MomentServiceImpl` (3 deps : MomentRepository, SejourRepository, **ActiviteRepository**) ; `@SuppressWarnings("null")` au niveau classe
   - `LieuServiceImpl` (2 deps : LieuRepository, SejourRepository) ; `@SuppressWarnings("null")` au niveau classe
   - **`HoraireServiceImpl`** (2 deps : **`HoraireRepository`**, **`SejourVerificationService`**) ; `@SuppressWarnings("null")` au niveau classe
   - [CIBLE] Conserver **constructor injection** uniquement — STANDARD APPLIQUÉ ✅
2. **Objets de Transfert de Données (DTOs)** :
   - [FAIT] Migration vers **Java Records** complétée ✅
   - Tous les DTOs et Payloads utilisent maintenant des Records Java :
    - `ProfilDto`, `SejourDto` (avec record imbriqué `DirecteurInfos`), `EnfantDto`, `DossierEnfantDto`, `GroupeDto` (avec record imbriqué `ReferentInfos`), **`MomentDto`** (**`ordre`** inclus), **`HoraireDto`** (`id`, `libelle`, `sejourId`), `ActiviteDto` (record imbriqué `MembreEquipeInfo`, **`MomentDto moment`** avec **`ordre`**, **`LieuDto lieu`** nullable, **`TypeActiviteDto typeActivite`** avec **`sejourId`** — **renseigné** pour toute activité persistée conforme, `groupeIds`, **`avertissementLieu`**), **`TypeActiviteDto`** (`id`, `libelle`, `predefini`, **`sejourId`**), `LieuDto` (**`partageableEntreAnimateurs`**, **`nombreMaxActivitesSimultanees`**)
    - `CreateSejourRequest`, `CreateEnfantRequest`, `UpdateDossierEnfantRequest`, `CreateGroupeRequest`, `AjouterReferentRequest`, `CreateActiviteRequest`, `UpdateActiviteRequest` (incl. **`typeActiviteId`** **obligatoire** `@NotNull`), **`SaveMomentRequest`**, **`ReorderMomentsRequest`** (`momentIds`), `SaveLieuRequest`, **`SaveHoraireRequest`** (`libelle`), **`SaveTypeActiviteRequest`** (`libelle`), **`PlanningCellulePayload`** / **`PlanningCelluleDto`** (cellules : listes **`horaireIds`**, **`horaireLibelles`**, **`momentIds`**, **`groupeIds`**, **`lieuIds`**, **`membreTokenIds`** — détail API : [documentation-api-rest.md](./documentation-api-rest.md)), `RegisterRequest`, `AuthenticationRequest`, `UpdateUserRequest`
    - `MembreEquipeRequest` (POST membre **existant** : `tokenId` + `roleSejour`), **`UpdateMembreEquipeRoleRequest`** (PUT **changer le rôle** : `roleSejour` seul — le membre est identifié par l’URL), `ChangePasswordRequest`, `RefreshTokenRequest`
     - `AuthenticationResponse`, `RefreshTokenResponse`
   - Les Records offrent l'immutabilité native et une syntaxe concise, idéale pour Java 21.
   - **Note** : `ErrorResponse` est une **classe Java** avec getters/setters et un **`ErrorResponseBuilder`** statique interne (`ErrorResponse.builder()` … `build()`) — pas de Lombok.
3. **Services** :
   - Utilisation d'interfaces pour les services (`SejourService`, `EnfantService`, `GroupeService`, `ActiviteService`, **`MomentService`**, **`TypeActiviteService`**, `LieuService`, **`HoraireService`**, **`PlanningGrilleService`**, `UtilisateurService`, `AuthenticationService`) et d'implémentations correspondantes (incl. **`MomentServiceImpl`**, **`TypeActiviteServiceImpl`**, **`HoraireServiceImpl`**, **`PlanningGrilleServiceImpl`**).
4. **API Response** :
   - Standardiser les retours (éviter de renvoyer des entités brutes, toujours des DTOs).
   - **Références `Utilisateur` (JSON, front ↔ back)** : **toujours** le **`tokenId`** (chaîne), **jamais** l’`id` SQL — convention détaillée dans **`.cursorrules`** (*Utilisateurs : identifiant côté API*).
5. **Gestion des Exceptions** :
   - Exceptions personnalisées : `ResourceNotFoundException`, `ResourceAlreadyExistsException`, `EmailDejaUtiliseException`, `UtilisateurException`, `TokenException`, **`ConflitPlanningAnimateurException`** (planning activité : animateur déjà pris sur le même séjour / jour / moment ; corps JSON **400** avec **`code`** = **`ANIMATEUR_DEJA_AFFECTE_CRENEAU`**, **`message`** + **`error`**).
   - **Mécanisme de gestion globale** : Utilisation de `@ControllerAdvice` pour centraliser la gestion des exceptions.
   - **`GlobalExceptionHandler`** (`@ControllerAdvice`) gère automatiquement toutes les exceptions non gérées lancées dans les contrôleurs :
     - Spring détecte automatiquement les exceptions non capturées
     - Cherche le `@ExceptionHandler` correspondant au type d'exception
     - Exécute la méthode handler appropriée
     - Retourne une `ResponseEntity` avec le code HTTP et le format d'erreur appropriés
   - **Mapping des exceptions** :
     - `MethodArgumentNotValidException` → 400 (validation automatique avec `@Valid`)
     - `ResourceAlreadyExistsException` → 409 (conflit)
     - `EmailDejaUtiliseException` → 409 (conflit)
     - `UtilisateurException` → 400 (erreur utilisateur)
     - `ResourceNotFoundException` → 404 (ressource non trouvée)
     - **`ConflitPlanningAnimateurException`** → **400** (JSON **`code`** + **`message`** + **`error`**)
     - `RuntimeException` → 404 (par défaut, handler générique)
     - `IllegalArgumentException` → 400 (argument invalide)
   - **`TokenControllerHandler`** (`@RestControllerAdvice`, package `handlers/`) gère spécifiquement les `TokenException` avec un format d'erreur structuré (`ErrorResponse`) - retourne HTTP 403 Forbidden.
   - **`CustomAccessDeniedHandler`** (package `config/`, implémente `AccessDeniedHandler`) : géré par Spring Security pour les `AccessDeniedException` (ex. : accès refusé aux endpoints dossier enfant si l'utilisateur ne participe pas au séjour). Retourne HTTP 403 Forbidden avec `ErrorResponse` structuré (status, error, timestamp, message, path).
  - **Organisation des packages** : `handlers/` regroupe `GlobalExceptionHandler`, `TokenControllerHandler` et `ErrorResponse` ; `exceptions/` regroupe uniquement les classes d'exception métier ; `config/` contient `CustomAccessDeniedHandler` pour la sécurité.
   - **Règle importante** : Les contrôleurs ne doivent **jamais** avoir de `try-catch` qui masquent les exceptions. Toutes les exceptions doivent remonter vers les handlers globaux pour une gestion cohérente et centralisée.
6. **Gestion des Warnings de Null-Safety** :
   - [FAIT] Utilisation de `@SuppressWarnings("null")` pour les cas où le linter ne peut pas garantir la non-nullité à la compilation, mais où nous savons que la valeur ne sera jamais null à l'exécution ✅
   - **Exemple** : `JpaRepository.save()` ne retourne jamais `null` pour une nouvelle entité (retourne toujours l'entité sauvegardée avec son ID généré), mais le linter ne peut pas le garantir statiquement.
   - **Standard appliqué** : Utiliser `@SuppressWarnings("null")` avec un commentaire explicatif quand la garantie de non-nullité est documentée et vérifiée à l'exécution.
   - **Cas résolu** : `EnfantServiceImpl.creerEtAjouterEnfantAuSejour()` ligne 57 - warning de null-safety résolu avec `@SuppressWarnings("null")` car `save()` garantit un retour non-null pour une nouvelle entité.
   - **Tests unitaires** : Pour les classes de test avec nombreux avertissements null-safety (ex: mocks Mockito, `when().thenReturn()`), placer `@SuppressWarnings("null")` au **niveau de la classe** plutôt que sur chaque méthode. Cela évite l'erreur Eclipse 1102 ("compiler option being ignored") tout en supprimant les avertissements.

