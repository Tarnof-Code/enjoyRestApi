<!-- Fiche Memory Bank — point d'entrée : [AI_MEMORY.md](../../AI_MEMORY.md) -->

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
- [ ] Tests d'intégration (Repository avec `@DataJpaTest`, End-to-End avec `@SpringBootTest`) — voir la section *Tests d'Intégration* dans [etat-projet.md](./etat-projet.md)

