<!-- Fiche Memory Bank — point d'entrée : [AI_MEMORY.md](../../AI_MEMORY.md) -->

## Décisions Architecturales
1. **Rôles et Privilèges** :
   - [FAIT] **Hiérarchie des rôles globaux** : Les utilisateurs ont un **`Role`** global (enum : `ADMIN`, `DIRECTION`, `BASIC_USER`) qui détermine leurs **privilèges** dans l'application (enum `Privilege` : `GESTION_UTILISATEURS`, `GESTION_SEJOURS`, `GESTION_SANITAIRE`, `ACCES_SEJOUR`).
   - **Mapping rôles globaux ↔ privilèges** :
     - `ADMIN` : tous les privilèges (GESTION_UTILISATEURS, GESTION_SEJOURS, GESTION_SANITAIRE, ACCES_SEJOUR)
     - `DIRECTION` : GESTION_SEJOURS, GESTION_SANITAIRE, ACCES_SEJOUR
     - `BASIC_USER` : ACCES_SEJOUR uniquement
   - **RoleSejour** (rôle dans un séjour spécifique) : Chaque membre d'équipe a un **`RoleSejour`** (enum : `ANIM`, `AS`, `ADJOINT`, `SB`, `AUTRE`) qui indique sa **fonction** dans ce séjour particulier et qui définit ses **privilèges dans le contexte du séjour**.
   - **Privilèges `RoleSejour`** (indépendants du `Role` global) :
     - `ANIM` (Animateur) → `ACCES_SEJOUR`
     - `AS` (Assistant Sanitaire) → `ACCES_SEJOUR` + `GESTION_SANITAIRE`
     - `ADJOINT` → `GESTION_SEJOURS` + `GESTION_SANITAIRE` + `ACCES_SEJOUR`
     - `SB` (Surveillant de Baignade) → `ACCES_SEJOUR`
     - `AUTRE` → `ACCES_SEJOUR`
   - **Séparation des concepts** : Le `Role` global (stocké dans la table `utilisateur`) contrôle les **droits d'accès aux fonctionnalités globales** de l'application. Le `RoleSejour` (stocké dans la table `sejour_equipe`) définit la **fonction** d'une personne dans un séjour spécifique et ses **privilèges dans le contexte de ce séjour**. Les deux systèmes sont **indépendants** : modifier le `RoleSejour` d'un membre ne modifie **pas** son `Role` global.
  - **Méthodes `RoleSejour`** : `getPrivileges()` retourne les privilèges du rôle séjour, `getAuthorities()` retourne les autorités Spring Security (privilèges + `ROLE_SEJOUR_<nom>`).
  - **Application effective dans Spring Security** : `Utilisateur.getAuthorities()` fusionne les authorities du rôle global **et** celles des `RoleSejour` de `sejoursEquipe` ; les endpoints de gestion “direction de séjour” utilisent `hasAuthority('GESTION_SEJOURS')` (ADJOINT inclus).
  - **Chargement auth** : pour exposer les authorities `RoleSejour` pendant l'authentification, `ApplicationSecurityConfig` charge l'utilisateur via `UtilisateurRepository.findWithSejoursEquipeByEmail/findWithSejoursEquipeByTokenId`.
  - **Garde-fou métier par séjour** : malgré la fusion des authorities, les actions de gestion enfants valident le périmètre séjour via `SejourVerificationService.verifierDroitGestionSejour(sejourId, utilisateurTokenId)` (directeur du séjour, membre d'équipe avec `GESTION_SEJOURS`, ou ADMIN).
   - **Gestion complète vs animateur sur une ressource** : `SejourVerificationService.aDroitGestionCompleteSurSejour(sejourId, utilisateurTokenId)` factorise la condition « peut agir sur tout le séjour comme la direction » (ADMIN, directeur du séjour, ou rôle équipe avec `GESTION_SEJOURS`, ex. ADJOINT). Sert notamment à **`verifierDroitModificationOuSuppressionActivite`** : si faux, un membre d’équipe ne peut **modifier/supprimer une activité** que s’il figure parmi **`activite.membres`** (sinon `AccessDeniedException`). La **création** d’activité exige **`verifierAppartenanceAuSejour`** (pas de création pour un compte sans lien au séjour).
   - **Modification de l'email utilisateur (`PUT /api/v1/utilisateurs`)** : l'email n'est **pas** modifiable en auto-service. Trois chemins dans `UtilisateurServiceImpl` : **`modifUserByAdmin`** (admin, email autorisé pour tous), **`modifUserByDirector`** (directeur modifiant un **`BASIC_USER`** autre que lui, email autorisé), **`modifUserByUser`** (auto-modification ou autres cas, email **interdit** — `AccessDeniedException` si l'email diffère). Routage dans **`UtilisateurController.modifierUtilisateur`** selon le rôle connecté et la cible.
