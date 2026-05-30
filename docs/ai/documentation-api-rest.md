<!-- Fiche Memory Bank — point d'entrée : [AI_MEMORY.md](../../AI_MEMORY.md) -->

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

#### GET `/api/v1/sejours/utilisateur/{utilisateurTokenId}`
- **Description** : Récupérer tous les séjours d'un utilisateur selon son rôle
  - **ADMIN** : récupère tous les séjours du système
  - **DIRECTION / BASIC_USER** : récupère uniquement les séjours où l'utilisateur est directeur ou membre de l'équipe
- **Autorisation** : `ROLE_ADMIN` ou `ROLE_DIRECTION` ou `ROLE_BASIC_USER`
- **Path Variable** : `utilisateurTokenId` (string)
- **Réponse** : `List<SejourDTO>` (200 OK)
- **Codes d'erreur** :
  - `404` : Utilisateur non trouvé
- **Note** : Remplace l'ancien endpoint `/api/v1/sejours/directeur/{directeurTokenId}` (déprécié)

#### POST `/api/v1/sejours/{id}/equipe/existant`
- **Description** : Ajouter un membre existant à l'équipe d'un séjour
- **Autorisation** : `ROLE_DIRECTION`
- **Path Variable** : `id` (int)
- **Body** : `MembreEquipeRequest` — **`tokenId`** (obligatoire, référence utilisateur côté API) et **`roleSejour`**
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
- **Path Variables** : `id` (int), `membreTokenId` (string) — identifiant **seul** du membre côté API (pas de `tokenId` redondant dans le body)
- **Body** : `UpdateMembreEquipeRoleRequest` — **`roleSejour`** uniquement
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

#### GET `/api/v1/sejours/{sejourId}/dossiers-enfants`
- **Description** : Liste pour un écran type **sanitaire** : un élément par enfant inscrit au séjour, avec identité, **groupes** du séjour auxquels l’enfant est rattaché, et **dossier** (même contenu fonctionnel que `DossierEnfantDto` du GET dossier unitaire). Charge optimisée (requêtes groupées : inscriptions, dossiers avec références alimentaires, groupes avec enfants).
- **Autorisation** : **`ACCES_SEJOUR`** — accès réservé au **directeur** du séjour ou à un **membre d’équipe** (même règle métier que la consultation des enfants / dossiers de ce séjour).
- **Path Variable** : `sejourId` (int)
- **Réponse** : `List<EnfantDossierSanitaireLigneDto>` (200 OK) — champs : **`enfantId`**, **`prenom`**, **`nom`**, **`groupes`** (`List<GroupeResumeDto>` : **`id`**, **`libelle`**), **`dossier`** (`DossierEnfantDto` ou **`null`** s’il n’existe pas de ligne dossier pour cet enfant ; un dossier « vide » reste un objet avec champs / listes vides).
- **Codes d'erreur** :
  - `403` : Utilisateur ne participant pas au séjour
  - `404` : Séjour non trouvé

