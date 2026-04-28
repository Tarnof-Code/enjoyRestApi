<!-- Fiche Memory Bank — point d'entrée : [AI_MEMORY.md](../../AI_MEMORY.md) -->

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
- **`PlanningGrille` / `PlanningLigne` / `PlanningCellule`** : grilles de planning (direction). **`PlanningGrilleServiceImpl`**, **`PlanningGrilleController`** sous **`/api/v1/sejours/{sejourId}/planning-grilles`**. Cellules : **`ManyToMany`** vers animateurs (**`token_id`**, table **`planning_cellule_utilisateur`**) et vers **`Horaire`**, **`Moment`**, **`Groupe`**, **`Lieu`** (tables **`planning_cellule_horaire`**, **`planning_cellule_moment`**, **`planning_cellule_groupe`**, **`planning_cellule_lieu`**). Contrat JSON cellules : **listes** **`horaireIds`**, **`horaireLibelles`**, **`momentIds`**, **`groupeIds`**, **`lieuIds`**, **`membreTokenIds`**. Ancien schéma avec colonnes **`horaire_id`**, **`moment_id`**, **`groupe_id`**, **`lieu_id`** sur **`planning_cellule`** : hors mapping JPA ; si encore présentes en MySQL, reprise éventuelle vers les tables de jointure puis **`DROP FOREIGN KEY`** puis **`DROP COLUMN`**. Endpoints et règles **`sourceContenuCellules`** : [documentation-api-rest.md](./documentation-api-rest.md). Tests : **`PlanningGrilleControllerTest`**, **`PlanningGrilleServiceImplTest`**.

### Synchronisation Backend-Frontend
- Le dépôt **enjoyApi** ne contient pas le frontend ; les chemins ci-dessous supposent le projet web associé (ex. `enjoyWebApp`).
- [FAIT] Fichier `api.d.ts` dans le frontend (`enjoyWebApp/src/types/api.d.ts`) avec les types TypeScript alignés sur les DTOs Java.
- Types disponibles : `SejourDTO`, `ProfilUtilisateurDTO`, `EnfantDto`, `DossierEnfantDto`, `GroupeDto`, `CreateGroupeRequest`, `AjouterReferentRequest`, **`MomentDto`** (incl. **`ordre`**), **`SaveMomentRequest`**, **`ReorderMomentsRequest`**, **`HoraireDto`**, **`SaveHoraireRequest`**, `ActiviteDto` (**`moment`** avec **`ordre`**, **`lieu`**, **`typeActivite`** avec **`sejourId`** (obligatoire côté domaine), **`avertissementLieu`** optionnel surtout après POST/PUT, **`groupeIds`**) / `CreateActiviteRequest` / `UpdateActiviteRequest` (`lieuId?`, **`momentId`**, **`typeActiviteId`** obligatoire, `groupeIds`), **`TypeActiviteDto`** (**`sejourId`**, **`predefini`**), **`SaveTypeActiviteRequest`**, **`LieuDto`** (**`partageableEntreAnimateurs`**, **`nombreMaxActivitesSimultanees`**) / **`SaveLieuRequest`** (mêmes champs partage), `EmplacementLieu`, `CreateSejourRequest`, `CreateEnfantRequest`, `UpdateDossierEnfantRequest`, `MembreEquipeRequest`, **`UpdateMembreEquipeRoleRequest`**, `RegisterRequest`, `UpdateUserRequest`, `AuthenticationRequest`, `AuthenticationResponse`, `RefreshTokenResponse`, `ErrorResponse`, `ExcelImportResponse`, `ExcelImportSpecResponse`, `ExcelImportColumnSpec`. **À jour côté enjoyApi** : vérifier que le frontend **`api.d.ts`** inclut bien **`moment` / `momentId`**, **`moment.ordre`**, **`PUT .../moments/reorder`** avec **`momentIds`**, **`HoraireDto` / `SaveHoraireRequest`** + CRUD **`/sejours/{sejourId}/horaires`**, et **`typeActivite` / `typeActiviteId`** (**obligatoire** en création / édition d’activité) + CRUD sous **`/sejours/{sejourId}/types-activite`**, et pour l’équipe séjour **`UpdateMembreEquipeRoleRequest`** (body du **`PUT .../equipe/{membreTokenId}`**).
- Les dates Java (`Date`, `Instant`) sont typées comme `string` en TypeScript car sérialisées en ISO 8601 par Jackson.
- [FAIT] Migration des types locaux vers `api.d.ts` effectuée :
  - `sejour.service.ts` : `SejourInfos` → `CreateSejourRequest`, retours typés avec `SejourDTO`
  - `DetailsSejour.tsx` : interface locale `Sejour` → `SejourDTO`
  - `ListeSejoursAdmin.tsx` : interface locale `Sejour` → `SejourDTO`
  - `ListeSejoursDirecteur.tsx` : interface locale `Sejour` → `SejourDTO`
  - `SejourForm.tsx` : `SejourInfos` → `CreateSejourRequest`
  - `UserForm.tsx` : `AddMembreRequest` → `MembreEquipeRequest` (POST **membre existant**). **Changement de rôle** : **`PUT /sejours/{id}/equipe/{membreTokenId}`** avec body **`UpdateMembreEquipeRoleRequest`** (`roleSejour` seul) — ne pas dupliquer `tokenId` dans le JSON.
- Tous les services et composants liés aux séjours utilisent maintenant les types centralisés de `api.d.ts`.
- [FAIT] Implémentation frontend complète de la gestion des enfants :
  - `AddEnfantForm.tsx` : Formulaire générique pour créer/modifier un enfant (utilise le composant `Form.tsx`)
  - `ListeEnfants.tsx` : Composant de liste avec filtres, tri, actions CRUD (création, modification, suppression)
  - `ImportExcelEnfants.tsx` : Composant d'import Excel avec gestion des résultats et messages d'erreur
  - `DetailsSejour.tsx` : Intégration de la liste des enfants dans la page de détails d'un séjour
  - `sejour.service.ts` : Services complets pour gérer les enfants (`getEnfantsDuSejour`, `creerEtAjouterEnfant`, `modifierEnfant`, `supprimerEnfantDuSejour`, `supprimerTousLesEnfants`, `importerEnfantsExcel`)
  - Tous les composants utilisent les types centralisés de `api.d.ts` (`EnfantDto`, `CreateEnfantRequest`, `ExcelImportResponse`)
- [À FAIRE côté frontend si écran planning déjà branché] **`PlanningCellulePayload` / `PlanningCelluleDto`** : champs **listes** **`horaireIds`**, **`horaireLibelles`**, **`momentIds`**, **`groupeIds`**, **`lieuIds`**, **`membreTokenIds`** (plus de `horaireId` / … singuliers pour les cellules). Voir [documentation-api-rest.md](./documentation-api-rest.md) et [frontend-planning-cellules-multiples.md](../frontend-planning-cellules-multiples.md).