- **Cahier d’infirmerie** : **`verifierDroitModificationEntreeCahierInfirmerie`** et **`verifierDroitSuppressionEntreeCahierInfirmerie`** (même logique) — **gestion complète du séjour** (**`aDroitGestionCompleteSurSejour`**) **ou** **auteur** (`createur.tokenId`) **ou** **soigneur** (`soigneur.tokenId`) ; **ADMIN** sans restriction. Si `createur` est null, **gestion complète** / **soigneur** / **ADMIN**. **Création** : connecté = `createur` ; **`soigneurTokenId`** peut être un **autre** directeur / membre d’équipe. **Contrôleur** : **`DELETE`** en **`ACCES_SEJOUR`** (garde-fou métier dans le service).
  - **Lecture restreinte au séjour (menus, agrégat alimentaire enfants)** : comme pour d’autres lectures sous `/sejours/{id}/...`, les **GET** concernés passent le **`tokenId`** au service et appellent **`verifierAppartenanceAuSejour`**.
  - **Référentiel alimentaire global (lecture large)** : les **GET** sur `/api/v1/references-alimentaires` exigent seulement **`ACCES_SEJOUR`** (catalogue partagé) ; les écritures restent réservées aux rôles globaux ADMIN/DIRECTION.
2. **Injection de Dépendances** :
   - [FAIT] **Constructor Injection** partout — **constructeurs explicites** (pas de Lombok, pas de génération de constructeur par annotation) ✅
   - Exemples : `SecurityConfiguration`, `AuthenticationController`, `JwtAuthenticationFilter`, `ApplicationSecurityConfig`, `AuthenticationServiceImpl`, `RefreshTokenServiceImpl`, tous les `*Controller` et `*ServiceImpl` ci-dessous exposent un constructeur prenant leurs dépendances `final`.
   - `SejourServiceImpl` (8 deps : SejourRepository, UtilisateurRepository, AuthenticationService, RefreshTokenRepository, SejourEquipeRepository, GroupeRepository, ActiviteRepository, **TypeActiviteService**)
   - `EnfantServiceImpl` (7 deps : EnfantRepository, SejourRepository, SejourEnfantRepository, GroupeRepository, DossierEnfantRepository, ReferenceAlimentaireRepository, SejourVerificationService)
   - `GroupeServiceImpl` (5 deps : GroupeRepository, SejourRepository, EnfantRepository, UtilisateurRepository, SejourEnfantRepository)
   - `SejourController` : **`SejourService`** + **`EnfantService`** (`GET .../dossiers-enfants` agrégé sanitaire).
   - `ActiviteController`, `TypeActiviteController`, `MomentController`, `LieuController`, **`ChambreController`**, **`HoraireController`**, **`PlanningGrilleController`**, **`CahierInfirmerieController`** (**`CahierInfirmerieService`** + **`HistoriqueModificationService`**), **`ActivitePrestataireController`**, `UtilisateurController` : **1** ou **2** services injectés selon le contrôleur
   - `UtilisateurServiceImpl` (4 deps)
   - `ActiviteServiceImpl` (8 deps : ActiviteRepository, SejourRepository, UtilisateurRepository, SejourEquipeRepository, GroupeRepository, LieuRepository, **MomentRepository**, **TypeActiviteRepository**) ; `@SuppressWarnings("null")` au niveau classe
   - `TypeActiviteServiceImpl` (3 deps : **TypeActiviteRepository**, **ActiviteRepository**, **SejourRepository**)
   - `MomentServiceImpl` (4 deps : MomentRepository, SejourRepository, **ActiviteRepository**, **ActivitePrestataireRepository**) ; `@SuppressWarnings("null")` au niveau classe
   - **`ActivitePrestataireServiceImpl`** (5 deps : **ActivitePrestataireRepository**, **SejourVerificationService**, **MomentRepository**, **GroupeRepository**, **UtilisateurRepository**) ; `@SuppressWarnings("null")` au niveau classe
   - `LieuServiceImpl` (2 deps : `LieuRepository`, **`SejourVerificationService`**) ; `@SuppressWarnings("null")` au niveau classe
   - **`ChambreServiceImpl`** (3 deps : **`ChambreRepository`**, **`SejourVerificationService`**, **`UtilisateurRepository`**) ; `@SuppressWarnings("null")` au niveau classe
   - **`HoraireServiceImpl`** (2 deps : **`HoraireRepository`**, **`SejourVerificationService`**) ; `@SuppressWarnings("null")` au niveau classe
   - [CIBLE] Conserver **constructor injection** uniquement — STANDARD APPLIQUÉ ✅
