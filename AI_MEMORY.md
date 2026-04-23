# AI MEMORY BANK

## Contexte Global
- **Langage** : Java 21
- **Framework** : Spring Boot 3.5.6
- **Build Tool** : Maven
- **Packaging** : WAR (`<packaging>war</packaging>`)
- **Base de Données** : MySQL
- **Sécurité** : Spring Security avec JWT (jjwt)
- **Outils** : Jakarta Validation, Java Records — **pas de Lombok** dans le dépôt (`pom.xml` sans dépendance Lombok ; aucune annotation Lombok dans `src/`). Entités / services / contrôleurs : **constructeurs explicites** et POJO JPA classiques où pertinent (**`Lieu`**, **`Moment`**, **`Horaire`**, etc.).
- **Build** : `pom.xml` — Spring Boot parent **3.5.6**, `maven-compiler-plugin` hérité du parent (Java 21), **sans** `annotationProcessorPaths` Lombok.
- **Utilitaires** : `ExcelHelper` (import Excel), `DateFormatHelper` (`formatDdMmYyyy(LocalDate)` pour messages utilisateur en `dd/MM/yyyy`, refuse `null`)
- **Tests** : JUnit 5, Mockito

## Architecture
- **Pattern** : Layered Architecture (Controller -> Service Interface -> Service Impl -> Repository)
- **Data Flow** : 
  - `Controller` reçoit `RequestPayload` / renvoie `DTO` ou `ResponseEntity`
  - `Service` manipule `Entity` et convertit en `DTO`
  - `Repository` (JPA) gère la persistance
- **Gestion des Erreurs** : Centralisée via `@ControllerAdvice` (`GlobalExceptionHandler` dans `handlers/`)
- **Structure des packages** : `exceptions/` = classes d'exception lancées (`throw`), `handlers/` = handlers qui attrapent et formatent les réponses d'erreur HTTP

