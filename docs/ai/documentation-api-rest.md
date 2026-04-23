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