3. **Objets de Transfert de Données (DTOs)** :
   - [FAIT] Migration vers **Java Records** complétée ✅
   - Tous les DTOs et Payloads utilisent maintenant des Records Java :
    - `ProfilDto`, `SejourDto` (avec record imbriqué `DirecteurInfos`), `EnfantDto`, `DossierEnfantDto`, **`EnfantDossierSanitaireLigneDto`**, **`GroupeResumeDto`**, `GroupeDto` (avec record imbriqué `ReferentInfos`), **`ChambreDto`** (record imbriqué **`ReferentInfos`**, **`typeChambre`**, **`identifiant`**, **`nom`** nullable), **`MomentDto`** (**`ordre`** inclus), **`HoraireDto`** (`id`, `libelle`, `sejourId`), `ActiviteDto` (record imbriqué `MembreEquipeInfo`, **`MomentDto moment`** avec **`ordre`**, **`LieuDto lieu`** nullable (**`usages`** = **`Set<UsageLieu>`**), **`TypeActiviteDto typeActivite`** avec **`sejourId`** — **renseigné** pour toute activité persistée conforme, `groupeIds`, **`avertissementLieu`**), **`ActivitePrestataireDto`** (**`moments[]`**, **`groupeIds`**, **`nonParticipations`** : **`NonParticipationPrestataireDto`**), **`TypeActiviteDto`** (`id`, `libelle`, `predefini`, **`sejourId`**), **`LieuDto`** (**`partageableEntreAnimateurs`**, **`nombreMaxActivitesSimultanees`**, **`usages`**)
    - `CreateSejourRequest`, `CreateEnfantRequest`, `UpdateDossierEnfantRequest`, `CreateGroupeRequest`, `AjouterReferentRequest`, **`SaveChambreRequest`**, `CreateActiviteRequest`, `UpdateActiviteRequest` (incl. **`typeActiviteId`** **obligatoire** `@NotNull`), **`SaveActivitePrestataireRequest`** (**`momentIds`** `@NotEmpty`, **`nonParticipations?`**), **`SaveMomentRequest`**, **`ReorderMomentsRequest`** (`momentIds`), **`SaveLieuRequest`** (**`usages`** **`@NotEmpty`**, `Set<UsageLieu>`), **`SaveHoraireRequest`** (`libelle`), **`SaveTypeActiviteRequest`** (`libelle`), **`SaveCahierInfirmerieEntreeRequest`**, **`CahierInfirmerieEntreeDto`**, **`HistoriqueModificationCahierInfirmerieDto`**, **`PlanningCellulePayload`** / **`PlanningCelluleDto`** (cellules : listes **`horaireIds`**, **`horaireLibelles`**, **`momentIds`**, **`groupeIds`**, **`lieuIds`**, **`membreTokenIds`** — détail API : [documentation-api-rest.md](./documentation-api-rest.md)), `RegisterRequest`, `AuthenticationRequest`, `UpdateUserRequest`
    - `MembreEquipeRequest` (POST membre **existant** : `tokenId` + `roleSejour`), **`UpdateMembreEquipeRoleRequest`** (PUT **changer le rôle** : `roleSejour` seul — le membre est identifié par l’URL), `ChangePasswordRequest`, `RefreshTokenRequest`
     - `AuthenticationResponse`, `RefreshTokenResponse`
   - Les Records offrent l'immutabilité native et une syntaxe concise, idéale pour Java 21.
   - **Note** : `ErrorResponse` est une **classe Java** avec getters/setters et un **`ErrorResponseBuilder`** statique interne (`ErrorResponse.builder()` … `build()`) — pas de Lombok.