## Contexte Actif
- **Phase** : Refactoring Architectural & Tests
- **Focus Actuel** : Migration vers les standards de code (Constructor Injection ✅, Java Records ✅) et ajout de tests unitaires ; compléter la couverture (dossier enfant, activités, **moments** ; **`TypeActiviteControllerTest`** si besoin).
- **Dernière mise à jour** : 2026-04-23
- **Dernières modifications** :
  - **Contrat API `Utilisateur` (JSON)** : toute **référence utilisateur** front ↔ back = **`tokenId`**, jamais l’`id` SQL — archivé en **`.cursorrules`** (*Utilisateurs : identifiant côté API*) et **§4 Décisions architecturales** ci-dessous.
  - **Plannings direction (grille générique)** : Entités **`PlanningGrille`** (séjour, titre, consigne globale, **`miseAJour`**, **`sourceLibelleLignes`** / **`sourceContenuCellules`**), **`PlanningLigne`** (**`PlanningLigneLibelleSource`** : libellé = saisie libre ou dérivé d’un **`Groupe`** / **`Lieu`** / **`Horaire`** / **`Moment`** / membre d’équipe via **`Utilisateur.tokenId`** (jointure **`libelle_utilisateur_token_id` → `utilisateur(token_id)`**) si `MEMBRE_EQUIPE` pour **`sourceLibelleLignes`** ; **`libelleRegroupement`** ; **`ordre`**), **`PlanningCellule`** (**`LocalDate`**, **`ManyToMany`** animateurs — table **`planning_cellule_utilisateur`**, colonne **`utilisateur_token_id` → `utilisateur(token_id)`** (aligné **`membreTokenIds`**), pas **`utilisateur_id`**), références **`Horaire`** / **`Moment`** / **`Groupe`** / **`Lieu`**, **`texteLibre`**. API **`/api/v1/sejours/{sejourId}/planning-grilles`**. Doc : **`docs/planning-grilles-api.md`**.
  - **Entité `Horaire` & CRUD (direction)** : Libellés du type **`6h00`**, **`7h15`**, **`18h30`** (pattern **`Horaire.LIBELLE_HORAIRE_PATTERN`** : heures **0–23**, minutes **00–59**). Entité **`Horaire`** : **`libelle`**, **`ManyToOne`** obligatoire vers **`Sejour`** ; **`Sejour.horaires`** en **`OneToMany`** cascade + orphanRemoval (constructeur / builder **`Sejour`** incluent **`List<Horaire> horaires`**). Contrainte BDD **`uk_horaire_sejour_libelle`** **`(sejour_id, libelle)`** ; côté service, unicité **insensible à la casse** (**`existsBySejourIdAndLibelleIgnoreCase`**, **`...AndIdNot`**) → **`ResourceAlreadyExistsException`** **409**. **`HoraireRepository`** : **`findBySejourIdOrderByIdAsc`**, **`findByIdAndSejourId`**, méthodes **`exists*`** ; pas de référence depuis **`Activite`** (suppression sans garde-fou métier). API **`GET/POST/PUT/DELETE /api/v1/sejours/{sejourId}/horaires`** et **`GET .../horaires/{horaireId}`** — **`@PreAuthorize("hasRole('DIRECTION')")`**. **`SaveHoraireRequest`** (`libelle`), **`HoraireDto`** (`id`, `libelle`, `sejourId`). **`HoraireService`** / **`HoraireServiceImpl`** (**`HoraireRepository`**, **`SejourVerificationService`**), **`@SuppressWarnings("null")`** au niveau classe. Tests : **`HoraireControllerTest`** (**7**), **`HoraireServiceImplTest`** (**7**).
  - **Activité : unicité animateur par créneau (séjour + jour + moment)** : Un même membre d’équipe ne peut **pas** être planifié sur **deux activités** pour le **même séjour**, la **même `LocalDate`** et le **même `Moment`**. Vérification dans **`ActiviteServiceImpl.verifierMembresDisponiblesPourCreneau`** après résolution de l’équipe ; requête **`ActiviteRepository.countActivitesAvecMembreMemeCreneau(sejourId, date, momentId, utilisateurId, excludeActiviteId)`** (JPQL sur la jointure **`membres`**, `excludeActiviteId` = id de l’activité exclue en **PUT**, `null` en **POST**). En conflit : exception **`ConflitPlanningAnimateurException`** (pas **`IllegalArgumentException`**). **Code API stable** (ASCII) : **`ConflitPlanningAnimateurException.CODE`** = **`ANIMATEUR_DEJA_AFFECTE_CRENEAU`**. **`GlobalExceptionHandler`** : handler dédié → HTTP **400**, corps JSON **`code`**, **`message`**, **`error`** (même texte que `message` pour rétrocompatibilité). **Message utilisateur** (prénom, sinon nom si prénom vide) : *« {Prénom} encadre déjà une autre activité le {dd/MM/yyyy} au moment "{nom du moment}". »* — **`DateFormatHelper.formatDdMmYyyy`**. *Tests* : scénarios conflit création / modification + exclusion de l’activité courante en **`ActiviteServiceImplTest`**.
  - **Audit codebase (mémo)** : Projet **sans Lombok** ; `ErrorResponse` = classe + **`ErrorResponseBuilder`** interne manuel ; injection par **constructeurs explicites** partout. Comptage tests à jour : **`SejourServiceImplTest` ~35**, **`UtilisateurServiceImplTest` ~32**, **`EnfantServiceImplTest` ~23**, **`TypeActiviteServiceImplTest` ~6**. Contrôleurs testés : **`HoraireControllerTest`** (**7**) ; pas de **`MomentControllerTest`** ni **`TypeActiviteControllerTest`** (autres contrôleurs : voir puce « Mémo projet (audit) »).
  - **Activité : type d’activité obligatoire** — `Activite.typeActivite` en **`@ManyToOne(optional = false)`**, **`type_activite_id` NOT NULL** ; **`CreateActiviteRequest` / `UpdateActiviteRequest`** : **`typeActiviteId`** **`@NotNull`** ; **`ActiviteDto.typeActivite`** attendu pour toute activité en base cohérente ; **migration** : backfill des lignes sans type avant contrainte (voir puce « Migration BDD » ci-dessous + § endpoints activités).
  - **Types d’activité (`TypeActivite`) — par séjour** : Entité JPA **`TypeActivite`** rattachée à un **`Sejour`** (`ManyToOne` obligatoire, `sejour_id`). Contrainte **`uk_type_activite_sejour_libelle`** : **unicité du libellé par séjour** (insensible à la casse côté service) ; le même libellé peut exister sur **deux séjours différents**. Champ **`predefini`** : `true` pour les six types livrés par défaut (**Sport, Manuel, Expression, Jeux, Cuisine, Musique**, liste **`TypeActiviteLibellesParDefaut`**) — **non modifiables / non supprimables** via l’API ; `false` pour les types créés par **`POST .../types-activite`**. Méthode **`TypeActiviteService.assurerTypesParDefautPourSejour(sejourId)`** : idempotent (crée les manquants, corrige **`predefini`** sur les libellés connus déjà présents). Appelée depuis **`SejourServiceImpl.creerSejour`** après sauvegarde du séjour, et depuis **`TypeActiviteInitializer`** au démarrage pour **chaque** séjour en base (`SejourRepository.findAll()`). **`TypeActiviteRepository`** : **`findBySejourIdOrderByLibelleAsc`**, **`findByIdAndSejourId`**, **`findBySejourIdAndLibelleIgnoreCase`**, **`existsBySejourIdAndLibelleIgnoreCase`**, **`existsBySejourIdAndLibelleIgnoreCaseAndIdNot`**. **`ActiviteServiceImpl`** résout **`typeActiviteId`** avec **`findByIdAndSejourId`** (type doit appartenir au **même** séjour que l’activité) ; **`typeActiviteId`** est **obligatoire** sur création / mise à jour d’activité (**`@NotNull`** + **`type_activite_id` NOT NULL** en entité). API **`/api/v1/sejours/{sejourId}/types-activite`** (comme lieux / moments). **`TypeActiviteDto`** : `id`, `libelle`, `predefini`, **`sejourId`**. **`SejourServiceImpl`** : **8** champs (incl. **`TypeActiviteService`**, **`ActiviteRepository`**). **`TypeActiviteServiceImpl`** : **`TypeActiviteRepository`**, **`ActiviteRepository`**, **`SejourRepository`**. *Tests* : **`TypeActiviteServiceImplTest`** (mockito, scénarios création / verrou prédéfini / **`assurerTypesParDefaut`**) ; **`SejourServiceImplTest`** vérifie **`assurerTypesParDefautPourSejour`** après **`creerSejour`** ; pas de **`TypeActiviteControllerTest`** pour l’instant. *Migration BDD* : passage d’un référentiel global à **`sejour_id` NOT NULL** peut exiger nettoyage d’anciennes lignes / index ; voir déploiement. *Frontend* : URLs avec **`sejourId`**, **`TypeActiviteDto.sejourId`**, services CRUD sous le séjour.
  - **Entité `Moment` & CRUD (direction)** : Créneaux du type « Matin », « Après-midi », etc., **un par séjour** (`ManyToOne` `Sejour`), **unicité du nom par séjour** (contrainte `uk_moment_sejour_nom`, doublon ignorant la casse côté service → `ResourceAlreadyExistsException` 409). Champ **`ordre`** (`Integer`, nullable en BDD pour lignes historiques) : **tri d’affichage chronologique** `ORDER BY COALESCE(ordre, id), id` via **`MomentRepository.findBySejourIdOrderChronologique`**. **Création** : `ordre` = max sur le séjour de `ordre` s’il est renseigné sinon `id`, puis **+1** (premier moment → **0**). **Réordonnancement** : **`PUT .../moments/reorder`**, body **`ReorderMomentsRequest`** (`momentIds` : liste **complète** des ids du séjour dans le **nouvel ordre** ; positions persistées **0 … n−1**) ; incohérences / doublons → **`IllegalArgumentException`** (**400**). DTO **`MomentDto`** : `id`, `nom`, `sejourId`, **`ordre`** (fallback affichage : `id` si `ordre` null). `Sejour.moments` en `OneToMany` cascade + orphanRemoval. API **`/api/v1/sejours/{sejourId}/moments`** — `@PreAuthorize("hasRole('DIRECTION')")`. Payloads : **`SaveMomentRequest`** (`nom` seul — pas d’`ordre` en création/édition nom), **`ReorderMomentsRequest`**. **`MomentRepository`** : `countBySejourId`, **`findBySejourIdOrderChronologique`**, `findByIdAndSejourId`, `existsBySejourIdAndNomIgnoreCase*`. Suppression refusée si **`ActiviteRepository.existsByMomentId`** → **400** (`IllegalArgumentException`). *Migration* : colonne `ordre` ajoutée par **`ddl-auto: update`** ; les lignes existantes peuvent avoir `ordre` null jusqu’à un appel **`PUT .../moments/reorder`** ou rester triées comme avant via `COALESCE(ordre, id)`.
  - **Activité ↔ Moment (obligatoire)** : `Activite.moment` **`@ManyToOne` obligatoire** (`moment_id`). **`CreateActiviteRequest` / `UpdateActiviteRequest`** : **`momentId`** (`Integer`, validé dans le service : si `countBySejourId == 0` → **400** avec message demandant à la **direction** de créer des moments ; si moments existent et `momentId == null` → **400** moment obligatoire ; sinon résolution via `MomentRepository.findByIdAndSejourId`). **`ActiviteDto`** inclut **`MomentDto moment`** (non null en sortie).
  - **Activité ↔ type d’activité (obligatoire)** : `Activite.typeActivite` **`@ManyToOne(optional = false)`**, jointure **`type_activite_id` NOT NULL**. **`CreateActiviteRequest` / `UpdateActiviteRequest`** : **`typeActiviteId`** avec **`@NotNull`** (message « Le type d’activité est obligatoire ») ; résolution via **`TypeActiviteRepository.findByIdAndSejourId`** ; si `null` malgré tout côté service → **`IllegalArgumentException`** (**400**) ; type inconnu ou pas du séjour → **404**. DTO : **`ActiviteDto.typeActivite`** (`TypeActiviteDto`) **toujours renseigné** pour une activité cohérente ; mapping **`typeActiviteVersDto`** via **`Objects.requireNonNull`**.
  - **Lieu : partage — jour + moment** : Les comptages d’occupation pour le partage utilisent **même séjour, même lieu, même `LocalDate`, même `moment`** : **`ActiviteRepository.countBySejour_IdAndLieu_IdAndDateAndMoment_Id`** et **`...AndMoment_IdAndIdNot`** (exclusion de l’activité en cours de mise à jour). Les messages (conflit, limite, **`avertissementLieu`**) mentionnent le **nom du moment**. *Ancienne logique « même jour seulement » remplacée.*
  - **Migration BDD** : Ajout de **`moment_id` NOT NULL** sur `activite` : avec données existantes, risque d’échec `ddl-auto` ; prévoir création de moments + **backfill** des activités ou migration SQL avant contrainte stricte. Idem pour **`type_activite_id` NOT NULL** : toute activité encore sans type doit être **mise à jour** (type valide du séjour) ou **supprimée** avant d’appliquer la contrainte, sinon échec `ddl-auto` / migration.
  - **Lieu : partage entre activités / animateurs** : Champs **`partageableEntreAnimateurs`** (`boolean`, défaut `false`) et **`nombreMaxActivitesSimultanees`** (`Integer`, `null` si non partageable). Le **plafond** s’applique au nombre d’activités sur **ce lieu, ce jour calendaire et ce moment** (voir comptages activités). Si partage activé : `nombreMaxActivitesSimultanees` **obligatoire** et **≥ 2** ; sinon **`IllegalArgumentException`** (400). Si partage désactivé : `nombreMaxActivitesSimultanees` doit rester **`null`**. Règles dans `LieuServiceImpl.validerParametresPartage`. `SaveLieuRequest` / `LieuDto` exposent ces champs.
  - **Activité : occupation d’un lieu (jour + moment)** : À la **création / mise à jour**, si `lieuId` est renseigné, `ActiviteServiceImpl` compte les **autres** activités avec **même lieu**, **même date** et **même moment** (méthodes `countBySejour_IdAndLieu_IdAndDateAndMoment_Id` / `...AndIdNot`). Comportement partage / limite / **`avertissementLieu`** inchangé par rapport à la doc historique, mais le **créneau (`Moment`)** est pris en compte. **`IllegalStateException`** si lieu partageable sans max.
  - **Activité ↔ Lieu / Moment / type (rappel)** : `Activite` **`@ManyToOne` optionnel** vers `Lieu` ; **`@ManyToOne` obligatoire** vers **`Moment`** et vers **`TypeActivite`**. Requêtes : **`lieuId`** optionnel, **`momentId`** requis dès qu’il existe au moins un moment pour le séjour, **`typeActiviteId`** toujours requis. `ActiviteDto` : **`moment`**, **`typeActivite`** (non null pour données cohérentes), **`lieu`**, **`avertissementLieu`**. **Frontend** : à aligner (`api.d.ts`) — activ avec **`moment` / `momentId`**, **`typeActivite` / `typeActiviteId`** en création / mise à jour.
  - **Lieu & unicité du nom** : Entité `Lieu` en **POJO JPA** (constructeur vide, getters/setters). `LieuServiceImpl` et `LieuController` : **constructeurs explicites** (alignés avec le reste du projet sans Lombok). Création : `new Lieu()` + setters. Unicité du **nom par séjour** (insensible à la casse) via `LieuRepository` : `existsBySejourIdAndNomIgnoreCase`, `existsBySejourIdAndNomIgnoreCaseAndIdNot` ; doublon → `ResourceAlreadyExistsException` (409). Nom normalisé (`trim` ; chaîne vide si `null`). Tests : `LieuServiceImplTest` (**8** tests : incl. validation partage sans max, partage avec max 2 OK, + les scénarios historiques doublon / trim / modifier / lister).
  - **Entité Lieu & API** : Lieux rattachés à un **seul** séjour : `nom`, `EmplacementLieu`, **`nombreMax`** optionnel (capacité personnes — `@Positive` si présent), **`partageableEntreAnimateurs`**, **`nombreMaxActivitesSimultanees`** (max d’activités **le même jour et le même moment** sur ce lieu, inclusif ; voir comptages `ActiviteRepository` ci-dessus). `ManyToOne` vers `Sejour` ; `Sejour.lieux` `OneToMany` cascade + orphanRemoval. `LieuRepository` : `findBySejour`, `findBySejourId`, **`findByIdAndSejourId`**, `existsBy*`. CRUD `LieuService` / `LieuController`, `@PreAuthorize("hasRole('DIRECTION')")`. **`SaveLieuRequest`** / **`LieuDto`** : `nom`, `emplacement`, `nombreMax`, `partageableEntreAnimateurs`, `nombreMaxActivitesSimultanees`. Tests : `LieuControllerTest` (**7**).
  - **Mémo projet (audit)** : `EnfantControllerTest` **19**, `EnfantServiceImplTest` **~23**, `ExcelHelperTest` **35**, `LieuControllerTest` **7**, `LieuServiceImplTest` **8**, `HoraireControllerTest` **7**, `HoraireServiceImplTest` **7**, **`PlanningGrilleControllerTest`**, **`PlanningGrilleServiceImplTest`**, `ActiviteServiceImplTest` **22** (incl. conflit animateur créneau), **`TypeActiviteServiceImplTest` ~6**, **`SejourServiceImplTest` ~35**, **`UtilisateurServiceImplTest` ~32** (autres : `SejourControllerTest`, `GroupeControllerTest`, `AuthenticationControllerTest`, `GlobalExceptionHandlerTest`, `JwtAuthenticationFilterTest`, `TokenControllerHandlerTest`, etc.). `ActiviteServiceImpl` : **8** dépendances (incl. **`MomentRepository`**, **`TypeActiviteRepository`**, résolution **`findByIdAndSejourId`** pour le type), `@SuppressWarnings("null")` classe. **`TypeActiviteServiceImpl`** : **`TypeActiviteRepository`**, **`ActiviteRepository`**, **`SejourRepository`**. **`SejourServiceImpl`** : incl. **`TypeActiviteService`** (bootstrap à la création de séjour). Pas de **`MomentControllerTest`** / **`TypeActiviteControllerTest`** pour l’instant. Packaging WAR en tête de fiche.
  - **Entité Activité & API** (`Activite`) : `LocalDate` date, nom, description, `Sejour`, **moment obligatoire**, lieu optionnel, **`typeActivite` obligatoire** (`TypeActivite` **du même séjour**, `type_activite_id` NOT NULL), `@ManyToMany` animateurs (`activite_membre_equipe`), groupes (`activite_groupe`). **`lieuId`**, **`momentId`**, **`typeActiviteId`** (**obligatoire**, id d’un **`TypeActivite`** du séjour), `membreTokenIds`, **`groupeIds`**. Date dans `dateDebut`–`dateFin` du séjour ; directeur du séjour autorisé hors `SejourEquipe`. `ActiviteRepository` : **`countBySejour_IdAndLieu_IdAndDateAndMoment_Id`**, **`countBySejour_IdAndLieu_IdAndDateAndMoment_IdAndIdNot`**, **`countActivitesAvecMembreMemeCreneau`**, **`existsByMomentId`**, **`countByTypeActivite_Id`**. CRUD `api/v1/sejours/{sejourId}/activites`. Tests : `ActiviteServiceImplTest` (**22**), `ActiviteControllerTest` (2).
  - **supprimerMembreEquipe - Retrait des référents** : Lorsqu'un membre est retiré de l'équipe d'un séjour (`DELETE /sejours/{id}/equipe/{membreTokenId}`), il est désormais automatiquement retiré de tous les groupes du séjour où il était référent. `SejourServiceImpl.supprimerMembreEquipe()` utilise `GroupeRepository.findBySejourId()` puis retire le membre de chaque groupe via `groupe.getReferents().removeIf()`. La suppression d'un utilisateur (`supprimerUtilisateur`) gère déjà la table `groupe_referent` via JPA (cascade implicite sur FK).
  - **ExcelImportSpec - Logique groupes (ET/OU)** : La détection des colonnes Excel utilise désormais une logique par groupes : chaque colonne a des groupes de mots-clés ; l'en-tête doit contenir **tous** les groupes (ET entre groupes), avec au moins un mot par groupe (OU dans un groupe). Ex : `emailParent1` = (email OU mail) ET parent ET 1. Structure `ColumnDef` avec `groupesMotsCles` (String[][]). `ExcelHelper.containsAllGroups()` pour la vérification. Colonnes parent 1/2 (email, téléphone) et traitements (matin, midi, soir, si besoin) utilisent cette logique pour éviter les ambiguïtés. `getColumnMappings()` retourne `Map<String, String[][]>`.
  - **Correction ExcelHelper DataFormatter (locale)** : Le `DataFormatter` d'Apache POI utilise par défaut la locale système. Sur une machine française, le séparateur décimal est la virgule (3,14) au lieu du point (3.14), ce qui faisait échouer le test `shouldExtractNumericDecimal`. Correction : `new DataFormatter(Locale.US)` pour garantir un format décimal cohérent (point) indépendamment de la machine.
  - **ExcelImportSpec et notice d'import Excel** : Classe `ExcelImportSpec` (`excel/`) centralisant la spécification des colonnes pour l'import Excel des enfants. Source unique de vérité : toute modification des colonnes ou de leurs noms se fait dans `ExcelImportSpec` et s'applique automatiquement à l'import et à l'API. Structure `ColumnDef` avec `groupesMotsCles` (String[][]) : chaque groupe = alternatives (OU), tous les groupes doivent matcher (ET). Ex : `emailParent1` = [["email","mail"], ["parent"], ["1"]]. DTOs `ExcelImportColumnSpec` (champ, libelle, motsCles, obligatoire) et `ExcelImportSpecResponse` (colonnesObligatoires, colonnesOptionnelles, formatsAcceptes). Endpoint `GET /api/v1/sejours/{sejourId}/enfants/import/spec`. `ExcelHelper.detectColumns()` accepte `Map<String, String[][]>` et utilise `containsAllGroups()`. 
  - **Endpoint PUT dossier enfant** : Nouvel endpoint `PUT /api/v1/sejours/{id}/enfants/{enfantId}/dossier` pour modifier le dossier d'un enfant. `UpdateDossierEnfantRequest` (record) avec tous les champs optionnels du dossier. Vérification d'accès : directeur du séjour ou membre de l'équipe (sinon `AccessDeniedException` 403). `getDossierEnfant` et `modifierDossierEnfant` dans `EnfantService`/`EnfantServiceImpl`.
  - **Import Excel DossierEnfant (correction)** : Lors de l'import Excel, si un enfant existant possède déjà un `DossierEnfant`, on met à jour le dossier existant au lieu d'en créer un nouveau. Cela évite la violation de contrainte unique sur `enfant_id` (erreur SQL 1062 "Duplicate entry for key UKok865k0nbygu5b0xendcnl997"). Méthode `populateDossierFromExcelRow()` extraite pour centraliser le remplissage des champs optionnels du dossier depuis les colonnes Excel. Utilisation de `dossierEnfantRepository.findByEnfantId()` pour récupérer le dossier existant avant mise à jour.
  - **Entité Groupe** : Nouvelle entité `Groupe` liée à `Sejour` (ManyToOne). Trois types : `THEMATIQUE` (enfants manuels), `AGE` (tranche ageMin-ageMax, ajout auto), `NIVEAU_SCOLAIRE` (tranche niveauScolaireMin-Max, ajout auto). Relations `@ManyToMany` avec `Enfant` (table `groupe_enfant`) et `Utilisateur` (table `groupe_referent`) — migration depuis entités de jointure explicites (GroupeEnfant, GroupeReferent supprimées). `GroupeService`/`GroupeServiceImpl`, `GroupeController` (CRUD + ajout/retrait enfants et référents). `GroupeDto`, `CreateGroupeRequest`, `AjouterReferentRequest`. Conversion `Date` → `LocalDate` via `toLocalDate()` (évite `UnsupportedOperationException` sur `java.sql.Date.toInstant()`). Documentation frontend : `docs/frontend-creation-groupes.md`.
  - **Entité DossierEnfant** : Nouvelle entité `DossierEnfant` en relation OneToOne avec `Enfant` pour stocker les informations de dossier (contacts parents : emailParent1/2, telephoneParent1/2 ; infos médicales, PAI, alimentaires ; traitements : matin, midi, soir, si besoin ; autres infos, à prendre en sortie). `DossierEnfantRepository` avec `findByEnfantId()`. Création automatique du dossier lors de `creerEtAjouterEnfantAuSejour()` (dossier vide par défaut) et lors de l'import Excel (colonnes optionnelles). Lors de l'import Excel, si l'enfant existe déjà et qu'il a un dossier, on met à jour le dossier existant au lieu d'en créer un nouveau. `DossierEnfantDto` (record) pour les réponses. `ExcelHelper.normalizePhone()` pour normaliser les numéros de téléphone. `EnfantServiceImpl` injecte `DossierEnfantRepository`.
  - **Restructuration handlers/exceptions** : `GlobalExceptionHandler` déplacé de `exceptions/` vers `handlers/` pour une séparation cohérente : `exceptions/` contient les classes d'exception métier (`ResourceNotFoundException`, `TokenException`, etc.), `handlers/` contient les ExceptionHandler (`GlobalExceptionHandler`, `TokenControllerHandler`) et `ErrorResponse`. Imports mis à jour dans tous les tests.
  - **Utilisation de l'enum Genre dans Utilisateur** : L'entité `Utilisateur` utilise maintenant `Genre` (enum) au lieu de `String` pour le champ genre, aligné avec `Enfant`. Méthode statique `Genre.parseGenre(String)` ajoutée pour parser les valeurs API ("Masculin", "Féminin", "Garçon", "Fille", "Homme", "Femme", "M", "F"). `ProfilDto` utilise aussi `Genre`. `RegisterRequest` et `UpdateUserRequest` gardent `String` pour l'API (parsing dans les services). `EnfantServiceImpl` utilise désormais `Genre.parseGenre()` au lieu de sa méthode privée.
  - **Correction ExcelHelperTest** : Suppression de `@SuppressWarnings("null")` (causait l'erreur 1102 "compiler option being ignored"). Correction du test `shouldHandleKeywordsWithAccents` : remplacement des assertions sujettes à des problèmes d'encodage Unicode par des scénarios concrets et fiables (`prenomenfant` avec mot-clé `prenom` ou `pr\u00e9nom`).
  - Simplification de l'entité `Enfant` : suppression des champs des parents (nomParent1, prenomParent1, telephoneParent1, emailParent1, nomParent2, prenomParent2, telephoneParent2, emailParent2). L'entité contient maintenant uniquement les informations personnelles de l'enfant (nom, prénom, genre, dateNaissance, niveauScolaire).
  - Mise à jour de `CreateEnfantRequest`, `EnfantDto` et `EnfantServiceImpl` pour refléter cette simplification.
  - Résolution du warning de null-safety dans `EnfantServiceImpl` (ligne 57) avec `@SuppressWarnings("null")`
  - Correction du warning dans `EnfantServiceImpl.importerEnfantsDepuisExcel()` : suppression de la variable `sejour` non utilisée (ligne 161). La vérification de l'existence du séjour est déjà effectuée dans `creerEtAjouterEnfantAuSejour()` qui est appelée pour chaque ligne du fichier Excel.
  - **Gestion intelligente des enfants** : Implémentation d'une logique de vérification d'existence d'enfants basée sur nom, prénom, genre et date de naissance. Lors de la création, si un enfant existe déjà en BDD, il est réutilisé. Lors de la modification, si les nouvelles informations correspondent à un autre enfant existant, remplacement de la relation SejourEnfant.
  - **Import Excel amélioré** : Détection flexible des colonnes (recherche de mots-clés dans les noms de colonnes), gestion des lignes vides, support de "fille"/"garçon" en plus de "Masculin"/"Féminin" pour le genre.
  - **Configuration des enums** : Passage de `Genre` et `NiveauScolaire` en `@Enumerated(EnumType.STRING)` pour un stockage lisible en base de données ("Masculin", "Féminin", "PS", "MS", etc. au lieu de 0, 1, 2...).
  - **Messages d'erreur améliorés** : Formatage des dates dans les messages d'erreur (format dd/MM/yyyy) et messages plus explicites pour la structure attendue des fichiers Excel.
  - **Correction du builder SejourEnfant** : Le builder initialise maintenant correctement l'ID avec les valeurs de `sejour.getId()` et `enfant.getId()` au lieu de créer un ID vide.
  - **Implémentation frontend complète** : Tous les composants frontend pour la gestion des enfants sont implémentés (`AddEnfantForm`, `ListeEnfants`, `ImportExcelEnfants`), intégrés dans `DetailsSejour`, et utilisent les types centralisés de `api.d.ts`. Les services frontend (`sejour.service.ts`) exposent toutes les méthodes nécessaires pour la gestion complète des enfants (CRUD + import Excel).
  - **Correction erreur 1102 dans EnfantServiceImplTest** : `@SuppressWarnings("null")` **une seule fois au niveau de la classe** (et non sur chaque méthode), pour éviter l'erreur Eclipse 1102 tout en couvrant les avertissements null-safety des mocks.
  - **Correction erreur 1102 dans ExcelHelperTest** : Suppression pure et simple de `@SuppressWarnings("null")` car l'analyse null n'est pas activée dans le projet (compilateur Maven standard). L'annotation était inutile et générait l'erreur 1102.

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
    - `CreateSejourRequest`, `CreateEnfantRequest`, `UpdateDossierEnfantRequest`, `CreateGroupeRequest`, `AjouterReferentRequest`, `CreateActiviteRequest`, `UpdateActiviteRequest` (incl. **`typeActiviteId`** **obligatoire** `@NotNull`), **`SaveMomentRequest`**, **`ReorderMomentsRequest`** (`momentIds`), `SaveLieuRequest`, **`SaveHoraireRequest`** (`libelle`), **`SaveTypeActiviteRequest`** (`libelle`), `RegisterRequest`, `AuthenticationRequest`, `UpdateUserRequest`
    - `MembreEquipeRequest`, `ChangePasswordRequest`, `RefreshTokenRequest`
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

## État Actuel du Projet

### Tests
- **Lancement des tests** :
  - Tous les tests : `mvn test`
  - Une classe : `mvn test -Dtest=EnfantServiceImplTest`
  - Un test spécifique : `mvn test -Dtest=EnfantServiceImplTest#creerEtAjouterEnfantAuSejour_WithNewEnfant_ShouldCreateAndAdd`
  - Depuis l'IDE : clic droit sur la classe/méthode → Run Java / Debug Java
- [FAIT] Tests unitaires complets pour `SejourServiceImpl` avec Mockito (`SejourServiceImplTest`) ✅
- Tests couvrent les cas d'usage principaux et l’évolution du séjour (y compris équipe, types d’activité par défaut, etc.) — **~35** méthodes `@Test` :
  - `getAllSejours()` : liste complète et liste vide
  - `getSejourById()` : séjour avec/sans directeur, séjour inexistant
  - `creerSejour()` : création avec/sans directeur, directeur inexistant
  - `modifierSejour()` : modification réussie, séjour/directeur inexistant
  - `ajouterMembreEquipe()` : ajout membre existant, membre déjà dans l'équipe, séjour inexistant
  - `modifierRoleMembreEquipe()` : modification réussie, membre non dans l'équipe, séjour inexistant
  - `supprimerMembreEquipe()` : suppression réussie, membre non dans l'équipe, membre inexistant
  - `supprimerSejour()` : suppression réussie, séjour inexistant
  - `getSejoursByDirecteur()` : récupération réussie, directeur inexistant
- Utilisation de `@ExtendWith(MockitoExtension.class)` et `@InjectMocks` pour l'injection des mocks.
- [FAIT] Les tests utilisent maintenant `ResourceNotFoundException` dans toutes les assertions `assertThatThrownBy()` pour une meilleure précision des tests ✅

- [FAIT] Tests unitaires complets pour `UtilisateurServiceImpl` avec Mockito (`UtilisateurServiceImplTest`) ✅
- Tests couvrent tous les cas d'usage principaux (**~32** méthodes `@Test`) :
  - `creerUtilisateur()` : création réussie, email déjà utilisé, téléphone déjà utilisé, exception lancée lors de la sauvegarde
  - `getAllUtilisateursDTO()` : liste complète, liste vide, exception lancée lors d'erreur
  - `getUtilisateursByRole()` : liste par rôle, liste vide, exception lancée lors d'erreur
  - `profilUtilisateur()` : utilisateur trouvé, utilisateur non trouvé
  - `getUtilisateurByEmail()` : utilisateur trouvé, utilisateur non trouvé
  - `mapUtilisateurToProfilDTO()` : mapping avec refreshToken, mapping sans refreshToken
  - `modifUserByUser()` : modification réussie, email déjà utilisé
  - `modifUserByAdmin()` : modification réussie, changement rôle DIRECTION (avec/sans séjours), mise à jour refreshToken, sans refreshToken, dateExpiration null, changement rôle non-DIRECTION
  - `supprimerUtilisateur()` : suppression réussie, utilisateur non trouvé
  - `changerMotDePasseParAdmin()` : changement réussi, utilisateur non trouvé
  - `changerMotDePasseParUtilisateur()` : changement réussi, ancien mot de passe incorrect, utilisateur non trouvé
- Tests organisés de manière cohérente par méthode avec tous les cas limites et d'erreur couverts.
- Utilisation de `@ExtendWith(MockitoExtension.class)`, `@InjectMocks`, `@Mock` et AssertJ pour les assertions.

- [FAIT] Tests unitaires complets pour `JwtServiceImpl` avec Mockito (`JwtServiceImplTest`) ✅
- Tests couvrent tous les cas d'usage principaux (7 tests) :
  - `extractUserName()` : extraction réussie d'un token valide
  - `generateToken()` : génération réussie d'un token valide, génération de tokens avec le même username
  - `isTokenValid()` : token valide retourne true, token avec mauvais username retourne false, token expiré lance exception, token invalide lance exception
- Tests organisés de manière cohérente par méthode avec tous les cas limites et d'erreur couverts.
- Utilisation de `@ExtendWith(MockitoExtension.class)`, `@Mock` et AssertJ pour les assertions.
- Configuration sécurisée avec clé de test dans `application-test.yml` (non poussée sur Git grâce à `.gitignore`).
- Utilisation de `ReflectionTestUtils` pour injecter les valeurs `@Value` depuis `application-test.yml` via `YamlPropertiesFactoryBean`.
- Utilisation de `verify()` pour vérifier les interactions avec `UserDetails`.

- [FAIT] Tests unitaires complets pour `RefreshTokenServiceImpl` avec Mockito (`RefreshTokenServiceImplTest`) ✅
- Tests couvrent tous les cas d'usage principaux (18 tests) :
  - `createRefreshToken()` : création réussie, utilisateur inexistant (2 tests)
  - `verifyExpiration()` : token valide retourne le token, token null lance exception, token expiré lance exception avec suppression (3 tests)
  - `findByToken()` : token trouvé, token inexistant (2 tests)
  - `findByUtilisateur()` : token trouvé, token inexistant (2 tests)
  - `generateNewToken()` : génération réussie, token inexistant lance exception, token expiré lance exception (3 tests)
  - `deleteByToken()` : suppression réussie, token inexistant ne fait rien (2 tests)
  - `generateRefreshTokenCookie()` : génération réussie d'un cookie avec toutes les propriétés (1 test)
  - `getRefreshTokenFromCookies()` : retourne le token du cookie, retourne chaîne vide si cookie absent (2 tests)
  - `getCleanRefreshTokenCookie()` : génération réussie d'un cookie vide pour suppression (1 test)
- Tests organisés de manière cohérente par méthode avec tous les cas limites et d'erreur couverts.
- Utilisation de `@ExtendWith(MockitoExtension.class)`, `@InjectMocks`, `@Mock` et AssertJ pour les assertions.
- Configuration sécurisée avec propriétés refresh-token dans `application-test.yml` (non poussée sur Git grâce à `.gitignore`).
- Utilisation de `ReflectionTestUtils` pour injecter les valeurs `@Value` depuis `application-test.yml` via `YamlPropertiesFactoryBean`.
- Utilisation de `MockedStatic` pour mocker `WebUtils.getCookie()` (méthode statique).
- Utilisation de `verify()` pour vérifier les interactions avec les repositories et services.

### Tests à Faire

#### Tests Unitaires de Services (Priorité Haute)
- [FAIT] **`UtilisateurServiceImplTest`** ✅
  - ~32 tests couvrant tous les cas d'usage, cas limites et cas d'erreur
  - Tests organisés de manière cohérente par méthode
  - Utilisation d'AssertJ et Mockito avec `@ExtendWith(MockitoExtension.class)`

- [FAIT] **`AuthenticationServiceImplTest`** ✅
  - 7 tests couvrant tous les cas d'usage, cas limites et cas d'erreur
  - `register()` : inscription réussie, email déjà utilisé, date d'expiration null (3 tests)
  - `authenticate()` : authentification réussie, identifiants invalides (`BadCredentialsException`), utilisateur non trouvé (`ResourceNotFoundException`), refreshToken introuvable (`ResourceNotFoundException`) (4 tests)
  - Tests organisés de manière cohérente par méthode
  - Utilisation d'AssertJ et Mockito avec `@ExtendWith(MockitoExtension.class)`

- [FAIT] **`JwtServiceImplTest`** ✅
  - 7 tests couvrant tous les cas d'usage, cas limites et cas d'erreur
  - `extractUserName()` : extraction réussie d'un token valide (1 test)
  - `generateToken()` : génération réussie d'un token valide, génération de tokens avec le même username (2 tests)
  - `isTokenValid()` : token valide retourne true, token avec mauvais username retourne false, token expiré lance exception, token invalide lance exception (4 tests)
  - Tests organisés de manière cohérente par méthode
  - Utilisation d'AssertJ et Mockito avec `@ExtendWith(MockitoExtension.class)`
  - Configuration sécurisée avec clé de test dans `application-test.yml`
  - Utilisation de `ReflectionTestUtils` pour injecter les valeurs `@Value`

- [FAIT] **`RefreshTokenServiceImplTest`** ✅
  - 18 tests couvrant tous les cas d'usage, cas limites et cas d'erreur
  - `createRefreshToken()` : création réussie, utilisateur inexistant (2 tests)
  - `verifyExpiration()` : token valide, token null, token expiré (3 tests)
  - `findByToken()` : token trouvé, token inexistant (2 tests)
  - `findByUtilisateur()` : token trouvé, token inexistant (2 tests)
  - `generateNewToken()` : génération réussie, token inexistant, token expiré (3 tests)
  - `deleteByToken()` : suppression réussie, token inexistant (2 tests)
  - `generateRefreshTokenCookie()` : génération réussie (1 test)
  - `getRefreshTokenFromCookies()` : cookie trouvé, cookie absent (2 tests)
  - `getCleanRefreshTokenCookie()` : génération réussie (1 test)
  - Tests organisés de manière cohérente par méthode
  - Utilisation d'AssertJ et Mockito avec `@ExtendWith(MockitoExtension.class)`
  - Configuration sécurisée avec propriétés refresh-token dans `application-test.yml`
  - Utilisation de `ReflectionTestUtils` pour injecter les valeurs `@Value`
  - Utilisation de `MockedStatic` pour mocker les méthodes statiques (`WebUtils.getCookie()`)

- [FAIT] **`EnfantServiceImplTest`** ✅
  - 23 tests couvrant tous les cas d'usage, cas limites et cas d'erreur
  - `creerEtAjouterEnfantAuSejour()` : création réussie, enfant existant réutilisé, enfant déjà dans le séjour (409), séjour inexistant (404)
  - `modifierEnfant()` : modification réussie, remplacement par enfant existant, enfant existant déjà dans séjour (409), enfant non inscrit (404)
  - `supprimerEnfantDuSejour()` : suppression réussie, suppression de l'enfant si dernier séjour, enfant non inscrit (404)
  - `supprimerTousLesEnfantsDuSejour()` : suppression réussie, suppression des enfants orphelins, séjour sans enfants, séjour inexistant (404), ne pas supprimer les enfants dans d'autres séjours
  - `getEnfantsDuSejour()` : récupération réussie avec liste, liste vide, séjour inexistant (404)
  - `importerEnfantsDepuisExcel()` : import réussi, colonnes manquantes (erreurs retournées), enfants déjà existants, séjour inexistant (erreurs retournées), fichier vide (exception)
  - Tests organisés de manière cohérente par méthode
  - Utilisation d'AssertJ et Mockito avec `@ExtendWith(MockitoExtension.class)`
  - Utilisation de fichiers Excel réels créés avec Apache POI (XSSFWorkbook) et MockMultipartFile

- [FAIT] **`GroupeServiceImplTest`** ✅
  - 22 tests couvrant `getGroupesDuSejour()`, `getGroupeById()`, `creerGroupe()`, `modifierGroupe()`, `supprimerGroupe()`, `ajouterEnfantAuGroupe()`, `retirerEnfantDuGroupe()`, `ajouterReferentAuGroupe()`, `retirerReferentDuGroupe()`, conflits (409), séjour/groupe/enfant inexistant, tranches AGE / NIVEAU_SCOLAIRE
  - Utilisation d'AssertJ et Mockito avec `@ExtendWith(MockitoExtension.class)`, `@SuppressWarnings("null")` au niveau classe

- [FAIT] **`ActiviteServiceImplTest`** ✅
  - **22** tests : comme avant + **aucun moment sur le séjour** → 400 (message direction), **`momentId` null** alors qu’il existe des moments → 400 ; mocks **`MomentRepository`** (`countBySejourId`, `findByIdAndSejourId`) ; comptages lieu **`countBySejour_IdAndLieu_IdAndDateAndMoment_Id`** ; **`countActivitesAvecMembreMemeCreneau`** (conflit **POST** / **PUT** + **PUT** succès avec exclusion d’id) ; **`ConflitPlanningAnimateurException`** ; liste (succès, séjour absent), création (persistance OK, lieu / partage / avertissement, erreurs lieu, sans lieu, directeur hors équipe, validations date & équipe & groupe), modification (date hors période, **mise à jour** avec vérif dispo membre excluant l’activité courante), get, suppression absente

- [FAIT] **`TypeActiviteServiceImplTest`** ✅
  - Tests Mockito : création par séjour (`predefini` false), refus modification / suppression si prédéfini, modification / suppression OK pour type utilisateur, **`assurerTypesParDefautPourSejour`** (inserts attendus). **`SejourRepository`** mocké.

#### Tests Unitaires de Contrôleurs (Priorité Moyenne)
- [FAIT] **`SejourControllerTest`** ✅
  - 27 tests couvrant tous les cas d'usage, cas limites et cas d'erreur
  - `getAllSejours()` : 200 OK avec liste, 200 OK avec liste vide (2 tests)
  - `getSejourById()` : 200 OK, 404 Not Found (2 tests)
  - `creerSejour()` : 200 OK, 404 Not Found (directeur inexistant) (2 tests)
  - `modifierSejour()` : 200 OK, 404 Not Found (2 tests)
  - `supprimerSejour()` : 204 No Content, 404 Not Found (2 tests)
  - `getSejoursByDirecteur()` : 200 OK avec liste, 404 Not Found (directeur inexistant) (2 tests)
  - `ajouterMembreExistant()` : 201 Created, 400 Bad Request (validation), 404 Not Found (séjour inexistant), 409 Conflict (membre déjà dans l'équipe) (4 tests)
  - `ajouterNouveauMembre()` : 201 Created, 400 Bad Request (validation), 404 Not Found (séjour inexistant), 409 Conflict (membre déjà dans l'équipe) (4 tests)
  - `modifierRoleMembreEquipe()` : 204 No Content, 400 Bad Request (validation), 404 Not Found (séjour inexistant) (3 tests)
  - `supprimerMembreEquipe()` : 204 No Content, 404 Not Found (séjour inexistant), 404 Not Found (membre inexistant) (3 tests)
  - Tests organisés de manière cohérente par méthode
  - Utilisation de `@InjectMocks` pour injecter automatiquement les mocks dans le contrôleur (cohérence avec les autres tests)
  - Utilisation de `MockMvcBuilders.standaloneSetup()` avec `@ExtendWith(MockitoExtension.class)`
  - Configuration de `GlobalExceptionHandler` pour la gestion des exceptions
  - Utilisation de `ObjectMapper` avec module JSR310 pour la sérialisation des dates
  - **Note importante** : Dans les tests unitaires avec `standaloneSetup()`, les annotations `@PreAuthorize` ne sont pas évaluées par Spring Security. Les tests vérifient la logique métier du contrôleur, pas la sécurité réelle. Pour tester réellement la sécurité, il faudrait des tests d'intégration avec un contexte Spring complet.
  - **Note** : La validation Jakarta (`@Valid`) n'est pas activée automatiquement dans `standaloneSetup()`. Les tests de validation vérifient que le service n'est pas appelé, mais la validation réelle nécessiterait des tests d'intégration avec `@WebMvcTest`.

- [FAIT] **`UtilisateurControllerTest`** ✅
  - 18 tests couvrant tous les cas d'usage, cas limites et cas d'erreur
  - `consulterLaListeDesUtilisateurs()` : 200 OK avec liste, 200 OK avec liste vide (2 tests)
  - `consulterLaListeDesUtilisateursParRole()` : 200 OK avec liste filtrée par rôle (1 test)
  - `chercherUtilisateurParEmail()` : 200 OK, 400 Bad Request (utilisateur DIRECTION), 400 Bad Request (utilisateur ADMIN), 404 Not Found (4 tests)
  - `profilUtilisateur()` : 200 OK, 404 Not Found (2 tests)
  - `supprimerUtilisateur()` : 204 No Content (1 test)
  - `modifierUtilisateur()` : 200 OK par admin, 200 OK par utilisateur, 404 Not Found (3 tests)
  - `changerMotDePasse()` : 200 OK par admin, 200 OK par utilisateur, 403 Forbidden (token différent), 400 Bad Request (ancien mot de passe manquant), 400 Bad Request (ancien mot de passe vide) (5 tests)
  - Tests organisés de manière cohérente par méthode
  - Utilisation de `@InjectMocks` pour injecter automatiquement les mocks dans le contrôleur (cohérence avec les autres tests)
  - Utilisation de `MockMvcBuilders.standaloneSetup()` avec `@ExtendWith(MockitoExtension.class)`
  - Configuration de `GlobalExceptionHandler` pour la gestion des exceptions
  - Utilisation de `ObjectMapper` avec module JSR310 pour la sérialisation des dates
  - Utilisation de mocks d'`Authentication` pour simuler différents types d'utilisateurs (admin avec `GESTION_UTILISATEURS`, utilisateur normal)
  - Utilisation de `@MockitoSettings(strictness = Strictness.LENIENT)` pour permettre les stubs inutilisés
  - **Note importante** : Dans les tests unitaires avec `standaloneSetup()`, les annotations `@PreAuthorize` ne sont pas évaluées par Spring Security. Les tests vérifient la logique métier du contrôleur, pas la sécurité réelle. Pour tester réellement la sécurité, il faudrait des tests d'intégration avec un contexte Spring complet.

- [FAIT] **`AuthenticationControllerTest`** ✅
  - 9 tests couvrant tous les cas d'usage, cas limites et cas d'erreur
  - `register()` : 200 OK avec tokens et cookie, 400 Bad Request (validation), 409 Conflict (email utilisé) (3 tests)
  - `authenticate()` : 200 OK avec tokens et cookie, 404 Not Found (identifiants invalides - BadCredentialsException hérite de RuntimeException) (2 tests)
  - `refreshToken()` : 200 OK avec nouveau token, 404 Not Found (token invalide - TokenException hérite de RuntimeException) (2 tests)
  - `logout()` : 200 OK avec suppression du cookie, 200 OK même sans cookie (2 tests)
  - Tests organisés de manière cohérente par méthode
  - Utilisation de `@InjectMocks` pour injecter automatiquement les mocks dans le contrôleur (cohérence avec les autres tests)
  - Utilisation de `MockMvcBuilders.standaloneSetup()` avec `@ExtendWith(MockitoExtension.class)`
  - Configuration de `GlobalExceptionHandler` et `TokenControllerHandler` pour la gestion des exceptions
  - Utilisation de `ObjectMapper` avec module JSR310 pour la sérialisation des dates

- [FAIT] **`EnfantControllerTest`** ✅
  - `getEnfantsDuSejour()` : 200 OK avec liste, 200 OK avec liste vide, 404 Not Found (séjour inexistant) (3 tests)
  - `creerEtAjouterEnfantAuSejour()` : 201 Created, 404 Not Found (séjour inexistant), 409 Conflict (enfant déjà dans séjour) (3 tests) — la validation `@Valid` en `standaloneSetup()` n’est pas couverte comme en intégration
  - `modifierEnfant()` : 200 OK, 404 Not Found, 409 Conflict (3 tests)
  - `supprimerEnfantDuSejour()` : 204 No Content, 404 Not Found (2 tests)
  - `supprimerTousLesEnfantsDuSejour()` : 204 No Content, 404 Not Found (2 tests)
  - `getExcelImportSpec()` : 200 OK avec spécification (colonnes obligatoires, optionnelles, formats) (1 test)
  - `importerEnfantsDepuisExcel()` : 200 OK avec réponse complète, 400 Bad Request (fichier vide), 400 Bad Request (format invalide), 200 avec erreurs si colonnes manquantes, 404 Not Found (séjour inexistant) (5 tests)
  - Tests organisés de manière cohérente par méthode
  - Utilisation de `@InjectMocks` pour injecter automatiquement les mocks dans le contrôleur (cohérence avec les autres tests)
  - Utilisation de `MockMvcBuilders.standaloneSetup()` avec `@ExtendWith(MockitoExtension.class)`
  - Configuration de `GlobalExceptionHandler` pour la gestion des exceptions
  - Utilisation de `MockMultipartFile` pour simuler les fichiers Excel
  - Utilisation de `ObjectMapper` avec module JSR310 pour la sérialisation des dates
  - **Note importante** : Dans les tests unitaires avec `standaloneSetup()`, les annotations `@PreAuthorize` ne sont pas évaluées par Spring Security. Les tests vérifient la logique métier du contrôleur, pas la sécurité réelle.
  - **19** tests au total : getEnfantsDuSejour (3), creerEtAjouterEnfantAuSejour (3), modifierEnfant (3), supprimerEnfantDuSejour (2), supprimerTousLesEnfantsDuSejour (2), getExcelImportSpec (1), importerEnfantsDepuisExcel (5)

- [FAIT] **`GroupeControllerTest`** ✅
  - 11 tests : `getGroupesDuSejour`, `getGroupeById` (200/404), `creerGroupe` (201), `modifierGroupe` (200), `supprimerGroupe` (204), ajout/retrait enfant (204, 409 conflit), ajout/retrait référent (201/204)
  - `MockMvc` standalone + `GlobalExceptionHandler`, même remarque que les autres contrôleurs sur `@PreAuthorize` / `@Valid`

- [FAIT] **`LieuControllerTest`** ✅
  - 7 tests : `GET .../lieux` (200), `GET .../lieux/{id}` (200), `GET` (404), `POST` (201), `POST` (409 doublon de nom), `PUT` (200), `DELETE` (204)
  - `MockMvc` standalone + `GlobalExceptionHandler`, même remarque sur `@PreAuthorize` / `@Valid` ; contrôleur injecté par **constructeur explicite** (compatible `@InjectMocks`)

- [FAIT] **`LieuServiceImplTest`** ✅
  - **8** tests : doublon nom, trim, modifier (conflit / même nom casse), lister 404 / vide, **partage activé sans max** → 400, **partage avec max 2** OK
  - `@SuppressWarnings("null")` au **niveau de la classe** (mocks Mockito, cohérent avec `GroupeServiceImplTest` / `LieuServiceImpl`)

- [FAIT] **`HoraireControllerTest`** ✅
  - **7** tests : `GET .../horaires` (200), `GET .../horaires/{id}` (200), `GET` (404), `POST` (201), `POST` (409 doublon de libellé), `PUT` (200), `DELETE` (204)
  - `MockMvc` standalone + `GlobalExceptionHandler`, même remarque sur `@PreAuthorize` / `@Valid`

- [FAIT] **`HoraireServiceImplTest`** ✅
  - **7** tests : doublon libellé (trim), création trim, modifier (conflit / même libellé conservé), lister 404 / vide, get mauvais séjour → 404
  - `@SuppressWarnings("null")` au **niveau de la classe** ; **`SejourVerificationService`** comme pour `LieuServiceImplTest`

- [FAIT] **`ActiviteControllerTest`** ✅ (couverture partielle)
  - 2 tests : `GET .../activites` (200, réponse avec **`moment`** + **`lieu`** + **`typeActivite`** dans l’`ActiviteDto`), `POST .../activites` (201, body avec **`lieuId`**, **`momentId`**, **`typeActiviteId`** obligatoire)
  - À compléter si besoin : GET par id, PUT, DELETE, erreurs 404 / 400 (lieu, moments)

#### Tests Unitaires de Mappers et Utilitaires (Priorité Moyenne)
- [FAIT] **Tests de conversion DTO** ✅
  - [FAIT] `mapUtilisateurToProfilDTO()` : conversion complète avec tous les champs (déjà testé dans `UtilisateurServiceImplTest`) ✅
  - [FAIT] `mapSejourToSejourDto()` (méthode privée `mapToDTO()` dans `SejourServiceImpl`) : 6 tests couvrant tous les cas ✅
    - Conversion avec directeur et équipe complète (1 test)
    - Conversion avec directeur mais sans équipe (1 test)
    - Conversion sans directeur mais avec équipe (1 test)
    - Conversion sans directeur et sans équipe (1 test)
    - Vérification de tous les champs d'un membre d'équipe (1 test)
    - Vérification que l'équipe n'est pas incluse quand `includeEquipe = false` (1 test)
  - Tests organisés de manière cohérente par cas d'usage
  - Utilisation des méthodes publiques (`getSejourById()` avec équipe, `getAllSejours()` sans équipe) pour tester la méthode privée `mapToDTO()`
  - Vérification exhaustive de tous les champs mappés (id, nom, description, dates, lieu, directeur, équipe)

- [FAIT] **`ExcelHelperTest`** ✅ — **35** tests organisés par `@Nested`
  - `detectColumns()` : détection réussie avec colonnes exactes, détection avec colonnes contenant mots-clés, colonnes manquantes, colonnes avec accents/espaces (4 tests). Utilise `Map<String, String[][]>` (groupes).
  - `normalizeColumnName()` : normalisation correcte (suppression accents, espaces, casse), gestion des valeurs null/vides, espaces multiples (4 tests)
  - `containsKeyword()` : détection de mots-clés simples, multiples mots-clés, mots-clés avec accents (échappements Unicode `\u00e9` pour éviter les problèmes d'encodage), valeurs null/vides (5 tests)
  - `containsAllGroups()` : matcher quand tous les groupes présents (ET), refuser quand un groupe manque, distinguer parent 1 et parent 2 (3 tests)
  - `isRowEmpty()` : ligne vide détectée, ligne avec données détectée, row null, cellules null (4 tests)
  - `getCellValueAsString()` : extraction STRING, NUMERIC entier/décimal, NUMERIC date formatée, BOOLEAN, FORMULA, cellule null (7 tests)
  - `parseDateFromString()` : parsing format dd/MM/yyyy, yyyy-MM-dd, format Excel numérique, format invalide, null/vide lance ParseException (6 tests)
  - `formatDate()` : formatage correct en dd/MM/yyyy, gestion des dates null (2 tests)
  - Utilisation de JUnit 5 et AssertJ
  - Utilisation de XSSFWorkbook pour créer des lignes et cellules réelles

#### Tests Unitaires de Gestionnaires d'Exceptions (Priorité Basse)
- [FAIT] **`GlobalExceptionHandlerTest`** ✅
  - 13 tests couvrant tous les handlers d'exceptions
  - `handleValidationExceptions()` : 400 Bad Request avec détails des erreurs (test direct + test via MockMvc) (1 test)
  - `handleResourceAlreadyExistsException()` : 409 Conflict (test direct + test via MockMvc) (2 tests)
  - `handleEmailDejaUtiliseException()` : 409 Conflict (test direct + test via MockMvc) (2 tests)
  - `handleUtilisateurException()` : 400 Bad Request (test direct + test via MockMvc) (2 tests)
  - `handleResourceNotFoundException()` : 404 Not Found (test direct + test via MockMvc) (2 tests)
  - `handleRuntimeException()` : 404 Not Found (test direct + test via MockMvc) (2 tests)
  - `handleIllegalArgumentException()` : 400 Bad Request (test direct + test via MockMvc) (2 tests)
  - Tests organisés de manière cohérente par handler
  - Utilisation d'un contrôleur de test (`TestController`) pour déclencher les exceptions via MockMvc
  - Tests directs des méthodes du handler pour vérifier le format de réponse exact
  - Utilisation de `MethodParameter` réel créé via Reflection pour tester `MethodArgumentNotValidException`

- [FAIT] **`TokenControllerHandlerTest`** ✅
  - 5 tests couvrant tous les cas d'usage
  - `handleRefreshTokenException()` : 403 Forbidden avec `ErrorResponse` structuré (test direct + test via MockMvc) (2 tests)
  - Vérification du format de réponse complet : status, error, timestamp, message, path (3 tests)
  - Tests organisés de manière cohérente par méthode
  - Utilisation d'un contrôleur de test (`TestController`) pour déclencher les exceptions via MockMvc
  - Tests directs des méthodes du handler pour vérifier le format de réponse exact
  - Utilisation de `MockHttpServletRequest` pour créer des `ServletWebRequest` dans les tests directs

#### Tests Unitaires de Filtres et Sécurité (Priorité Basse)
- [FAIT] **`JwtAuthenticationFilterTest`** ✅
  - 11 tests couvrant tous les cas d'usage
  - Pas de header Authorization : continue la chaîne sans authentification (1 test)
  - Header Authorization ne commence pas par "Bearer " : continue la chaîne sans authentification (1 test)
  - Token valide : authentification réussie avec définition du SecurityContext (1 test)
  - Token invalide : continue la chaîne sans authentification (1 test)
  - Token expiré (`ExpiredJwtException`) : continue la chaîne sans authentification (1 test)
  - Exception JWT (`JwtException`) : continue la chaîne sans authentification (1 test)
  - Exception générique : continue la chaîne sans authentification (1 test)
  - Email extrait vide : ne pas authentifier (1 test)
  - Authentification déjà existante : ne pas remplacer l'authentification existante (1 test)
  - Extraction correcte du token depuis le header Bearer (1 test)
  - Définition des détails de l'authentification avec WebAuthenticationDetailsSource (1 test)
  - Tests organisés de manière cohérente par cas d'usage
  - Utilisation de Mockito pour mocker HttpServletRequest, HttpServletResponse, FilterChain, JwtServiceImpl et UserDetailsService
  - Vérification du SecurityContext pour confirmer l'authentification ou son absence

#### Tests d'Intégration (Priorité Basse)
- [ ] **Tests de Repository** (avec `@DataJpaTest` et H2) :
  - `SejourRepository` : CRUD complet, requêtes personnalisées (`findByDirecteur`)
  - `UtilisateurRepository` : CRUD complet, `findByTokenId`, `findByEmail`
  - `SejourEquipeRepository` : CRUD avec clé composite
  - `RefreshTokenRepository` : CRUD, `findByToken`, `deleteByUserId`

- [ ] **Tests d'Intégration End-to-End** (avec `@SpringBootTest` et `@AutoConfigureMockMvc`) :
  - Scénarios complets d'authentification (inscription → connexion → refresh → logout)
  - Scénarios complets de gestion de séjours (création → ajout membre → modification → suppression)
  - Scénarios complets de gestion d'utilisateurs (création → modification → suppression)

#### Améliorations des Tests Existants
- [FAIT] **`SejourServiceImplTest`** ✅
  - [FAIT] Remplacer `RuntimeException` par `ResourceNotFoundException` dans toutes les assertions - toutes les assertions utilisent maintenant `ResourceNotFoundException` ✅
  - [FAIT] Tests complets pour tous les cas d'usage principaux (19 tests) couvrant les cas limites et cas d'erreur ✅
  - Les tests couvrent déjà les cas avec/sans directeur, membres existants/nouveaux, équipe vide, etc.
  - La validation des dates et chaînes vides/null est gérée par Jakarta Validation au niveau du contrôleur, donc les tests du service se concentrent sur la logique métier

### Exceptions
- [FAIT] `ResourceAlreadyExistsException` créée et intégrée dans `GlobalExceptionHandler` (HTTP 409) ✅
- [FAIT] `ResourceNotFoundException` créée et intégrée dans `GlobalExceptionHandler` (HTTP 404) ✅
- [FAIT] `ResourceNotFoundException` utilisée dans `SejourServiceImpl` (16 occurrences remplacées) ✅
- [FAIT] `ResourceNotFoundException` utilisée dans `AuthenticationServiceImpl` pour les cas "utilisateur non trouvé" et "refreshToken introuvable" ✅
- [FAIT] `GlobalExceptionHandler` gère : `MethodArgumentNotValidException`, `ResourceNotFoundException`, `ResourceAlreadyExistsException`, `EmailDejaUtiliseException`, `UtilisateurException`, `RuntimeException`, `IllegalArgumentException` ✅
- [FAIT] `TokenControllerHandler` gère `TokenException` avec format d'erreur structuré (`ErrorResponse`) - retourne HTTP 403 Forbidden ✅
- [FAIT] Remplacer `RuntimeException` par `ResourceNotFoundException` dans `UtilisateurController` ligne 92 ✅
- [FAIT] Nettoyer les `try-catch` dans `UtilisateurServiceImpl` qui retournent `null` au lieu de laisser les exceptions remonter :
  - `creerUtilisateur()` : try-catch supprimé, exceptions remontent naturellement ✅
  - `getAllUtilisateursDTO()` : try-catch supprimé, exceptions remontent naturellement ✅
  - `getUtilisateursByRole()` : try-catch supprimé, exceptions remontent naturellement ✅

### Entités & Relations
- `SejourEquipe` : Table de jointure avec clé composite (`SejourEquipeId`).
- `RoleSejour` : Enum pour les rôles dans une équipe de séjour.
- `Utilisateur` : Le champ `genre` utilise l'enum `Genre` (aligné avec `Enfant`). Implémente `UserDetails` pour Spring Security.
- Relations bien définies entre `Sejour`, `Utilisateur`, et `SejourEquipe`.
- `Enfant` : Entité représentant un enfant avec ses informations personnelles uniquement (nom, prénom, genre, date de naissance, niveau scolaire).
  - **Important** : Un enfant peut exister indépendamment et être réutilisé dans plusieurs séjours. Les informations des parents et du dossier (contacts, médical, traitements) sont stockées dans `DossierEnfant`.
  - **Relations JPA** : `@OneToOne` avec `DossierEnfant` (côté inverse `mappedBy="enfant"`, cascade, orphanRemoval) ; `@OneToMany` vers `SejourEnfant` ; `@ManyToMany(mappedBy="enfants")` vers `Groupe` (côté inverse de la collection `enfants` sur `Groupe`, table `groupe_enfant`). Un dossier est créé automatiquement à la création d'un enfant.
  - **Configuration des enums** : `Genre` et `NiveauScolaire` utilisent `@Enumerated(EnumType.STRING)` pour un stockage lisible en base de données.
  - **Identité d'un enfant** : Un enfant est identifié par la combinaison unique de nom, prénom, genre et date de naissance (méthode `findByNomAndPrenomAndGenreAndDateNaissance` dans `EnfantRepository`).
- `DossierEnfant` : Entité OneToOne avec `Enfant` pour les informations de dossier (emailParent1/2, telephoneParent1/2, informationsMedicales, pai, informationsAlimentaires, traitements matin/midi/soir/si besoin, autresInformations, aPrendreEnSortie). Validation Jakarta sur email et téléphone. `DossierEnfantRepository.findByEnfantId()`. **Import Excel** : Si l'enfant existant a déjà un dossier, on met à jour le dossier existant (évite la violation de contrainte unique sur `enfant_id`).
- `SejourEnfant` : Table de jointure avec clé composite (`SejourEnfantId`) pour la relation Many-to-Many entre `Sejour` et `Enfant`.
  - **Important** : La propriété `enfant` fait partie de la clé primaire composite (`@MapsId("enfantId")`), donc elle est immuable. Pour remplacer un enfant dans un séjour, il faut supprimer l'ancienne relation et créer une nouvelle relation.
- `Groupe` : Entité liée à `Sejour` (ManyToOne). Trois types via enum `TypeGroupe` : `THEMATIQUE` (enfants manuels uniquement), `AGE` (tranche ageMin-ageMax, ajout auto à la création), `NIVEAU_SCOLAIRE` (tranche niveauScolaireMin-Max, ajout auto). Relations `@ManyToMany` avec `Enfant` (table `groupe_enfant`) et `Utilisateur` (table `groupe_referent` pour les référents). `GroupeRepository.findBySejourId()`.
- `Lieu` : Lieu pour un `Sejour` (ManyToOne obligatoire). Champs : `nom`, `emplacement` (`EmplacementLieu`), **`nombreMax`** (capacité personnes, optionnel), **`partageableEntreAnimateurs`** (`boolean`), **`nombreMaxActivitesSimultanees`** (`Integer`, si partage : ≥ 2 ; sinon `null`). Table `lieu`. **Sans Lombok**. `LieuRepository` : `findBySejour`, `findBySejourId`, **`findByIdAndSejourId`**, `existsBy*`. `Sejour.lieux` `OneToMany`. Référencé par **`Activite.lieu`** (optionnel). Le plafond de partage s’applique par **jour + moment** (voir comptages activités).
- **`Horaire`** : Libellé horaire pour un **`Sejour`** (**`ManyToOne` obligatoire**). Champ **`libelle`** (affichage type **`8h30`**, voir pattern **`Horaire.LIBELLE_HORAIRE_PATTERN`**). Contrainte **`uk_horaire_sejour_libelle`**. Table **`horaire`**. **`HoraireRepository`** : **`findBySejourIdOrderByIdAsc`**, **`findByIdAndSejourId`**, **`existsBySejourIdAndLibelleIgnoreCase*`**. **`Sejour.horaires`** **`OneToMany`**. Pas de lien JPA avec **`Activite`** à ce stade.
- **`Moment`** : Créneau (ex. matin / après-midi) pour un `Sejour` (**`ManyToOne` obligatoire**). `nom` unique par séjour (`uk_moment_sejour_nom`). **`ordre`** (`Integer`, nullable) : position dans la journée ; liste triée par **`COALESCE(ordre, id)`** puis `id`. Table `moment`. **Sans Lombok**. `MomentRepository` : `countBySejourId`, **`findBySejourIdOrderChronologique`** (JPQL avec `COALESCE`), `findByIdAndSejourId`, `existsBySejourIdAndNomIgnoreCase*`. `Sejour.moments` `OneToMany`. Référencé par **`Activite.moment`** (obligatoire).
- **`TypeActivite`** : Types d’activité **par séjour** (table **`type_activite`**, **`ManyToOne`** obligatoire vers **`Sejour`**). Unicité **`(sejour_id, libelle)`** (`uk_type_activite_sejour_libelle`). Champs : **`libelle`**, **`predefini`**. Liste des six libellés système : **`TypeActiviteLibellesParDefaut.LIBELLES`**. Bootstrap : **`assurerTypesParDefautPourSejour`** (création de séjour + **`TypeActiviteInitializer`** au démarrage par séjour). CRUD API **`/api/v1/sejours/{sejourId}/types-activite`**. Entité **sans Lombok** (POJO comme `Moment` / `Lieu`).
- `Activite` : `LocalDate` date, nom, description, **`@ManyToOne` obligatoire `Moment`** (`moment_id`), **`@ManyToOne` optionnel `Lieu`**, **`@ManyToOne` obligatoire `TypeActivite`** (`type_activite_id` NOT NULL). Règles **jour + moment** sur un lieu : comptage `countBySejour_IdAndLieu_IdAndDateAndMoment_Id` / `...AndIdNot`, partage **`avertissementLieu`** dans le DTO après POST/PUT. `@ManyToMany` `Utilisateur` (`activite_membre_equipe`), `Groupe` (`activite_groupe`). **`existsByMomentId`** (garde à la suppression d’un moment). **`countByTypeActivite_Id`** (garde à la suppression d’un type d’activité).
- Relations bien définies entre `Sejour`, `Enfant`, et `SejourEnfant` (pattern similaire à `SejourEquipe`).

### Synchronisation Backend-Frontend
- Le dépôt **enjoyApi** ne contient pas le frontend ; les chemins ci-dessous supposent le projet web associé (ex. `enjoyWebApp`).
- [FAIT] Fichier `api.d.ts` dans le frontend (`enjoyWebApp/src/types/api.d.ts`) avec les types TypeScript alignés sur les DTOs Java.
- Types disponibles : `SejourDTO`, `ProfilUtilisateurDTO`, `EnfantDto`, `DossierEnfantDto`, `GroupeDto`, `CreateGroupeRequest`, `AjouterReferentRequest`, **`MomentDto`** (incl. **`ordre`**), **`SaveMomentRequest`**, **`ReorderMomentsRequest`**, **`HoraireDto`**, **`SaveHoraireRequest`**, `ActiviteDto` (**`moment`** avec **`ordre`**, **`lieu`**, **`typeActivite`** avec **`sejourId`** (obligatoire côté domaine), **`avertissementLieu`** optionnel surtout après POST/PUT, **`groupeIds`**) / `CreateActiviteRequest` / `UpdateActiviteRequest` (`lieuId?`, **`momentId`**, **`typeActiviteId`** obligatoire, `groupeIds`), **`TypeActiviteDto`** (**`sejourId`**, **`predefini`**), **`SaveTypeActiviteRequest`**, **`LieuDto`** (**`partageableEntreAnimateurs`**, **`nombreMaxActivitesSimultanees`**) / **`SaveLieuRequest`** (mêmes champs partage), `EmplacementLieu`, `CreateSejourRequest`, `CreateEnfantRequest`, `UpdateDossierEnfantRequest`, `MembreEquipeRequest`, `RegisterRequest`, `UpdateUserRequest`, `AuthenticationRequest`, `AuthenticationResponse`, `RefreshTokenResponse`, `ErrorResponse`, `ExcelImportResponse`, `ExcelImportSpecResponse`, `ExcelImportColumnSpec`. **À jour côté enjoyApi** : vérifier que le frontend **`api.d.ts`** inclut bien **`moment` / `momentId`**, **`moment.ordre`**, **`PUT .../moments/reorder`** avec **`momentIds`**, **`HoraireDto` / `SaveHoraireRequest`** + CRUD **`/sejours/{sejourId}/horaires`**, et **`typeActivite` / `typeActiviteId`** (**obligatoire** en création / édition d’activité) + CRUD sous **`/sejours/{sejourId}/types-activite`**.
- Les dates Java (`Date`, `Instant`) sont typées comme `string` en TypeScript car sérialisées en ISO 8601 par Jackson.
- [FAIT] Migration des types locaux vers `api.d.ts` effectuée :
  - `sejour.service.ts` : `SejourInfos` → `CreateSejourRequest`, retours typés avec `SejourDTO`
  - `DetailsSejour.tsx` : interface locale `Sejour` → `SejourDTO`
  - `ListeSejoursAdmin.tsx` : interface locale `Sejour` → `SejourDTO`
  - `ListeSejoursDirecteur.tsx` : interface locale `Sejour` → `SejourDTO`
  - `SejourForm.tsx` : `SejourInfos` → `CreateSejourRequest`
  - `UserForm.tsx` : `AddMembreRequest` → `MembreEquipeRequest`
- Tous les services et composants liés aux séjours utilisent maintenant les types centralisés de `api.d.ts`.
- [FAIT] Implémentation frontend complète de la gestion des enfants :
  - `AddEnfantForm.tsx` : Formulaire générique pour créer/modifier un enfant (utilise le composant `Form.tsx`)
  - `ListeEnfants.tsx` : Composant de liste avec filtres, tri, actions CRUD (création, modification, suppression)
  - `ImportExcelEnfants.tsx` : Composant d'import Excel avec gestion des résultats et messages d'erreur
  - `DetailsSejour.tsx` : Intégration de la liste des enfants dans la page de détails d'un séjour
  - `sejour.service.ts` : Services complets pour gérer les enfants (`getEnfantsDuSejour`, `creerEtAjouterEnfant`, `modifierEnfant`, `supprimerEnfantDuSejour`, `supprimerTousLesEnfants`, `importerEnfantsExcel`)
  - Tous les composants utilisent les types centralisés de `api.d.ts` (`EnfantDto`, `CreateEnfantRequest`, `ExcelImportResponse`)

## Documentation API REST

### Base URL
- **Base Path** : `/api/v1`
- **Authentification** : JWT Bearer Token (sauf endpoints d'authentification)
- **Format des dates** : ISO 8601 (sérialisées en `string` côté frontend)

### Endpoints d'Authentification (`/api/v1/auth`)

#### POST `/api/v1/auth/inscription`
- **Description** : Inscription d'un nouvel utilisateur
- **Autorisation** : Aucune
- **Body** : `RegisterRequest`
- **Réponse** : `AuthenticationResponse` (200 OK)
  - `accessToken` : JWT token
  - `refreshToken` : `null` (dans le body, envoyé via cookie HttpOnly)
  - Cookie `refreshToken` : HttpOnly, Secure, SameSite
- **Codes d'erreur** :
  - `400` : Validation échouée (champs invalides)
  - `409` : Email déjà utilisé (`EmailDejaUtiliseException`)

#### POST `/api/v1/auth/connexion`
- **Description** : Connexion d'un utilisateur existant
- **Autorisation** : Aucune
- **Body** : `AuthenticationRequest` (`email`, `password`)
- **Réponse** : `AuthenticationResponse` (200 OK)
  - `accessToken` : JWT token
  - `refreshToken` : `null` (dans le body, envoyé via cookie HttpOnly)
  - Cookie `refreshToken` : HttpOnly, Secure, SameSite
- **Codes d'erreur** :
  - `401` : Identifiants invalides

#### POST `/api/v1/auth/refresh-token`
- **Description** : Rafraîchir le token d'accès
- **Autorisation** : Cookie `refreshToken` (HttpOnly)
- **Body** : Aucun (utilise le cookie)
- **Réponse** : `RefreshTokenResponse` (200 OK)
  - `accessToken` : Nouveau JWT token
- **Codes d'erreur** :
  - `403` : Refresh token invalide ou expiré (`TokenException`)

#### POST `/api/v1/auth/logout`
- **Description** : Déconnexion de l'utilisateur
- **Autorisation** : Cookie `refreshToken` (HttpOnly)
- **Body** : Aucun
- **Réponse** : `204 No Content`
  - Cookie `refreshToken` : Supprimé (expiré)

### Endpoints des Séjours (`/api/v1/sejours`)

#### GET `/api/v1/sejours`
- **Description** : Récupérer tous les séjours
- **Autorisation** : `ROLE_ADMIN`
- **Réponse** : `List<SejourDTO>` (200 OK)

#### GET `/api/v1/sejours/{id}`
- **Description** : Récupérer un séjour par son ID
- **Autorisation** : `ROLE_ADMIN` ou `ROLE_DIRECTION`
- **Path Variable** : `id` (int)
- **Réponse** : `SejourDTO` (200 OK)
- **Codes d'erreur** :
  - `404` : Séjour non trouvé

#### POST `/api/v1/sejours`
- **Description** : Créer un nouveau séjour
- **Autorisation** : `ROLE_ADMIN`
- **Body** : `CreateSejourRequest`
- **Réponse** : `SejourDTO` (200 OK)
- **Codes d'erreur** :
  - `400` : Validation échouée
  - `404` : Directeur non trouvé (si `directeurTokenId` fourni)

#### PUT `/api/v1/sejours/{id}`
- **Description** : Modifier un séjour existant
- **Autorisation** : `ROLE_ADMIN`
- **Path Variable** : `id` (int)
- **Body** : `CreateSejourRequest`
- **Réponse** : `SejourDTO` (200 OK)
- **Codes d'erreur** :
  - `400` : Validation échouée
  - `404` : Séjour ou directeur non trouvé

#### DELETE `/api/v1/sejours/{id}`
- **Description** : Supprimer un séjour
- **Autorisation** : `ROLE_ADMIN`
- **Path Variable** : `id` (int)
- **Réponse** : `204 No Content`
- **Codes d'erreur** :
  - `404` : Séjour non trouvé

#### GET `/api/v1/sejours/directeur/{directeurTokenId}`
- **Description** : Récupérer tous les séjours d'un directeur
- **Autorisation** : `ROLE_DIRECTION`
- **Path Variable** : `directeurTokenId` (string)
- **Réponse** : `List<SejourDTO>` (200 OK)
- **Codes d'erreur** :
  - `404` : Directeur non trouvé

#### POST `/api/v1/sejours/{id}/equipe/existant`
- **Description** : Ajouter un membre existant à l'équipe d'un séjour
- **Autorisation** : `ROLE_DIRECTION`
- **Path Variable** : `id` (int)
- **Body** : `MembreEquipeRequest` (`tokenId`, `roleSejour`)
- **Réponse** : `201 Created`
- **Codes d'erreur** :
  - `400` : Validation échouée
  - `404` : Séjour ou membre non trouvé
  - `409` : Membre déjà dans l'équipe (`ResourceAlreadyExistsException`)

#### POST `/api/v1/sejours/{id}/equipe/nouveau`
- **Description** : Ajouter un nouveau membre à l'équipe d'un séjour (création + ajout)
- **Autorisation** : `ROLE_DIRECTION`
- **Path Variable** : `id` (int)
- **Body** : `RegisterRequest` (complet avec email, password, etc.)
- **Réponse** : `201 Created`
- **Codes d'erreur** :
  - `400` : Validation échouée
  - `404` : Séjour non trouvé
  - `409` : Email déjà utilisé ou membre déjà dans l'équipe

#### PUT `/api/v1/sejours/{id}/equipe/{membreTokenId}`
- **Description** : Modifier le rôle d'un membre dans l'équipe d'un séjour
- **Autorisation** : `ROLE_DIRECTION`
- **Path Variables** : `id` (int), `membreTokenId` (string)
- **Body** : `MembreEquipeRequest` (`roleSejour`)
- **Réponse** : `204 No Content`
- **Codes d'erreur** :
  - `400` : Validation échouée
  - `404` : Séjour ou membre non trouvé

#### DELETE `/api/v1/sejours/{id}/equipe/{membreTokenId}`
- **Description** : Supprimer un membre de l'équipe d'un séjour
- **Autorisation** : `ROLE_DIRECTION`
- **Path Variables** : `id` (int), `membreTokenId` (string)
- **Réponse** : `204 No Content`
- **Codes d'erreur** :
  - `404` : Séjour ou membre non trouvé

#### GET `/api/v1/sejours/{id}/enfants`
- **Description** : Récupérer tous les enfants inscrits à un séjour
- **Autorisation** : `ROLE_DIRECTION`
- **Path Variable** : `id` (int) - ID du séjour
- **Réponse** : `List<EnfantDto>` (200 OK)
- **Codes d'erreur** :
  - `404` : Séjour non trouvé

#### GET `/api/v1/sejours/{id}/enfants/{enfantId}/dossier`
- **Description** : Récupérer le dossier d'un enfant (contacts parents, infos médicales, traitements, etc.)
- **Autorisation** : `ROLE_DIRECTION` (directeur du séjour ou membre de l'équipe)
- **Path Variables** : `id` (int) - ID du séjour, `enfantId` (int) - ID de l'enfant
- **Réponse** : `DossierEnfantDto` (200 OK)
- **Codes d'erreur** :
  - `403` : Utilisateur ne participant pas au séjour (`AccessDeniedException`)
  - `404` : Séjour non trouvé, enfant non inscrit au séjour, ou dossier non trouvé

#### PUT `/api/v1/sejours/{id}/enfants/{enfantId}/dossier`
- **Description** : Modifier le dossier d'un enfant (contacts parents, infos médicales, traitements, etc.)
- **Autorisation** : `ROLE_DIRECTION` (directeur du séjour ou membre de l'équipe)
- **Path Variables** : `id` (int) - ID du séjour, `enfantId` (int) - ID de l'enfant
- **Body** : `UpdateDossierEnfantRequest` (tous les champs optionnels : emailParent1/2, telephoneParent1/2, informationsMedicales, pai, informationsAlimentaires, traitements matin/midi/soir/si besoin, autresInformations, aPrendreEnSortie)
- **Réponse** : `DossierEnfantDto` (200 OK)
- **Codes d'erreur** :
  - `400` : Validation échouée (email ou téléphone invalide)
  - `403` : Utilisateur ne participant pas au séjour (`AccessDeniedException`)
  - `404` : Séjour non trouvé, enfant non inscrit au séjour, ou dossier non trouvé

#### POST `/api/v1/sejours/{id}/enfants`
- **Description** : Créer un nouvel enfant avec ses informations personnelles et l'ajouter directement à un séjour en une seule opération. 
  - **Logique de vérification** :
    1. Vérifie si l'enfant existe déjà en base de données (nom, prénom, genre, date de naissance)
    2. Si l'enfant existe déjà et qu'il est déjà dans le séjour → erreur 409 avec message formaté : "Eva AZZI née le 06/12/2015 existe déjà dans ce séjour"
    3. Si l'enfant existe déjà mais n'est pas dans le séjour → réutilise l'enfant existant et l'ajoute au séjour
    4. Si l'enfant n'existe pas → crée un nouvel enfant et l'ajoute au séjour
- **Autorisation** : `ROLE_DIRECTION`
- **Path Variable** : `id` (int) - ID du séjour
- **Body** : `CreateEnfantRequest` contenant :
  - Informations de l'enfant : `nom`, `prenom`, `genre`, `dateNaissance`, `niveauScolaire` (tous obligatoires)
- **Réponse** : `201 Created` (pas de body)
- **Codes d'erreur** :
  - `400` : Validation échouée (champs invalides ou manquants)
  - `404` : Séjour non trouvé
  - `409` : Enfant déjà inscrit au séjour (`ResourceAlreadyExistsException`) avec message formaté incluant le nom, prénom, genre (né/née) et date formatée

#### PUT `/api/v1/sejours/{id}/enfants/{enfantId}`
- **Description** : Modifier les informations d'un enfant inscrit à un séjour
  - **Logique de vérification** :
    1. Vérifie si un autre enfant avec les nouvelles informations existe déjà en base de données
    2. Si l'enfant existant est déjà dans le séjour → erreur 409 avec message formaté
    3. Si l'enfant existant n'est pas dans le séjour → remplace la relation SejourEnfant (supprime l'ancienne, crée une nouvelle) et supprime l'ancien enfant s'il n'est plus dans aucun séjour
    4. Si aucun autre enfant n'existe → modifie l'enfant actuel avec les nouvelles informations
- **Autorisation** : `ROLE_DIRECTION`
- **Path Variables** : `id` (int) - ID du séjour, `enfantId` (int) - ID de l'enfant
- **Body** : `CreateEnfantRequest` contenant :
  - Informations de l'enfant : `nom`, `prenom`, `genre`, `dateNaissance`, `niveauScolaire` (tous obligatoires)
- **Réponse** : `EnfantDto` (200 OK)
- **Codes d'erreur** :
  - `400` : Validation échouée (champs invalides ou manquants)
  - `404` : Séjour non trouvé ou enfant non inscrit au séjour
  - `409` : Un enfant avec ces informations existe déjà dans ce séjour (`ResourceAlreadyExistsException`)

#### DELETE `/api/v1/sejours/{id}/enfants/{enfantId}`
- **Description** : Retirer un enfant d'un séjour
- **Autorisation** : `ROLE_DIRECTION`
- **Path Variables** : `id` (int), `enfantId` (int)
- **Réponse** : `204 No Content`
- **Codes d'erreur** :
  - `404` : Séjour non trouvé ou enfant non inscrit au séjour

#### DELETE `/api/v1/sejours/{id}/enfants/all`
- **Description** : Retirer tous les enfants d'un séjour. Les enfants qui ne sont inscrits à aucun autre séjour seront automatiquement supprimés de la base de données.
- **Autorisation** : `ROLE_DIRECTION`
- **Path Variable** : `id` (int) - ID du séjour
- **Réponse** : `204 No Content`
- **Codes d'erreur** :
  - `404` : Séjour non trouvé

#### GET `/api/v1/sejours/{sejourId}/enfants/import/spec`
- **Description** : Récupérer la spécification d'import Excel (notice pour le frontend : colonnes obligatoires, colonnes optionnelles, noms possibles pour chaque colonne).
- **Autorisation** : `ROLE_DIRECTION`
- **Path Variable** : `sejourId` (int) - ID du séjour
- **Réponse** : `ExcelImportSpecResponse` (200 OK) contenant :
  - `colonnesObligatoires` : Liste de `ExcelImportColumnSpec` (champ, libelle, motsCles, obligatoire=true)
  - `colonnesOptionnelles` : Liste de `ExcelImportColumnSpec` (champ, libelle, motsCles, obligatoire=false)
  - `formatsAcceptes` : `[".xlsx", ".xls"]`
- **Usage** : Le frontend peut appeler cet endpoint pour afficher la notice d'import avant ou pendant l'upload du fichier Excel.

#### POST `/api/v1/sejours/{id}/enfants/import`
- **Description** : Importer plusieurs enfants depuis un fichier Excel et les ajouter à un séjour. La spécification des colonnes est centralisée dans `ExcelImportSpec` (voir endpoint GET `/import/spec` pour la notice complète).
- **Autorisation** : `ROLE_DIRECTION`
- **Path Variable** : `id` (int) - ID du séjour
- **Body** : `multipart/form-data` avec un champ `file` contenant le fichier Excel (.xlsx ou .xls)
- **Réponse** : `ExcelImportResponse` (200 OK) contenant :
  - `totalLignes` : Nombre total de lignes traitées (lignes vides ignorées)
  - `enfantsCrees` : Nombre d'enfants créés avec succès
  - `enfantsDejaExistants` : Nombre d'enfants déjà inscrits au séjour
  - `erreurs` : Nombre d'erreurs rencontrées
  - `messagesErreur` : Liste des messages d'erreur détaillés (une par ligne en erreur) avec message général expliquant la structure attendue si colonnes manquantes
- **Codes d'erreur** :
  - `400` : Fichier vide, format invalide (pas Excel), colonnes requises manquantes, ou erreurs de validation dans les données
  - `404` : Séjour non trouvé
- **Format Excel attendu** :
  - **Détection des colonnes par groupes (ET/OU)** : Chaque colonne a des groupes de mots-clés. L'en-tête doit contenir **tous** les groupes (ET), avec au moins un mot par groupe (OU). Normalisation : suppression accents, espaces, casse.
    - **Colonnes simples** (1 groupe) : Nom (nom), Prénom (prenom), Genre (genre/sexe), Date de naissance (datenaissance/naissance), Niveau scolaire (niveau/classe)
    - **Colonnes parent 1/2** : emailParent1 = (email ou mail) ET parent ET 1 ; telephoneParent1 = (telephone ou tel) ET parent ET 1 ; idem pour parent 2 avec "2"
    - **Colonnes traitements** : traitementMatin = (traitement ou medicament) ET matin ; idem pour midi, soir, si besoin
  - **Colonnes optionnelles (dossier)** : emailParent1, telephoneParent1, emailParent2, telephoneParent2, informationsMedicales, pai, informationsAlimentaires, traitementMatin, traitementMidi, traitementSoir, traitementSiBesoin, autresInformations, aPrendreEnSortie
  - **Valeurs acceptées** :
    - **Genre** : `Masculin`, `Féminin`, `Garçon`, `Fille` (conversion automatique : Garçon → Masculin, Fille → Féminin)
    - **Date de naissance** : formats acceptés : `dd/MM/yyyy`, `yyyy-MM-dd`, ou format Excel numérique
    - **Niveau scolaire** : valeurs de l'enum `NiveauScolaire` en majuscules (`PS`, `MS`, `GS`, `CP`, `CE1`, `CE2`, `CM1`, `CM2`, `SIXIEME`, `CINQUIEME`, `QUATRIEME`, `TROISIEME`, `DEUXIEME`, `PREMIERE`, `TERMINALE`)
  - La première ligne doit contenir les en-têtes
  - Les lignes de données commencent à la ligne 2
  - **Lignes vides** : Les lignes vides sont automatiquement ignorées (ne comptent pas dans `totalLignes`)

### Endpoints des Groupes (`/api/v1/sejours/{sejourId}/groupes`)

**Autorisation** : `ROLE_DIRECTION` pour toutes les opérations. Documentation détaillée : `docs/frontend-creation-groupes.md`.

#### GET `/api/v1/sejours/{sejourId}/groupes`
- **Description** : Récupérer tous les groupes d'un séjour
- **Réponse** : `List<GroupeDto>` (200 OK)
- **Codes d'erreur** : `404` : Séjour non trouvé

#### GET `/api/v1/sejours/{sejourId}/groupes/{groupeId}`
- **Description** : Récupérer un groupe par son ID
- **Réponse** : `GroupeDto` (200 OK)
- **Codes d'erreur** : `404` : Séjour ou groupe non trouvé

#### POST `/api/v1/sejours/{sejourId}/groupes`
- **Description** : Créer un groupe. Pour `AGE` : ageMin/ageMax obligatoires, enfants du séjour dans la tranche ajoutés automatiquement. Pour `NIVEAU_SCOLAIRE` : niveauScolaireMin/Max obligatoires. Pour `THEMATIQUE` : aucune tranche.
- **Body** : `CreateGroupeRequest`
- **Réponse** : `GroupeDto` (201 Created)
- **Codes d'erreur** : `400` : Validation échouée, `404` : Séjour non trouvé

#### PUT `/api/v1/sejours/{sejourId}/groupes/{groupeId}`
- **Description** : Modifier un groupe
- **Body** : `CreateGroupeRequest`
- **Réponse** : `GroupeDto` (200 OK)
- **Codes d'erreur** : `400` : Validation échouée, `404` : Séjour ou groupe non trouvé

#### DELETE `/api/v1/sejours/{sejourId}/groupes/{groupeId}`
- **Description** : Supprimer un groupe
- **Réponse** : `204 No Content`
- **Codes d'erreur** : `404` : Séjour ou groupe non trouvé

#### POST `/api/v1/sejours/{sejourId}/groupes/{groupeId}/enfants/{enfantId}`
- **Description** : Ajouter un enfant au groupe
- **Réponse** : `204 No Content`
- **Codes d'erreur** : `404` : Séjour, groupe ou enfant non trouvé, enfant non inscrit au séjour

#### DELETE `/api/v1/sejours/{sejourId}/groupes/{groupeId}/enfants/{enfantId}`
- **Description** : Retirer un enfant du groupe
- **Réponse** : `204 No Content`
- **Codes d'erreur** : `404` : Séjour, groupe ou enfant non trouvé

#### POST `/api/v1/sejours/{sejourId}/groupes/{groupeId}/referents`
- **Description** : Ajouter un référent au groupe
- **Body** : `AjouterReferentRequest` (`tokenId`)
- **Réponse** : `201 Created` (corps vide)
- **Codes d'erreur** : `404` : Séjour, groupe ou référent non trouvé

#### DELETE `/api/v1/sejours/{sejourId}/groupes/{groupeId}/referents/{referentTokenId}`
- **Description** : Retirer un référent du groupe
- **Réponse** : `204 No Content`
- **Codes d'erreur** : `404` : Séjour, groupe ou référent non trouvé

### Endpoints des Lieux (`/api/v1/sejours/{sejourId}/lieux`)

**Autorisation** : `ROLE_DIRECTION` pour toutes les opérations.

#### GET `/api/v1/sejours/{sejourId}/lieux`
- **Description** : Lister les lieux du séjour
- **Réponse** : `List<LieuDto>` (200 OK) — inclut **`partageableEntreAnimateurs`**, **`nombreMaxActivitesSimultanees`**
- **Codes d'erreur** : `404` : Séjour non trouvé

#### GET `/api/v1/sejours/{sejourId}/lieux/{lieuId}`
- **Description** : Détail d'un lieu
- **Réponse** : `LieuDto` (200 OK)
- **Codes d'erreur** : `404` : Séjour ou lieu non trouvé, ou lieu d'un autre séjour

#### POST `/api/v1/sejours/{sejourId}/lieux`
- **Description** : Créer un lieu pour le séjour
- **Body** : `SaveLieuRequest` (`nom`, `emplacement`, `nombreMax` optionnel `@Positive`, **`partageableEntreAnimateurs`**, **`nombreMaxActivitesSimultanees`** : obligatoire et **≥ 2** si partage ; **`null`** si pas de partage)
- **Réponse** : `LieuDto` (201 Created)
- **Codes d'erreur** : `400` : validation Jakarta ou règles partage (`IllegalArgumentException`), `404` : séjour, `409` : nom déjà utilisé (casse ignorée)

#### PUT `/api/v1/sejours/{sejourId}/lieux/{lieuId}`
- **Description** : Modifier un lieu
- **Body** : `SaveLieuRequest`
- **Réponse** : `LieuDto` (200 OK)
- **Codes d'erreur** : `400` : validation, `404` : Séjour ou lieu non trouvé / mauvais séjour, `409` : nouveau nom déjà pris par un autre lieu du même séjour

#### DELETE `/api/v1/sejours/{sejourId}/lieux/{lieuId}`
- **Description** : Supprimer un lieu
- **Réponse** : `204 No Content`
- **Codes d'erreur** : `404` : Séjour ou lieu non trouvé / mauvais séjour

### Endpoints des Horaires (`/api/v1/sejours/{sejourId}/horaires`)

**Autorisation** : `ROLE_DIRECTION` pour toutes les opérations.

#### GET `/api/v1/sejours/{sejourId}/horaires`
- **Description** : Lister les horaires du séjour (**tri par `id` croissant**)
- **Réponse** : `List<HoraireDto>` (200 OK) — champs `id`, `libelle`, `sejourId`
- **Codes d'erreur** : `404` : Séjour non trouvé

#### GET `/api/v1/sejours/{sejourId}/horaires/{horaireId}`
- **Description** : Détail d’un horaire
- **Réponse** : `HoraireDto` (200 OK)
- **Codes d'erreur** : `404` : Séjour inexistant ou horaire absent / pas pour ce séjour (résolution via **`findByIdAndSejourId`**)

#### POST `/api/v1/sejours/{sejourId}/horaires`
- **Description** : Créer un horaire pour le séjour
- **Body** : `SaveHoraireRequest` (`libelle`, format **`6h00`** … **`18h30`**, validation **`Horaire.LIBELLE_HORAIRE_PATTERN`**)
- **Réponse** : `HoraireDto` (201 Created)
- **Codes d'erreur** : `400` validation Jakarta, `404` séjour, `409` libellé déjà utilisé pour ce séjour (**casse ignorée**)

#### PUT `/api/v1/sejours/{sejourId}/horaires/{horaireId}`
- **Description** : Modifier un horaire
- **Body** : `SaveHoraireRequest`
- **Réponse** : `HoraireDto` (200 OK)
- **Codes d'erreur** : `400` validation, `404` séjour ou horaire, `409` nouveau libellé en conflit avec un autre horaire du même séjour

#### DELETE `/api/v1/sejours/{sejourId}/horaires/{horaireId}`
- **Description** : Supprimer un horaire (aucune garde liée aux activités pour l’instant)
- **Réponse** : `204 No Content`
- **Codes d'erreur** : `404` : Séjour ou horaire non trouvé / mauvais séjour

### Endpoints des Moments (`/api/v1/sejours/{sejourId}/moments`)

**Autorisation** : `ROLE_DIRECTION` pour toutes les opérations.

#### GET `/api/v1/sejours/{sejourId}/moments`
- **Description** : Lister les moments du séjour (**tri chronologique** : `COALESCE(ordre, id)` croissant, puis `id`)
- **Réponse** : `List<MomentDto>` (200 OK) — champs `id`, `nom`, `sejourId`, **`ordre`** (si `ordre` null en base, valeur renvoyée = `id` pour cohérence d’affichage)
- **Codes d'erreur** : `404` : Séjour non trouvé

#### GET `/api/v1/sejours/{sejourId}/moments/{momentId}`
- **Description** : Détail d’un moment
- **Réponse** : `MomentDto` (200 OK) — idem champs dont **`ordre`**
- **Codes d'erreur** : `404` : Séjour ou moment non trouvé / moment d’un autre séjour

#### POST `/api/v1/sejours/{sejourId}/moments`
- **Description** : Créer un moment pour le séjour (**`ordre` calculé côté serveur** : placé après les moments existants, voir mémo « Dernières modifications »)
- **Body** : `SaveMomentRequest` (`nom`, `@NotBlank`, `@Size(max=200)`) — pas d’`ordre` dans le body
- **Réponse** : `MomentDto` (201 Created)
- **Codes d'erreur** : `400` validation, `404` séjour, `409` nom déjà utilisé pour ce séjour (casse ignorée)

#### PUT `/api/v1/sejours/{sejourId}/moments/reorder`
- **Description** : Réordonner **tous** les moments du séjour en une fois (ex. après drag-and-drop)
- **Body** : `ReorderMomentsRequest` — **`momentIds`** : liste **complète**, **sans doublon**, exactement les ids des moments de ce séjour, dans le **nouvel ordre** (index persisté : 0 … n−1)
- **Réponse** : `List<MomentDto>` (200 OK), déjà triée comme le GET liste
- **Codes d'erreur** : `400` : liste invalide (`IllegalArgumentException` — taille, doublons, id inconnu / manquant) ; `404` séjour

#### PUT `/api/v1/sejours/{sejourId}/moments/{momentId}`
- **Description** : Modifier un moment
- **Body** : `SaveMomentRequest`
- **Réponse** : `MomentDto` (200 OK)
- **Codes d'erreur** : `400` validation, `404` séjour ou moment, `409` nouveau nom en conflit

#### DELETE `/api/v1/sejours/{sejourId}/moments/{momentId}`
- **Description** : Supprimer un moment (impossible si des activités y sont rattachées)
- **Réponse** : `204 No Content`
- **Codes d'erreur** : `404` séjour ou moment, `400` si activités existantes (`IllegalArgumentException`)

### Endpoints des Activités (`/api/v1/sejours/{sejourId}/activites`)

**Autorisation** : `ROLE_DIRECTION` pour toutes les opérations.

#### GET `/api/v1/sejours/{sejourId}/activites`
- **Description** : Lister les activités du séjour (tri date croissante puis id)
- **Réponse** : `List<ActiviteDto>` (200 OK) — **`moment`** et **`typeActivite`** toujours renseignés pour des activités en base cohérentes ; **`lieu`** si affecté, sinon `null` ; **`avertissementLieu`** toujours **`null`** (réservé aux réponses POST/PUT après création ou mise à jour)
- **Codes d'erreur** : `404` : Séjour non trouvé

#### GET `/api/v1/sejours/{sejourId}/activites/{activiteId}`
- **Description** : Détail d'une activité
- **Réponse** : `ActiviteDto` (200 OK) — même principe que la liste : **`moment`**, **`typeActivite`**, **`lieu`** issus de l’entité ; **`avertissementLieu`** **`null`** en GET
- **Codes d'erreur** : `404` : Séjour ou activité non trouvé

#### POST `/api/v1/sejours/{sejourId}/activites`
- **Description** : Créer une activité
- **Body** :

`CreateActiviteRequest` (`date`, `nom`, `description` optionnelle, **`lieuId` optionnel**, **`momentId`** — obligatoire côté service si au moins un moment existe pour le séjour ; si **aucun moment** → **400** avec consigne de faire créer des moments par la direction, **`typeActiviteId` obligatoire** (`@NotNull`), `membreTokenIds`, **`groupeIds`**). **Partage lieu** : cohérence **jour + moment + lieu** (voir `ActiviteRepository`).
- **Réponse** : `ActiviteDto` (201 Created) — **`moment`**, **`typeActivite`**, `groupeIds`, **`lieu`**, éventuellement **`avertissementLieu`** si le lieu était déjà occupé **ce jour et ce moment** mais le partage le permet
- **Codes d'erreur** : `400` : validation Jakarta (dont **`typeActiviteId`** manquant), date / équipe / groupe / **moments** (aucun moment, moment obligatoire manquant), **lieu déjà pris** ou **limite de partage** ; `404` : séjour, membre, groupe, lieu, **moment**, **type d’activité** (**id inconnu** ou **pas pour ce séjour**) ; `500` théorique si lieu partageable sans max en base

#### PUT `/api/v1/sejours/{sejourId}/activites/{activiteId}`
- **Description** : Modifier une activité
- **Body** : `UpdateActiviteRequest` (comme la création ; **`lieuId` null** retire le lieu ; **`typeActiviteId` obligatoire** pour pointer vers un type du séjour — pas de retrait du type ; **`momentId`** requis selon les mêmes règles que POST)
- **Réponse** : `ActiviteDto` (200 OK), **`avertissementLieu`** possible comme en POST
- **Codes d'erreur** : `400` / `404` comme POST (comptage lieu **exclut** l’activité modifiée)

#### DELETE `/api/v1/sejours/{sejourId}/activites/{activiteId}`
- **Description** : Supprimer une activité
- **Réponse** : `204 No Content`
- **Codes d'erreur** : `404` : Séjour ou activité non trouvé

### Endpoints des types d’activité (`/api/v1/sejours/{sejourId}/types-activite`)

**Par séjour** (même espace que lieux / moments). **Autorisation** : `ROLE_DIRECTION` pour toutes les opérations.

À la **création d’un séjour** et au **démarrage** de l’appli pour les séjours existants : les six types par défaut (**`TypeActiviteLibellesParDefaut`**) sont assurés pour le séjour (`predefini = true`, non modifiables / non supprimables).

#### GET `/api/v1/sejours/{sejourId}/types-activite`
- **Description** : Lister les types du séjour, tri par **`libelle`** croissant
- **Réponse** : `List<TypeActiviteDto>` (200 OK) — chaque élément inclut **`sejourId`**, **`predefini`**
- **Codes d'erreur** : `404` : séjour inexistant

#### GET `/api/v1/sejours/{sejourId}/types-activite/{id}`
- **Description** : Détail d’un type **de ce séjour**
- **Réponse** : `TypeActiviteDto` (200 OK)
- **Codes d'erreur** : `404` : séjour ou type inexistant / type d’un autre séjour

#### POST `/api/v1/sejours/{sejourId}/types-activite`
- **Description** : Créer un type **pour ce séjour** (`predefini = false`)
- **Body** : `SaveTypeActiviteRequest` (`libelle` `@NotBlank`, max 100 car.)
- **Réponse** : `TypeActiviteDto` (201 Created)
- **Codes d'erreur** : `400` : validation ou libellé déjà pris **pour ce séjour** (**insensible à la casse**, après `trim`) ; `404` : séjour

#### PUT `/api/v1/sejours/{sejourId}/types-activite/{id}`
- **Description** : Modifier le libellé (refus si **`predefini`**)
- **Body** : `SaveTypeActiviteRequest`
- **Réponse** : `TypeActiviteDto` (200 OK)
- **Codes d'erreur** : `400` : validation, doublon de libellé, ou type **prédéfini** ; `404` : séjour ou type

#### DELETE `/api/v1/sejours/{sejourId}/types-activite/{id}`
- **Description** : Supprimer un type (refus si **`predefini`** ou si des **`Activite`** le référencent)
- **Réponse** : `204 No Content`
- **Codes d'erreur** : `400` : type prédéfini ou encore utilisé ; `404` : séjour ou type

### Endpoints des Utilisateurs (`/api/v1/utilisateurs`)

#### GET `/api/v1/utilisateurs`
- **Description** : Récupérer tous les utilisateurs
- **Autorisation** : `GESTION_UTILISATEURS` (privilege)
- **Réponse** : `List<ProfilUtilisateurDTO>` (200 OK)

#### GET `/api/v1/utilisateurs/{role}`
- **Description** : Récupérer tous les utilisateurs par rôle
- **Autorisation** : `GESTION_UTILISATEURS` (privilege)
- **Path Variable** : `role` (enum: `ADMIN`, `DIRECTION`, `USER`)
- **Réponse** : `List<ProfilUtilisateurDTO>` (200 OK)

#### GET `/api/v1/utilisateurs/search?email={email}`
- **Description** : Rechercher un utilisateur par email
- **Autorisation** : `ROLE_DIRECTION` ou `ROLE_ADMIN`
- **Query Parameter** : `email` (string)
- **Réponse** : `ProfilUtilisateurDTO` (200 OK)
- **Codes d'erreur** :
  - `400` : Utilisateur est ADMIN ou DIRECTION (ne peut pas être ajouté)
  - `404` : Utilisateur non trouvé

#### GET `/api/v1/utilisateurs/profil?tokenId={tokenId}`
- **Description** : Récupérer le profil d'un utilisateur par son tokenId
- **Autorisation** : Aucune (endpoint public)
- **Query Parameter** : `tokenId` (string)
- **Réponse** : `ProfilUtilisateurDTO` (200 OK)
- **Codes d'erreur** :
  - `404` : Utilisateur non trouvé

#### PUT `/api/v1/utilisateurs`
- **Description** : Modifier un utilisateur (soi-même ou par admin)
- **Autorisation** : Utilisateur connecté (modification de soi) ou `GESTION_UTILISATEURS` (modification par admin)
- **Body** : `UpdateUserRequest`
- **Réponse** : `ProfilUtilisateurDTO` (200 OK)
- **Codes d'erreur** :
  - `400` : Validation échouée
  - `404` : Utilisateur non trouvé

#### DELETE `/api/v1/utilisateurs/{tokenId}`
- **Description** : Supprimer un utilisateur
- **Autorisation** : `GESTION_UTILISATEURS` (privilege)
- **Path Variable** : `tokenId` (string)
- **Réponse** : `204 No Content`
- **Codes d'erreur** :
  - `404` : Utilisateur non trouvé

#### PATCH `/api/v1/utilisateurs/mot-de-passe`
- **Description** : Changer le mot de passe d'un utilisateur
- **Autorisation** : Utilisateur connecté (modification de soi) ou `GESTION_UTILISATEURS` (modification par admin)
- **Body** : `ChangePasswordRequest`
  - `tokenId` : Identifiant de l'utilisateur
  - `ancienMotDePasse` : Obligatoire si l'utilisateur modifie son propre mot de passe
  - `nouveauMotDePasse` : Nouveau mot de passe
- **Réponse** : `200 OK` avec `{"message": "Mot de passe modifié avec succès"}`
- **Codes d'erreur** :
  - `400` : Validation échouée ou ancien mot de passe manquant (pour utilisateur non-admin)
  - `403` : Tentative de modification du mot de passe d'un autre utilisateur (pour utilisateur non-admin)
  - `404` : Utilisateur non trouvé

### Gestion des Erreurs

#### Format des Erreurs

**Erreurs de Validation (400)** :
```json
{
  "fieldName1": "Message d'erreur 1",
  "fieldName2": "Message d'erreur 2"
}
```

**Erreurs Génériques (400, 404, 409)** :
```json
{
  "error": "Message d'erreur descriptif"
}
```

**Erreurs Token (403)** :
```json
{
  "status": 403,
  "error": "Invalid Token",
  "timestamp": "2024-01-01T12:00:00Z",
  "message": "Message d'erreur",
  "path": "/api/v1/auth/refresh-token"
}
```

#### Codes d'Erreur HTTP

- **200 OK** : Requête réussie
- **201 Created** : Ressource créée avec succès
- **204 No Content** : Opération réussie sans contenu à retourner
- **400 Bad Request** : Requête invalide (validation, argument illégal, exception utilisateur)
- **401 Unauthorized** : Authentification requise (non authentifié)
- **403 Forbidden** : Accès refusé (autorisation insuffisante) ou token invalide/expiré (`TokenException`)
- **404 Not Found** : Ressource non trouvée
- **409 Conflict** : Ressource déjà existante (email utilisé, membre déjà dans équipe, enfant déjà inscrit au séjour)

### Types de Données

Tous les types TypeScript sont définis dans `enjoyWebApp/src/types/api.d.ts` :
- `SejourDTO`
- `ProfilUtilisateurDTO`
- `EnfantDto` (note: 'd' minuscule pour correspondre au nom Java `EnfantDto`)
- `DossierEnfantDto`
- `GroupeDto`, `CreateGroupeRequest`, `AjouterReferentRequest`
- `LieuDto`, `SaveLieuRequest`, `EmplacementLieu` (enum API — à ajouter dans `api.d.ts` si le frontend gère les lieux)
- `HoraireDto`, `SaveHoraireRequest` (à ajouter dans `api.d.ts` si le frontend gère les horaires)
- `MomentDto` (**`ordre`**), `SaveMomentRequest`, `ReorderMomentsRequest` (**`momentIds`**)
- `ActiviteDto` (**`moment`**, **`lieu`**, **`typeActivite`**, **`avertissementLieu`**, `groupeIds`), `CreateActiviteRequest`, `UpdateActiviteRequest` (**`typeActiviteId`** obligatoire)
- `TypeActiviteDto` (**`sejourId`**, **`predefini`**), `SaveTypeActiviteRequest`
- `CreateSejourRequest`
- `CreateEnfantRequest`
- `UpdateDossierEnfantRequest`
- `MembreEquipeRequest`
- `RegisterRequest`
- `UpdateUserRequest`
- `AuthenticationRequest`
- `AuthenticationResponse`
- `RefreshTokenResponse`
- `ExcelImportResponse`
- `ExcelImportSpecResponse` (colonnesObligatoires, colonnesOptionnelles, formatsAcceptes)
- `ExcelImportColumnSpec` (champ, libelle, motsCles, obligatoire)
- `ErrorResponse`

**Note** : Les dates Java (`Date`, `Instant`) sont sérialisées en ISO 8601 et typées comme `string` en TypeScript.

## Roadmap

### Complété ✅
- [x] Créer un fichier de types TypeScript synchronisé avec les DTOs Java (`api.d.ts`)
- [x] Utiliser progressivement les types de `api.d.ts` dans les nouveaux fichiers frontend
- [x] Migrer progressivement les types locaux existants vers `api.d.ts` (séjours et équipes migrés)
- [x] Refactoriser tous les composants pour utiliser l'injection par constructeur avec `@RequiredArgsConstructor`
- [x] Créer une exception `ResourceNotFoundException` pour remplacer les `RuntimeException` génériques
- [x] Intégrer `ResourceNotFoundException` dans `GlobalExceptionHandler` (HTTP 404)
- [x] Remplacer toutes les `RuntimeException` par `ResourceNotFoundException` dans `SejourServiceImpl` (16 occurrences)
- [x] Ajouter des tests unitaires pour `SejourServiceImpl` (19 tests couvrant tous les cas d'usage)
- [x] Ajouter des tests unitaires pour `UtilisateurServiceImpl` (33 tests couvrant tous les cas d'usage, cas limites et cas d'erreur)
- [x] Mettre à jour les tests `SejourServiceImplTest` pour utiliser `ResourceNotFoundException` au lieu de `RuntimeException` dans les assertions ✅
- [x] Remplacer `RuntimeException` par `ResourceNotFoundException` dans `UtilisateurController` ✅
- [x] Nettoyer les `try-catch` dans `UtilisateurServiceImpl` qui retournent `null` au lieu de laisser les exceptions remonter ✅
- [x] Mettre à jour les tests `UtilisateurServiceImplTest` pour vérifier les exceptions au lieu de `null` ✅
- [x] Ajouter des tests unitaires pour `AuthenticationServiceImpl` (7 tests couvrant tous les cas d'usage, cas limites et cas d'erreur) ✅
- [x] Améliorer `AuthenticationServiceImpl` : utilisation de `ResourceNotFoundException` au lieu de `IllegalArgumentException` pour les cas "utilisateur non trouvé" et "refreshToken introuvable" ✅
- [x] Ajouter des tests unitaires pour `JwtServiceImpl` (7 tests couvrant tous les cas d'usage, cas limites et cas d'erreur) ✅
- [x] Ajouter des tests unitaires pour `RefreshTokenServiceImpl` (18 tests couvrant tous les cas d'usage, cas limites et cas d'erreur) ✅
- [x] Nettoyer le `try-catch` dans `AuthenticationController` - suppression du `try-catch` inutile qui attrapait `ValidationException` (jamais lancée) et suppression du `System.out.println` de debug ✅
- [x] Ajouter des tests unitaires pour `AuthenticationController` (9 tests couvrant tous les cas d'usage, cas limites et cas d'erreur) ✅
- [x] Ajouter une règle dans `.cursorrules` pour utiliser `@InjectMocks` dans les tests de contrôleurs ✅
- [x] Ajouter des tests unitaires pour `UtilisateurController` (18 tests couvrant tous les cas d'usage, cas limites et cas d'erreur) ✅
- [x] Améliorer `SejourServiceImplTest` : toutes les assertions utilisent `ResourceNotFoundException`, tests complets pour tous les cas d'usage (19 tests) ✅
- [x] Ajouter des tests unitaires pour `SejourController` (27 tests couvrant tous les cas d'usage, cas limites et cas d'erreur) ✅
- [x] Ajouter des tests unitaires pour `GlobalExceptionHandler` (13 tests couvrant tous les handlers d'exceptions) ✅
- [x] Ajouter des tests unitaires pour `TokenControllerHandler` (5 tests couvrant tous les cas d'usage, vérification du format ErrorResponse structuré) ✅
- [x] Ajouter des tests de conversion DTO pour `mapToDTO()` dans `SejourServiceImpl` (6 tests couvrant tous les cas : avec/sans directeur, avec/sans équipe, vérification de tous les champs) ✅
- [x] Ajouter des tests unitaires pour `JwtAuthenticationFilter` (11 tests couvrant tous les cas d'usage : extraction token, authentification réussie, rejet avec token invalide/expiré, pas d'authentification si pas de token) ✅
- [x] Implémenter la gestion des enfants dans les séjours : création des entités `Enfant`, `SejourEnfant`, `SejourEnfantId`, repositories `EnfantRepository` et `SejourEnfantRepository`, DTO `EnfantDto` et `CreateEnfantRequest`, méthodes dans `EnfantService` (`creerEtAjouterEnfantAuSejour`, `supprimerEnfantDuSejour`, `getEnfantsDuSejour`, `modifierEnfant`), endpoints REST (`GET /api/v1/sejours/{id}/enfants`, `POST /api/v1/sejours/{id}/enfants`, `PUT /api/v1/sejours/{id}/enfants/{enfantId}`, `DELETE /api/v1/sejours/{id}/enfants/{enfantId}`). **Note importante** : Un enfant peut exister indépendamment et être réutilisé dans plusieurs séjours. L'entité `Enfant` contient uniquement les informations personnelles de l'enfant (nom, prénom, genre, date de naissance, niveau scolaire), sans les informations des parents. ✅
- [x] Simplifier l'entité `Enfant` : suppression des champs des parents (nomParent1, prenomParent1, telephoneParent1, emailParent1, nomParent2, prenomParent2, telephoneParent2, emailParent2) de l'entité `Enfant`, du DTO `EnfantDto` et du payload `CreateEnfantRequest`. Mise à jour de `EnfantServiceImpl` pour refléter cette simplification. L'entité `Enfant` contient maintenant uniquement les informations personnelles de l'enfant. ✅
- [x] Implémenter la logique de vérification d'existence d'enfants : méthode `findByNomAndPrenomAndGenreAndDateNaissance` dans `EnfantRepository` pour identifier un enfant de manière unique. Lors de la création, réutilisation de l'enfant existant s'il existe déjà. Lors de la modification, remplacement de la relation SejourEnfant si les nouvelles informations correspondent à un autre enfant existant. ✅
- [x] Améliorer l'import Excel : détection flexible des colonnes basée sur les mots-clés (normalisation des noms), gestion des lignes vides, support de "fille"/"garçon" en plus de "Masculin"/"Féminin" pour le genre, messages d'erreur explicites avec formatage des dates. ✅
- [x] Configurer les enums pour un stockage lisible : ajout de `@Enumerated(EnumType.STRING)` pour `Genre` et `NiveauScolaire` dans l'entité `Enfant` pour stocker les valeurs comme chaînes de caractères ("Masculin", "Féminin", "PS", "MS", etc.) au lieu d'entiers. ✅
- [x] Améliorer les messages d'erreur : formatage des dates en dd/MM/yyyy dans les messages d'erreur, messages plus explicites pour la structure attendue des fichiers Excel, gestion du genre (né/née) dans les messages. ✅
- [x] Implémenter l'interface frontend complète pour la gestion des enfants : création des composants `AddEnfantForm`, `ListeEnfants`, `ImportExcelEnfants`, intégration dans `DetailsSejour`, ajout des services dans `sejour.service.ts` pour tous les endpoints enfants, utilisation des types centralisés de `api.d.ts`. ✅
- [x] Ajouter des tests unitaires pour `EnfantController` (19 tests couvrant les endpoints REST enfants + import Excel ; pas encore GET/PUT dossier) ✅
- [x] Ajouter des tests unitaires pour `ExcelHelper` (35 tests : detectColumns, normalizeColumnName, containsKeyword, containsAllGroups, isRowEmpty, getCellValueAsString, parseDateFromString, formatDate) ✅
- [x] Entité `DossierEnfant` : relation OneToOne avec `Enfant` pour les informations de dossier (contacts parents, médical, traitements). `DossierEnfantRepository`, `DossierEnfantDto`. Création automatique à la création d'un enfant. Import Excel avec colonnes optionnelles du dossier. `ExcelHelper.normalizePhone()` pour normalisation des téléphones. ✅
- [x] Entité `Groupe` : `GroupeService`/`GroupeServiceImpl`, `GroupeController`, CRUD + gestion enfants/référents. Types `THEMATIQUE`, `AGE`, `NIVEAU_SCOLAIRE`. Relations `@ManyToMany` avec `Enfant` et `Utilisateur`. `docs/frontend-creation-groupes.md`. ✅
- [x] Entité **`Moment`** + CRUD direction, **`ordre`** + tri chronologique + **`PUT .../moments/reorder`** (`ReorderMomentsRequest`), unicité nom par séjour, suppression bloquée si activités liées ; entité `Activite` : **moment obligatoire**, **type d’activité obligatoire**, lieu optionnel, **occupation lieu par jour + moment** (partage / limite / **`avertissementLieu`**), `ActiviteServiceImplTest` (**22**), `ActiviteControllerTest` (2). **Conflit animateur** (même créneau) : **`ConflitPlanningAnimateurException`**, code **`ANIMATEUR_DEJA_AFFECTE_CRENEAU`**. ⚠️ Aligner **`api.d.ts`** (front) sur **`moment` / `momentId` / `moment.ordre`**, **`reorder`**, **`typeActivite` / `typeActiviteId`**, **erreur 400** avec **`code`** pour conflit animateur. ✅
- [x] Entité `Lieu` + CRUD : `EmplacementLieu`, **partage entre activités** (`partageableEntreAnimateurs`, `nombreMaxActivitesSimultanees`), `SaveLieuRequest` / `LieuDto`, `LieuService`/`LieuServiceImpl`, `LieuController`, `LieuControllerTest` (7), `LieuServiceImplTest` (**8**). ✅
- [x] **`TypeActivite`** (**par séjour**, unicité libellé par séjour, six types par défaut + bootstrap séjour / démarrage), **lien obligatoire** sur **`Activite`** (`type_activite_id` NOT NULL, résolution **`findByIdAndSejourId`**), CRUD **`/api/v1/sejours/{sejourId}/types-activite`**, `TypeActiviteServiceImpl`, **`TypeActiviteLibellesParDefaut`**, **`SejourServiceImpl`** + **`TypeActiviteInitializer`**, tests **`TypeActiviteServiceImplTest`**. ✅

### En cours / À faire
- [x] **`LieuServiceImplTest`** : doublon nom, trim, modifier, lister, **validation partage** (sans max / avec max 2). ✅
- [ ] Ajouter les tests pour les endpoints dossier : `GET /enfants/{enfantId}/dossier` et `PUT /enfants/{enfantId}/dossier` (EnfantControllerTest, EnfantServiceImplTest) — inclure les cas : accès autorisé (directeur/équipe), accès refusé (403), enfant non inscrit (404), dossier non trouvé (404)
- [ ] Compléter les tests Activité : `ActiviteServiceImplTest` — succès **`modifierActivite`** (lieu / cas restants) si besoin, suppression réussie ; *déjà couvert* : conflit **animateur** créneau (POST/PUT), **`modifierActivite`** exclusion id courant. `ActiviteControllerTest` : GET par id, PUT, DELETE, **400** lieu occupé / limite partage. Ajouter **`MomentControllerTest`** / **`MomentServiceImplTest`** si couverture souhaitée. Ajouter **`TypeActiviteControllerTest`** si couverture souhaitée (**`TypeActiviteServiceImplTest`** déjà présent).
- [ ] Tests d'intégration (Repository avec `@DataJpaTest`, End-to-End avec `@SpringBootTest`) - voir section "Tests d'Intégration" ci-dessus