#### GET `/api/v1/sejours/{id}/enfants/{enfantId}/dossier`
- **Description** : Récupérer le dossier d'un enfant (contacts parents, infos médicales, traitements, etc.)
- **Autorisation** : `ROLE_DIRECTION` (directeur du séjour ou membre de l'équipe)
- **Path Variables** : `id` (int) - ID du séjour, `enfantId` (int) - ID de l'enfant
- **Réponse** : `DossierEnfantDto` (200 OK) — inclut **`allergenes`** et **`regimesEtPreferences`** (`List<ReferenceAlimentaireDto>`)
- **Codes d'erreur** :
  - `403` : Utilisateur ne participant pas au séjour (`AccessDeniedException`)
  - `404` : Séjour non trouvé, enfant non inscrit au séjour, ou dossier non trouvé

#### PUT `/api/v1/sejours/{id}/enfants/{enfantId}/dossier`
- **Description** : Modifier le dossier d'un enfant (contacts parents, infos médicales, traitements, etc.)
- **Autorisation** : `ROLE_DIRECTION` (directeur du séjour ou membre de l'équipe)
- **Path Variables** : `id` (int) - ID du séjour, `enfantId` (int) - ID de l'enfant
- **Body** : `UpdateDossierEnfantRequest` (tous les champs optionnels : emailParent1/2, telephoneParent1/2, informationsMedicales, pai, **`allergeneIds`**, **`regimePreferenceIds`** (listes d’ids `ReferenceAlimentaire`), informationsAlimentaires, traitements matin/midi/soir/si besoin, autresInformations, aPrendreEnSortie)
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

### Menus et références alimentaires

#### Référentiel global — `/api/v1/references-alimentaires`

- **Autorisation** : **`GET`** (liste et détail) : privilège **`ACCES_SEJOUR`** (ex. animateurs, direction, admin selon JWT). **`POST` / `PUT` / `DELETE`** : **`ROLE_ADMIN`** ou **`ROLE_DIRECTION`** uniquement.
- **GET** `/api/v1/references-alimentaires` — Liste des références ; query optionnelle **`type`** (`TypeReferenceAlimentaire` : ex. allergène vs régime/préférence).
- **GET** `/api/v1/references-alimentaires/{id}` — Détail.
- **POST** `/api/v1/references-alimentaires` — Création ; body **`SaveReferenceAlimentaireRequest`** → **`201`**, **`ReferenceAlimentaireDto`**.
- **PUT** `/api/v1/references-alimentaires/{id}` — Mise à jour ; body **`UpdateReferenceAlimentaireRequest`**.
- **DELETE** `/api/v1/references-alimentaires/{id}` — **`204`**.

Les lignes « catalogue » attendues au besoin sont aussi créées idempotent par **`ReferenceAlimentaireInitializer`** au démarrage de l’application.

#### Menus par séjour — `/api/v1/sejours/{sejourId}/menus`

- **Autorisation** : **`GET`** (liste, détail) : **`ACCES_SEJOUR`** + appartenance au séjour (directeur, membre d’équipe ou **ADMIN** — vérifiée côté service via **`tokenId`**). **`POST` / `PUT` / `DELETE`** : **`GESTION_SEJOURS`** (directeur, adjoint avec droits, etc., selon JWT + garde-fous existants).
- **GET** `/api/v1/sejours/{sejourId}/menus` — Liste **`MenuRepasDto`** ; **obligatoire** : soit **`date`** (un jour, format ISO date), soit **`dateDebut` et `dateFin`** (période). Sinon **`400`** (`IllegalArgumentException` : message demandant l’un ou l’autre mode).
- **GET** `/api/v1/sejours/{sejourId}/menus/{menuId}` — Détail.
- **POST** `/api/v1/sejours/{sejourId}/menus` — Création ; body **`SaveMenuRepasRequest`** (`dateRepas`, **`typeRepas`** (`TypeRepas`), champs texte optionnels selon le type de repas, **`allergeneIds`**, **`regimePreferenceIds`**) → **`201`**, **`MenuRepasDto`**.
- **PUT** `/api/v1/sejours/{sejourId}/menus/{menuId}` — Mise à jour ; même body que la création.
- **DELETE** `/api/v1/sejours/{sejourId}/menus/{menuId}` — **`204`**.

Un seul menu par couple **`(sejour, date du repas, type de repas)`** (contrainte d’unicité côté entité).

#### Agrégation dossiers enfants du séjour — `/api/v1/sejours/{sejourId}/references-alimentaires-agregees-enfants`

- **Autorisation** : **`ACCES_SEJOUR`** + appartenance au séjour (vérification côté service).
- **GET** — **`ReferencesAlimentairesAgregeesEnfantsDto`** : union des **`ReferenceAlimentaireDto`** déclarés sur au moins un **`DossierEnfant`** d’un enfant **inscrit** au séjour, séparée en **`allergenes`** et **`regimesEtPreferences`** (sans doublon). Sert typiquement à proposer des tags cohérents lors de la composition des menus.

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

**Autorisation** : **`GET`** **`ACCES_SEJOUR`** + **appartenance au séjour** (`verifierAppartenanceAuSejour`). **`POST` / `PUT` / `DELETE`** **`GESTION_SEJOURS`** (directeur / adjoint avec droits, etc. — aligné sur les autres ressources « direction de séjour »).

#### GET `/api/v1/sejours/{sejourId}/lieux`
- **Description** : Lister les lieux du séjour
- **Réponse** : `List<LieuDto>` (200 OK) — inclut **`partageableEntreAnimateurs`**, **`nombreMaxActivitesSimultanees`**, **`usages`** (`Set` d’**`UsageLieu`** : **`ACTIVITE`**, **`SURVEILLANCE`**, **`RASSEMBLEMENT`**, ordre stable côté API)
- **Codes d'erreur** : `404` : Séjour non trouvé

#### GET `/api/v1/sejours/{sejourId}/lieux/{lieuId}`
- **Description** : Détail d'un lieu
- **Réponse** : `LieuDto` (200 OK)
- **Codes d'erreur** : `404` : Séjour ou lieu non trouvé, ou lieu d'un autre séjour

#### POST `/api/v1/sejours/{sejourId}/lieux`
- **Description** : Créer un lieu pour le séjour
- **Body** : `SaveLieuRequest` (`nom`, `emplacement`, `nombreMax` optionnel `@Positive`, **`partageableEntreAnimateurs`**, **`nombreMaxActivitesSimultanees`** : obligatoire et **≥ 2** si partage ; **`null`** si pas de partage ; **`usages`** : ensemble **non vide** de **`UsageLieu`** — **`@NotEmpty`**)
- **Réponse** : `LieuDto` (201 Created)
- **Codes d'erreur** : `400` : validation Jakarta, règles partage, ou **aucun usage valide** après filtrage (`IllegalArgumentException`), `404` : séjour, `409` : nom déjà utilisé (casse ignorée)

#### PUT `/api/v1/sejours/{sejourId}/lieux/{lieuId}`
- **Description** : Modifier un lieu
- **Body** : `SaveLieuRequest` (inchangé, incl. **`usages`** obligatoires côté contrat)
- **Réponse** : `LieuDto` (200 OK)
- **Codes d'erreur** : `400` : validation / usages, `404` : Séjour ou lieu non trouvé / mauvais séjour, `409` : nouveau nom déjà pris par un autre lieu du même séjour

#### DELETE `/api/v1/sejours/{sejourId}/lieux/{lieuId}`
- **Description** : Supprimer un lieu
- **Réponse** : `204 No Content`
- **Codes d'erreur** : `404` : Séjour ou lieu non trouvé / mauvais séjour

### Endpoints des Chambres (`/api/v1/sejours/{sejourId}/chambres`)

**Autorisation** : **`GET`** **`ACCES_SEJOUR`** + appartenance au séjour ; **`POST` / `PUT` / `DELETE`** et gestion référents **`GESTION_SEJOURS`**.

**Modèle** : hébergement (distinct des **Lieux** d’activité). **`TypeChambre`** : **`ENFANT`** (référents autorisés) ou **`EQUIPE`** (pas de référents). **`identifiant`** obligatoire, **unique par séjour** (casse ignorée) ; **`nom`** optionnel (surnom). **Hors périmètre API** : affectation des occupants (enfants / membres d’équipe) aux chambres.

#### GET `/api/v1/sejours/{sejourId}/chambres`
- **Description** : Lister les chambres du séjour (**tri** : `batiment`, `etage`, `couloir`, `identifiant`)
- **Réponse** : `List<ChambreDto>` (200 OK)
- **Codes d'erreur** : `404` : Séjour non trouvé ; `403` : hors séjour

#### GET `/api/v1/sejours/{sejourId}/chambres/{chambreId}`
- **Description** : Détail d’une chambre
- **Réponse** : `ChambreDto` (200 OK)
- **Codes d'erreur** : `404` : Séjour ou chambre non trouvé / mauvais séjour

#### POST `/api/v1/sejours/{sejourId}/chambres`
- **Description** : Créer une chambre
- **Body** : `SaveChambreRequest` (`typeChambre`, `identifiant` **@NotBlank** max 50, `nom?` max 150, `capaciteMax` **@Positive**, `genreAutorise`, `description?`, `batiment?`, `couloir?`, `etage?`)
- **Réponse** : `ChambreDto` (201 Created)
- **Codes d'erreur** : `400` validation Jakarta, `404` séjour, `409` identifiant déjà utilisé pour ce séjour

#### PUT `/api/v1/sejours/{sejourId}/chambres/{chambreId}`
- **Description** : Modifier une chambre (même body que POST). Passage **`ENFANT` → `EQUIPE`** : référents supprimés côté serveur.
- **Réponse** : `ChambreDto` (200 OK)
- **Codes d'erreur** : `400` validation, `404` séjour ou chambre, `409` identifiant en conflit

#### DELETE `/api/v1/sejours/{sejourId}/chambres/{chambreId}`
- **Description** : Supprimer une chambre
- **Réponse** : `204 No Content`
- **Codes d'erreur** : `404` : Séjour ou chambre non trouvé

#### POST `/api/v1/sejours/{sejourId}/chambres/{chambreId}/referents`
- **Description** : Ajouter un référent (**chambres `ENFANT` uniquement**)
- **Body** : `AjouterReferentRequest` (`referentTokenId`)
- **Réponse** : `201 Created` (corps vide)
- **Codes d'erreur** : `400` chambre **`EQUIPE`**, `404` séjour / chambre / référent, `409` référent déjà présent

#### DELETE `/api/v1/sejours/{sejourId}/chambres/{chambreId}/referents/{referentTokenId}`
- **Description** : Retirer un référent (**chambres `ENFANT` uniquement**)
- **Réponse** : `204 No Content`
- **Codes d'erreur** : `400` chambre **`EQUIPE`**, `404` séjour / chambre / référent absent

**DTO `ChambreDto`** : `id`, `sejourId`, `typeChambre`, `identifiant`, `nom`, `capaciteMax`, `genreAutorise`, `description`, `batiment`, `couloir`, `etage`, `referents[]` (`tokenId`, `nom`, `prenom` — **`[]`** pour **`EQUIPE`**).

**Enums** : **`TypeChambre`** (`ENFANT`, `EQUIPE`), **`GenreChambre`** (`MASCULIN`, `FEMININ`, `MIXTE`).

### Endpoints des Horaires (`/api/v1/sejours/{sejourId}/horaires`)

**Autorisation** : **`GET`** **`ACCES_SEJOUR`** + appartenance au séjour ; **`POST` / `PUT` / `DELETE`** **`GESTION_SEJOURS`**.

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

### Cahier d’infirmerie (`/api/v1/sejours/{sejourId}/cahier-infirmerie`)

**Autorisation** — **`GET`** (liste, détail, historique) : **`ACCES_SEJOUR`** + appartenance au séjour. **`POST`** : idem ; le **créateur** est l’utilisateur connecté, le **soigneur** est choisi via **`soigneurTokenId`**. **`PUT`** et **`DELETE`** : **`ACCES_SEJOUR`** ; côté service : **gestion complète du séjour** sur ce séjour (**`aDroitGestionCompleteSurSejour`** : directeur, adjoint **`GESTION_SEJOURS`**, …), **ADMIN**, **ou** **auteur** (**`createur.tokenId`**) **ou** **soigneur** (**`soigneur.tokenId`**), sinon **`403`**.

#### GET `/api/v1/sejours/{sejourId}/cahier-infirmerie`
- **Description** : Lister les entrées du cahier (**tri** : `dateHeure` décroissante, puis `id`)
- **Réponse** : `List<CahierInfirmerieEntreeDto>` (200 OK)

#### GET `/api/v1/sejours/{sejourId}/cahier-infirmerie/{entreeId}`
- **Description** : Détail d’une entrée
- **Réponse** : `CahierInfirmerieEntreeDto` (200 OK)
- **Codes d'erreur** : `404` si entrée absente ou pas pour ce séjour

#### GET `/api/v1/sejours/{sejourId}/cahier-infirmerie/{entreeId}/historique`
- **Description** : Historique des modifications (création, modification, suppression) avec **snapshots lisibles** dans `ancienneValeur` / `nouvelleValeur`
- **Réponse** : `List<HistoriqueModificationCahierInfirmerieDto>` (200 OK) — **`type`** = **`CAHIER_INFIRMERIE`**, champ **`cahierInfirmerieEntreeId`**
- **Codes d'erreur** : `404` si entrée introuvable pour ce séjour

#### POST `/api/v1/sejours/{sejourId}/cahier-infirmerie`
- **Description** : Créer une entrée ; le **créateur** est l’utilisateur authentifié (`createur` persisté). Le **soigneur** peut être **une autre personne** : **`soigneurTokenId`** désigne qui a réalisé les soins (directeur du séjour ou membre **`sejour_equipe`**).
- **Body** : `SaveCahierInfirmerieEntreeRequest` (`dateHeure`, `enfantId`, `description`, `localisationCorps?`, **`soins`** non vide, `soinsAutrePrecision?`, **`temperatureCelsius?`** — **obligatoire** (`BigDecimal`, ex. `37.5`) si **`PRISE_TEMPERATURE`** ∈ `soins`, interdit sinon ; plage métier **30–45** °C, **≤ 2** décimales, `appels` défaut `[]`, `appelAutrePrecision?`, **`soigneurTokenId`** — doit désigner le **directeur** du séjour ou un utilisateur présent dans **`sejour_equipe`**) — si **`AUTRE`** dans `soins` / `appels`, précision obligatoire (**400**)
- **Réponse** : `CahierInfirmerieEntreeDto` (201 Created)
- **Codes d'erreur** : `400` (validation, enfant non inscrit au séjour), `404` utilisateur token pour persistance créateur

#### PUT `/api/v1/sejours/{sejourId}/cahier-infirmerie/{entreeId}`
- **Description** : Mettre à jour une entrée (même body que POST). **Gestion du séjour** (directeur / adjoint avec **`GESTION_SEJOURS`**), **auteur**, **soigneur** désigné ou **ADMIN**.
- **Réponse** : `CahierInfirmerieEntreeDto` (200 OK)
- **Codes d'erreur** : `403` si aucun de ces rôles / liens ; `400` / `404` comme POST

#### DELETE `/api/v1/sejours/{sejourId}/cahier-infirmerie/{entreeId}`
- **Description** : Supprimer une entrée — **mêmes acteurs autorisés** que pour le **PUT** (direction du séjour sur ce séjour, auteur, soigneur désigné, **ADMIN**).
- **Réponse** : `204 No Content` — une ligne **`SUPPRESSION`** est enregistrée dans l’historique **avant** suppression
- **Codes d'erreur** : `403`, `404`

### Endpoints des Moments (`/api/v1/sejours/{sejourId}/moments`)

**Autorisation** : **`GET`** **`ACCES_SEJOUR`** + appartenance au séjour ; **`POST` / `PUT` / `DELETE`** **`GESTION_SEJOURS`** (incl. **`PUT .../moments/reorder`**).

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
- **Description** : Supprimer un moment (impossible si des activités internes **ou** des sorties prestataires y sont rattachées)
- **Réponse** : `204 No Content`
- **Codes d'erreur** : `404` séjour ou moment, `400` si activités ou sorties existantes (`IllegalArgumentException`)

### Endpoints des réunions / comptes rendus (`/api/v1/sejours/{sejourId}/reunions`)

**Autorisation** : **`GET` (liste et détail)** **`ACCES_SEJOUR`** + appartenance au séjour (**`SejourVerificationService.verifierAppartenanceAuSejour`**). **`POST` / `PUT` / `DELETE`** **`GESTION_SEJOURS`** (même périmètre que moments / lieux pour l’écriture « direction » au sens Spring).

**Contrat données** :
- **`date`** : **`LocalDate`**, sérialisée **`yyyy-MM-dd`** (`@JsonFormat` sur **`ReunionDto`** et **`SaveReunionRequest`**).
- **`ordreDuJour`** : **optionnel** ; string courte **`@Size(max=500)`**, normalisée côté service (`null` si absent ou blanc après trim).
- **`contenu`** : **document JSON TipTap** (ProseMirror), obligatoire en création/mise à jour ; persisté en colonne **`contenu_json`** (TEXT). Le backend sérialise / désérialise avec Jackson (`JsonNode`).

#### GET `/api/v1/sejours/{sejourId}/reunions`
- **Description** : Lister les comptes rendus du séjour (**tri** : **`date_reunion` croissant**, puis **`id`**)
- **Réponse** : `List<ReunionDto>` (200 OK) — `id`, `sejourId`, `date`, `ordreDuJour` (peut être `null`), `contenu` (objet JSON)
- **Codes d'erreur** : `403` si pas d’accès au séjour

#### GET `/api/v1/sejours/{sejourId}/reunions/{reunionId}`
- **Description** : Détail d’une réunion
- **Réponse** : `ReunionDto` (200 OK)
- **Codes d'erreur** : `404` réunion absente ou pas pour ce séjour

#### POST `/api/v1/sejours/{sejourId}/reunions`
- **Body** : `SaveReunionRequest` (`date` **obligatoire**, `ordreDuJour` **optionnel**, `contenu` **obligatoire** — JSON TipTap, ex. `{"type":"doc","content":[]}`)
- **Réponse** : `ReunionDto` (201 Created)
- **Codes d'erreur** : `400` validation ; `404` séjour

#### PUT `/api/v1/sejours/{sejourId}/reunions/{reunionId}`
- **Description** : Remplacement du document (même body que POST)
- **Réponse** : `ReunionDto` (200 OK)
- **Codes d'erreur** : `400` / `404` comme POST

#### DELETE `/api/v1/sejours/{sejourId}/reunions/{reunionId}`
- **Réponse** : `204 No Content`
- **Codes d'erreur** : `404`

### Endpoints des activités prestataires / sorties (`/api/v1/sejours/{sejourId}/activites-prestataires`)

**Autorisation** : **`GET` (liste et détail)** **`ACCES_SEJOUR`** + appartenance au séjour. **`POST` / `PUT` / `DELETE`** **`GESTION_SEJOURS`** (direction / adjoint).

**Modèle** :
- Sortie externalisée : **`nom`**, **`date`** (`yyyy-MM-dd`), **`moments`** (un ou plusieurs, min. 1), **`heureDepart` / `heureRetour`** optionnels (`HH:mm`), **`informations`**, **`telephone`**, **`groupeIds`** optionnel (0 à N).
- **Animateurs concernés (calendrier)** : référents (`referents[].tokenId`) des groupes listés dans **`groupeIds`**. Si **`groupeIds` vide** → aucune ligne calendrier animateur (liste sorties uniquement).
- **`nonParticipations`** : `{ tokenId, momentId }[]` — animateur concerné qui **ne voit pas** la sortie sur **ce moment** (granularité par moment). Liste vide par défaut.

**`SaveActivitePrestataireRequest`** :
- **`momentIds`** : `@NotEmpty`, min. 1.
- **`nonParticipations`** optionnel : si **fourni** en PUT → **liste complète de remplacement** ; si **omis** (`null`) → conserver l’existant puis **élaguer** (moment retiré, animateur plus référent, groupes vidés). Sync incrémentale côté serveur (réutilise les lignes existantes pour éviter violation **`uk_ap_non_participation`**).

**Règles métier** :
- **Anti-doublon** : impossible d’avoir deux sorties distinctes pour le même triplet **date + moment + groupe** sur le séjour (**400**, message avec nom du groupe et du moment). Si **`groupeIds` vide**, pas de contrôle. En PUT, la sortie courante est exclue.
- **Non-participation** : **`tokenId`** doit être référent d’un groupe de **`groupeIds`** ; **`momentId`** ∈ moments de la sortie ; refus si **`groupeIds` vide** et liste non vide.

#### GET `/api/v1/sejours/{sejourId}/activites-prestataires`
- **Description** : Lister les sorties du séjour (tri **`date` croissante**, puis **`id`**)
- **Réponse** : `List<ActivitePrestataireDto>` (200 OK)

#### GET `/api/v1/sejours/{sejourId}/activites-prestataires/{activitePrestataireId}`
- **Réponse** : `ActivitePrestataireDto` (200 OK) — champs : `id`, `nom`, `date`, **`moments`** (`MomentDto[]`, tri chronologique), `sejourId`, `heureDepart`, `heureRetour`, `informations`, `telephone`, `groupeIds`, **`nonParticipations`**
- **Codes d'erreur** : `404` si absente pour ce séjour ; `403` accès séjour

#### POST `/api/v1/sejours/{sejourId}/activites-prestataires`
- **Body** : `SaveActivitePrestataireRequest`
- **Réponse** : `ActivitePrestataireDto` (201 Created)
- **Codes d'erreur** : `400` validation, date hors séjour, doublon date+moment+groupe, non-participation invalide ; `404` moment / groupe / utilisateur

#### PUT `/api/v1/sejours/{sejourId}/activites-prestataires/{activitePrestataireId}`
- **Body** : `SaveActivitePrestataireRequest` (même schéma que POST)
- **Réponse** : `ActivitePrestataireDto` (200 OK)
- **Codes d'erreur** : idem POST

#### DELETE `/api/v1/sejours/{sejourId}/activites-prestataires/{activitePrestataireId}`
- **Réponse** : `204 No Content` (cascade suppression **`activite_prestataire_non_participation`**)
- **Codes d'erreur** : `404`

**Calendrier activités (front)** : afficher une carte sortie sur la ligne `(tokenId, date)` pour chaque moment `m` si l’animateur est référent concerné et **aucune** entrée dans **`nonParticipations`** pour `{ tokenId, m.id }`. Conflit avec activité interne : résolution direction (DELETE activité interne ou PUT avec non-participation) — pas d’endpoint dédié « conflits ».

### Endpoints des Activités (`/api/v1/sejours/{sejourId}/activites`)

**Autorisation** — **`GET`** : **`ACCES_SEJOUR`** + appartenance au séjour (comportement aligné sur lieux / horaires / activités en lecture). **`POST`** : **`ACCES_SEJOUR`** + **appartenance au séjour** (création réservée aux participants du séjour, pas au seul privilège global `GESTION_SEJOURS` sans lien). **`PUT` / `DELETE`** : **`ACCES_SEJOUR`** ; côté service, autorisé si **gestion complète du séjour** (**`SejourVerificationService.aDroitGestionCompleteSurSejour`**, ex. directeur, **ADJOINT**, **ADMIN**) **ou** si l’utilisateur est **affecté à l’activité** (liste **`membres`**), sinon **`403`** avec message métier (`AccessDeniedException`).

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

`CreateActiviteRequest` (`date`, `nom`, `description` optionnelle, **`lieuId` optionnel** — si renseigné, le lieu doit porter l’usage **`ACTIVITE`** (`UsageLieu`) sinon **400**, **`momentId`** — obligatoire côté service si au moins un moment existe pour le séjour ; si **aucun moment** → **400** avec consigne de faire créer des moments par la direction, **`typeActiviteId` obligatoire** (`@NotNull`), `membreTokenIds`, **`groupeIds`**). **Partage lieu** : cohérence **jour + moment + lieu** (voir `ActiviteRepository`).
- **Réponse** : `ActiviteDto` (201 Created) — **`moment`**, **`typeActivite`**, `groupeIds`, **`lieu`** (**`LieuDto.usages`**), éventuellement **`avertissementLieu`** si le lieu était déjà occupé **ce jour et ce moment** mais le partage le permet
- **Codes d'erreur** : `400` : validation Jakarta (dont **`typeActiviteId`** manquant), date / équipe / groupe / **moments**, **lieu non « lieu d’activité »** (**`IllegalArgumentException`**, message avec id lieu), **lieu déjà pris** ou **limite de partage** ; `404` : séjour, membre, groupe, lieu, **moment**, **type d’activité** (**id inconnu** ou **pas pour ce séjour**) ; `403` : pas d’accès au séjour ; `500` théorique si lieu partageable sans max en base

#### PUT `/api/v1/sejours/{sejourId}/activites/{activiteId}`
- **Description** : Modifier une activité
- **Body** : `UpdateActiviteRequest` (comme la création ; **`lieuId` null** retire le lieu ; **`typeActiviteId` obligatoire** pour pointer vers un type du séjour — pas de retrait du type ; **`momentId`** requis selon les mêmes règles que POST)
- **Réponse** : `ActiviteDto` (200 OK), **`avertissementLieu`** possible comme en POST
- **Codes d'erreur** : `400` / `404` comme POST (comptage lieu **exclut** l’activité modifiée) ; `403` si l’utilisateur n’a pas la gestion complète du séjour **et** n’est pas affecté à l’activité

#### DELETE `/api/v1/sejours/{sejourId}/activites/{activiteId}`
- **Description** : Supprimer une activité
- **Réponse** : `204 No Content`
- **Codes d'erreur** : `404` : Séjour ou activité non trouvé ; `403` : pas les droits (animateur non affecté à l’activité, etc.)

#### GET `/api/v1/sejours/{sejourId}/activites/{activiteId}/historique`
- **Description** : Consulter l'historique des modifications d'une activité (création, modification, suppression) avec snapshots compacts des valeurs
- **Autorisation** : **`ACCES_SEJOUR`** + appartenance au séjour
- **Réponse** : `List<HistoriqueModificationActiviteDto>` (200 OK) — tri chronologique (date modification croissante). Chaque entrée contient : `base` (`id`, `type`, `dateModification`, `modificateurTokenId`, `nom`, `prenom`, `action`, **`ancienneValeur`** (string ou null), **`nouvelleValeur`** (string ou null)), `activiteId`. **Format snapshots** (pipe-separated, **libellés lisibles**, pas d’ids) : `date|nom|description|nomLieu|nomMoment|libelleTypeActivite|animateurs|nomsGroupes`. **Animateurs** : `prénom nom` séparés par `", "` (tri alphabétique sur ce libellé). Lieu / moment / type absents : `-`. Exemple : `2026-05-15|Randonnée|Balade en forêt|Salle A|Matin|Sportive|Jean Dupont, Alice Martin|Groupe 1`
- **Codes d'erreur** : `404` : Séjour ou activité non trouvé ; `403` : pas d'accès au séjour

### Endpoints des types d’activité (`/api/v1/sejours/{sejourId}/types-activite`)

**Par séjour** (même espace que lieux / moments). **Autorisation** : **`GET`** **`ACCES_SEJOUR`** + appartenance ; **`POST` / `PUT` / `DELETE`** **`GESTION_SEJOURS`**.

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

### Plannings — grilles (`/api/v1/sejours/{sejourId}/planning-grilles`)

**Autorisation** :
- **Lecture** (`GET`) : privilège **`ACCES_SEJOUR`** (directeur, équipe, ou ADMIN). Vérification d'**appartenance au séjour** côté service via **`SejourVerificationService.verifierAppartenanceAuSejour`** ; un appel hors séjour → **403** (`AccessDeniedException`).
- **Écriture** (`POST` / `PUT` / `DELETE`) : **`GESTION_SEJOURS`** (aligné sur les autres grilles / ressources direction de séjour ; plus seulement `ROLE_DIRECTION`).
- **Inscription personnelle sur une cellule « membre d'équipe »** (`PATCH .../cellules/{jour}/ma-presence`) : **`ACCES_SEJOUR`** + appartenance au séjour. Réservé aux grilles dont **`sourceContenuCellules`** effectif est **`MEMBRE_EQUIPE`** ; seul **l'utilisateur connecté** peut être ajouté ou retiré (**pas** les autres animateurs). Hors type membre d'équipe → **403**.

Grille = **`PlanningGrille`** (titre, consigne, **`sourceLibelleLignes`**, **`sourceContenuCellules`**, **`miseAJour`**). Lignes = **`PlanningLigne`** (ordre, libellés / refs selon la source — **une seule** référence métier pour le libellé de ligne, ex. `libelleMomentId`). Cellules = **`PlanningCellule`** (jour, texte libre, **plusieurs** animateurs / horaires / moments / groupes / lieux via listes JSON — voir ci‑dessous). **Règle lieu** : lorsque la source est **`LIEU`** (libellé de ligne ou contenu de cellule), chaque lieu référencé doit avoir au moins l’usage **`SURVEILLANCE`** ou **`RASSEMBLEMENT`** ; sinon **`IllegalArgumentException`** (**400**).

#### GET `/api/v1/sejours/{sejourId}/planning-grilles`
- **Autorisation** : `ACCES_SEJOUR` (directeur / membre d'équipe / ADMIN)
- **Réponse** : `List<PlanningGrilleSummaryDto>` (200)
- **403** : utilisateur non rattaché au séjour

#### GET `/api/v1/sejours/{sejourId}/planning-grilles/{grilleId}`
- **Autorisation** : `ACCES_SEJOUR` (directeur / membre d'équipe / ADMIN)
- **Réponse** : `PlanningGrilleDetailDto` (200) — inclut les lignes et leurs cellules
- **403** : utilisateur non rattaché au séjour
- **404** : séjour ou grille

#### POST `/api/v1/sejours/{sejourId}/planning-grilles`
- **Body** : `SavePlanningGrilleRequest` (titre, consigne, sources libellés / contenu cellules)
- **Réponse** : `PlanningGrilleDetailDto` (201)

#### PUT `/api/v1/sejours/{sejourId}/planning-grilles/{grilleId}`
- **Body** : `UpdatePlanningGrilleRequest`
- **Réponse** : `PlanningGrilleDetailDto` (200)

#### DELETE `/api/v1/sejours/{sejourId}/planning-grilles/{grilleId}`
- **Réponse** : `204`

#### POST `/api/v1/sejours/{sejourId}/planning-grilles/{grilleId}/lignes`
- **Body** : `SavePlanningLigneRequest`
- **Réponse** : `PlanningLigneDto` (201)

#### PUT `/api/v1/sejours/{sejourId}/planning-grilles/{grilleId}/lignes/{ligneId}`
- **Body** : `UpdatePlanningLigneRequest`
- **Réponse** : `PlanningLigneDto` (200)

#### DELETE `/api/v1/sejours/{sejourId}/planning-grilles/{grilleId}/lignes/{ligneId}`

#### GET `/api/v1/sejours/{sejourId}/planning-grilles/{grilleId}/lignes/{ligneId}/historique-cellules`
- **Description** : Consulter l'historique des modifications des cellules d'une ligne de planning (création, modification, suppression) avec snapshots compacts des valeurs
- **Autorisation** : **`ACCES_SEJOUR`** + appartenance au séjour
- **Query Parameters** : `jour` (`LocalDate`, optionnel) — filtre par jour spécifique
- **Réponse** : `List<HistoriqueModificationPlanningCelluleDto>` (200 OK) — tri chronologique. Chaque entrée contient : `base` (`id`, `type`, `dateModification`, `modificateurTokenId`, `nom`, `prenom`, `action`, **`ancienneValeur`** (string ou null), **`nouvelleValeur`** (string ou null)), `planningLigneId`, `planningJour`, `planningCelluleId`. **Format snapshots** (pipe-separated, **libellés lisibles**) : `texteLibre|animateurs|horaires|moments|groupes|lieux`. Segments = listes séparées par **virgule et espace** (`", "`) : animateurs en **prénom nom** (tri alphabétique), horaires par **libellé**, moments / groupes / lieux par **nom**. Exemple : `Surveillance piscine|Jean Dupont|8h00, 9h00|Matin|Les ados|Piscine, Clairière`
- **Codes d'erreur** : `404` : Séjour, grille ou ligne non trouvé ; `403` : pas d'accès au séjour

#### PUT `/api/v1/sejours/{sejourId}/planning-grilles/{grilleId}/lignes/{ligneId}/cellules`
- **Body** : `UpsertPlanningCellulesRequest` — champ **`cellules`** : liste de **`PlanningCellulePayload`** (`jour` obligatoire ; **`membreTokenIds`**, **`horaireIds`**, **`momentIds`**, **`groupeIds`**, **`lieuIds`** en **tableaux** ; **`texteLibre`** ; plus de champs singuliers `horaireId` / … pour les cellules).
- **Réponse** : `List<PlanningCelluleDto>` (200) — **`horaireIds`** + **`horaireLibelles`** (même ordre, tri par id horaire) ; autres ids / `membreTokenIds` en listes.
- **Règle** : selon **`sourceContenuCellules`** de la grille, **une seule** « famille » de listes d’ids doit être renseignée par cellule (ex. `MOMENT` → au moins un **`momentIds`**, pas `groupeIds` en même temps). `MEMBRE_EQUIPE` → uniquement **`membreTokenIds`**. Incohérence → **400**.
- **Guide front** (détail UX / exemples) : [`docs/frontend-planning-cellules-multiples.md`](../../frontend-planning-cellules-multiples.md).

#### PATCH `/api/v1/sejours/{sejourId}/planning-grilles/{grilleId}/lignes/{ligneId}/cellules/{jour}/ma-presence`
- **Description** : L’animateur ou tout membre d’équipe avec **`ACCES_SEJOUR`** s’inscrit (`present: true`) ou se désinscrit (`present: false`) sur une cellule d’une journée précise, **uniquement si** la grille a un contenu de cellules de type **`MEMBRE_EQUIPE`**. Aucune liste d’animateurs dans le corps : impossible d’ajouter ou de retirer quelqu’un d’autre.
- **Autorisation** : **`ACCES_SEJOUR`** + appartenance au séjour.
- **Path** : **`jour`** en date ISO (`yyyy-MM-dd`).
- **Body** : `ModifierMaPresenceCelluleMembreEquipeRequest` — **`present`** (`Boolean`, obligatoire : `true` = inscription, `false` = désinscription).
- **Réponse** : **`PlanningCelluleDto`** (200) si une cellule subsiste après l’opération (inchangée si désinscription alors que le connecté n’était pas inscrit) ; **`204 No Content`** si la cellule est supprimée après retrait du dernier inscrit, ou s’il n’existait aucune cellule pour ce jour et que `present` est `false` (réponse vide idempotente).
- **Codes d'erreur** : **`403`** : type de cellule différent de membre d’équipe, ou non rattaché au séjour ; **`404`** : séjour, grille ou ligne ; **`400`** : contraintes métier (ex. participant non valide pour le séjour).
- **Front** : pour un membre d’équipe **sans** **`GESTION_SEJOURS`**, ne pas appeler **PUT** `/cellules` avec une liste d’animateurs : utiliser **PATCH** `ma-presence` ; dans la modale cellule, afficher les autres animateurs **non modifiables** (cases désactivées), seule la case du **connecté** est éditable.

**Persistance (réf.)** : jointures `planning_cellule_utilisateur`, `planning_cellule_horaire`, `planning_cellule_moment`, `planning_cellule_groupe`, `planning_cellule_lieu`. Anciennes colonnes `horaire_id` / … sur `planning_cellule` : hors mapping ; migration manuelle possible (reprise puis `DROP FOREIGN KEY` + `DROP COLUMN`) — voir [etat-projet.md](./etat-projet.md) (entités).

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
- **Description** : Modifier un utilisateur (soi-même, par admin, ou par directeur pour un membre d'équipe)
- **Autorisation** :
  - **`GESTION_UTILISATEURS`** (admin) : modification complète de tout utilisateur
  - **`DIRECTION`** modifiant un autre utilisateur **`BASIC_USER`** : modification du profil, **y compris l'email**
  - Utilisateur connecté modifiant **son propre** profil : nom, prénom, genre, téléphone, date de naissance — **pas l'email**
- **Body** : `UpdateUserRequest`
- **Réponse** : `ProfilUtilisateurDTO` (200 OK)
- **Règle email** :
  - Un **directeur** ne peut **pas** modifier **son propre** email (réservé à l'admin)
  - Un **membre d'équipe** (`BASIC_USER`) ne peut **pas** modifier **son propre** email (réservé à un directeur ou à l'admin)
- **Codes d'erreur** :
  - `400` : Validation échouée
  - `403` : Tentative de modification de l'email sans droit (`AccessDeniedException`, message : « Vous n'êtes pas autorisé à modifier l'adresse email »)
  - `404` : Utilisateur non trouvé
  - `409` : Email déjà utilisé par un autre compte

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

**403 Autorisation Spring Security (`ErrorResponse`)** — refus JWT / rôles / privilèges (hors flux token ci-dessus) : même enveloppe **`ErrorResponse`** (`status`, `error` = `Forbidden`, `timestamp`, `message`, `path`). Lorsque la cause est une **`AccessDeniedException`** avec un message technique peu utile (*Access is denied*, etc.), le backend substitue un **`message` en français** selon la ressource appelée (ex. gestion globale des séjours réservée aux **admins plateforme**, référentiel **`references-alimentaires`** global, **`/utilisateurs`** et recherche pour l’équipe, consultation des séjours d’un autre utilisateur, modification du **dossier santé** sans droit sanitaire/direction, autres opérations sous **`/sejours/...`** réservées au **directeur** ou **adjoint** avec droits de gestion). Les détails d’implémentation : `CustomAccessDeniedHandler` (filtre) + **`GlobalExceptionHandler`** pour les refus **`@PreAuthorize`**.

#### Codes d'Erreur HTTP

- **200 OK** : Requête réussie
- **201 Created** : Ressource créée avec succès
- **204 No Content** : Opération réussie sans contenu à retourner
- **400 Bad Request** : Requête invalide (validation, argument illégal, exception utilisateur)
- **401 Unauthorized** : Authentification requise (non authentifié)
- **403 Forbidden** : Autorisation Spring insuffisante (corps **`ErrorResponse`** ; message parfois contextualisé — voir section *403 Autorisation Spring Security*) ; refresh token invalide (`TokenException`) ; autres cas 403 documentés par endpoint
- **404 Not Found** : Ressource non trouvée
- **409 Conflict** : Ressource déjà existante (email utilisé, membre déjà dans équipe, enfant déjà inscrit au séjour)

### Types de Données

Tous les types TypeScript sont définis dans `enjoyWebApp/src/types/api.d.ts` :
- `SejourDTO`
- `ProfilUtilisateurDTO`
- `EnfantDto` (note: 'd' minuscule pour correspondre au nom Java `EnfantDto`)
- `DossierEnfantDto` (**`allergenes`**, **`regimesEtPreferences`** : `ReferenceAlimentaireDto[]`)
- `ReferenceAlimentaireDto` (`id`, **`type`** (`TypeReferenceAlimentaire`), `libelle`, `ordre`, `actif`)
- `MenuRepasDto`, `SaveMenuRepasRequest` (**`typeRepas`** : `TypeRepas`, **`allergeneIds`**, **`regimePreferenceIds`**)
- `SaveReferenceAlimentaireRequest`, `UpdateReferenceAlimentaireRequest`
- `ReferencesAlimentairesAgregeesEnfantsDto`
- `GroupeDto`, `CreateGroupeRequest`, `AjouterReferentRequest`
- `LieuDto`, `SaveLieuRequest`, `EmplacementLieu`, **`UsageLieu`** (enum API — **à ajouter / aligner** dans `api.d.ts` : **`usages`** sur lieux)
- `HoraireDto`, `SaveHoraireRequest` (à ajouter dans `api.d.ts` si le frontend gère les horaires)
- `MomentDto` (**`ordre`**), `SaveMomentRequest`, `ReorderMomentsRequest` (**`momentIds`**)
- **`ReunionDto`** (`id`, `sejourId`, **`date`** `yyyy-MM-dd`, **`ordreDuJour`** optionnel, **`contenu`** objet JSON TipTap), **`SaveReunionRequest`** — **à aligner dans `api.d.ts`**
- `ActiviteDto` (**`moment`**, **`lieu`**, **`typeActivite`**, **`avertissementLieu`**, `groupeIds`), `CreateActiviteRequest`, `UpdateActiviteRequest` (**`typeActiviteId`** obligatoire)
- `TypeActiviteDto` (**`sejourId`**, **`predefini`**), `SaveTypeActiviteRequest`
- `HistoriqueModificationActiviteDto`, `HistoriqueModificationPlanningCelluleDto`, **`HistoriqueModificationCahierInfirmerieDto`** (**`cahierInfirmerieEntreeId`**), `HistoriqueModificationBaseDto` (inclut **`type`** dont **`CAHIER_INFIRMERIE`**, **`ancienneValeur`** et **`nouvelleValeur`** : texte lisible pour l’historique ; pas du JSON binaire)
- **`CahierInfirmerieEntreeDto`** (**`createurTokenId`**, **`createurNom`**, **`createurPrenom`**, **`soigneurTokenId`**, **`soigneurNom`**, **`soigneurPrenom`**, **`temperatureCelsius`** : `BigDecimal` ou `null`, `enfantId`, enums **`TypeSoinInfirmerie`**, **`TypeAppelInfirmerie`**), **`SaveCahierInfirmerieEntreeRequest`**
- `PlanningGrilleSummaryDto`, `PlanningGrilleDetailDto`, `PlanningLigneDto`, **`PlanningCelluleDto`** (listes **`horaireIds`**, **`horaireLibelles`**, **`momentIds`**, **`groupeIds`**, **`lieuIds`**, **`membreTokenIds`**), `UpsertPlanningCellulesRequest`, **`PlanningCellulePayload`**
- `CreateSejourRequest`
- `CreateEnfantRequest`
- `UpdateDossierEnfantRequest` (**`allergeneIds`**, **`regimePreferenceIds`** optionnels)
- `MembreEquipeRequest` (POST `.../equipe/existant`)
- `UpdateMembreEquipeRoleRequest` (PUT `.../equipe/{membreTokenId}`)
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