4. **Services** :
   - Utilisation d'interfaces pour les services (`SejourService`, `EnfantService`, `GroupeService`, `ActiviteService`, **`ActivitePrestataireService`**, **`MomentService`**, **`TypeActiviteService`**, `LieuService`, **`ChambreService`**, **`HoraireService`**, **`PlanningGrilleService`**, **`CahierInfirmerieService`**, **`HistoriqueModificationService`**, `UtilisateurService`, `AuthenticationService`) et d'implémentations correspondantes (incl. **`ActivitePrestataireServiceImpl`**, **`MomentServiceImpl`**, **`TypeActiviteServiceImpl`**, **`HoraireServiceImpl`**, **`PlanningGrilleServiceImpl`**, **`CahierInfirmerieServiceImpl`**, **`HistoriqueModificationServiceImpl`**, **`ChambreServiceImpl`**). Méthode **`EnfantService.listerDossiersEnfantsDuSejour`** pour le chargement groupé sanitaire.
5. **API Response** :
   - Standardiser les retours (éviter de renvoyer des entités brutes, toujours des DTOs).
   - **Références `Utilisateur` (JSON, front ↔ back)** : **toujours** le **`tokenId`** (chaîne), **jamais** l’`id` SQL — convention détaillée dans **`.cursorrules`** (*Utilisateurs : identifiant côté API*).
6. **Gestion des Exceptions** :
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
   - **`CustomAccessDeniedHandler`** (package `config/`, implémente `AccessDeniedHandler`) : utilisé par Spring Security sur la **chaîne de filtres**. Corps **403** au format **`ErrorResponse`** (`status`, `error`, `timestamp`, `message`, `path`). La méthode **`resolveMessageForClient(HttpServletRequest, AccessDeniedException)`** : si le message d’exception Spring est **générique** (`Access is denied`, etc.), le **`message`** JSON est **remplacé** par un texte métier (FR) déduit de l’**URI** et de la **méthode** — ex. séjours admin global, référentiel alimentaire global, utilisateurs / recherche équipe, liste séjours par utilisateur, **`PUT` dossier santé**, sinon règle large « gestion séjour » réservée **directeur / adjoint avec droits** ; sinon message générique. Si l’exception porte déjà un message exploitable, il est **conservé**.
   - **`GlobalExceptionHandler`** : **`@ExceptionHandler(AccessDeniedException.class)`** pour les refus de **sécurité méthode** (`@PreAuthorize`, etc.) levés **après** les filtres (non gérés par `AccessDeniedHandler`). Réponse **403** **`ErrorResponse`** en réutilisant **`CustomAccessDeniedHandler.resolveMessageForClient`** pour aligner le comportement avec la chaîne filtres.
  - **Organisation des packages** : `handlers/` regroupe `GlobalExceptionHandler`, `TokenControllerHandler` et `ErrorResponse` ; `exceptions/` regroupe uniquement les classes d'exception métier ; `config/` contient `CustomAccessDeniedHandler` pour la sécurité.
   - **Règle importante** : Les contrôleurs ne doivent **jamais** avoir de `try-catch` qui masquent les exceptions. Toutes les exceptions doivent remonter vers les handlers globaux pour une gestion cohérente et centralisée.
7. **Gestion des Warnings de Null-Safety** :
   - [FAIT] Utilisation de `@SuppressWarnings("null")` pour les cas où le linter ne peut pas garantir la non-nullité à la compilation, mais où nous savons que la valeur ne sera jamais null à l'exécution ✅
   - **Exemple** : `JpaRepository.save()` ne retourne jamais `null` pour une nouvelle entité (retourne toujours l'entité sauvegardée avec son ID généré), mais le linter ne peut pas le garantir statiquement.
   - **Standard appliqué** : Utiliser `@SuppressWarnings("null")` avec un commentaire explicatif quand la garantie de non-nullité est documentée et vérifiée à l'exécution.
   - **Cas résolu** : `EnfantServiceImpl.creerEtAjouterEnfantAuSejour()` ligne 57 - warning de null-safety résolu avec `@SuppressWarnings("null")` car `save()` garantit un retour non-null pour une nouvelle entité.
   - **Tests unitaires** : Pour les classes de test avec nombreux avertissements null-safety (ex: mocks Mockito, `when().thenReturn()`), placer `@SuppressWarnings("null")` au **niveau de la classe** plutôt que sur chaque méthode. Cela évite l'erreur Eclipse 1102 ("compiler option being ignored") tout en supprimant les avertissements.
8. **Activités prestataires (sorties)** :
   - CRUD sous **`/api/v1/sejours/{sejourId}/activites-prestataires`** ; **lecture** **`ACCES_SEJOUR`**, **écriture** **`GESTION_SEJOURS`** (aligné réunions / groupes).
   - **`@ManyToMany`** vers **`Moment`** (min. 1) et **`Groupe`** (optionnel). Pas d’historique de modifications (contrairement aux activités internes).
   - **Calendrier animateurs** : animateurs concernés = **référents** des **`groupeIds`** ; exclusion par **`ActivitePrestataireNonParticipation`** (`tokenId` + `momentId`, contrainte **`uk_ap_non_participation`**). PUT : **`nonParticipations`** fourni = liste complète de remplacement ; omis = conserver + élaguer. Sync incrémentale (réutilise les lignes existantes) pour éviter doublon Hibernate sur **`uk_ap_non_participation`**.
   - **Anti-doublon métier** : une seule sortie par triplet **date + moment + groupe** sur le séjour (**`IllegalArgumentException`** **400**). Conflit sortie vs activité interne : résolu côté front (dialogue direction) + **`nonParticipations`** / DELETE activité interne — pas d’endpoint dédié.
   - Suppression d’un **`Moment`** bloquée aussi si des sorties y sont rattachées (**`ActivitePrestataireRepository.existsByMoments_Id`**).
9. **Chambres (hébergement séjour)** :
   - Entité **`Chambre`** **distincte de `Lieu`** : `Lieu` = activités / surveillance / rassemblements ; `Chambre` = hébergement (enfants ou équipe).
   - **`TypeChambre`** : **`ENFANT`** (référents via **`chambre_referent`**, même pattern que groupes) ; **`EQUIPE`** (pas de référents — **400** si tentative d’ajout ; passage **`ENFANT` → `EQUIPE`** en PUT efface les référents).
   - **`identifiant`** obligatoire, **unique par séjour** (**`uk_chambre_sejour_identifiant`**, casse ignorée) ; **`nom`** optionnel (surnom).
   - CRUD **`/api/v1/sejours/{sejourId}/chambres`** ; **lecture** **`ACCES_SEJOUR`**, **écriture** **`GESTION_SEJOURS`**.
   - **Hors périmètre v1** : affectation des occupants (enfants / membres d’équipe) aux chambres.

