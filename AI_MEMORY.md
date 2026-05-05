# AI MEMORY BANK

Point d’entrée unique pour le **contexte projet** (assistants IA et humains). Le détail volumineux est découpé en fiches dans [`docs/ai/`](docs/ai/) — fichiers **versionnés** avec le dépôt, prêts à être commités et poussés.

## Protocole IA (voir [`.cursorrules`](.cursorrules))

- **Lecture minimale** : ce pivot, puis **systématiquement** [`docs/ai/contexte-actif.md`](docs/ai/contexte-actif.md) et [`docs/ai/decisions-architecturales.md`](docs/ai/decisions-architecturales.md) ; ensuite les autres fiches selon la tâche (**§7.1**).
- **Mise à jour / demande « mets à jour `AI_MEMORY.md` »** : traiter comme **toute** la Memory Bank (pivot + fiches `docs/ai/`) ; **avant** toute **nouvelle** fiche dans `docs/ai/`, appliquer **§5** (placement par défaut, pas de micro-fiches) — détail **§7.2**.
- **Après commit + push** significatif : aligner Memory Bank et éventuellement `.cursorrules` — **§8**.

## Carte des sections `.cursorrules`

| § | Thème |
|---|--------|
| **1** | Langue (FR), commits (EN), ton |
| **2** | `pom.xml`, Java 21 |
| **3** | Spring : injection, DTOs/records, `tokenId` API, contrôleurs / services |
| **4** | Qualité : nommage, imports, erreurs, Excel/locale, tests |
| **5** | **Documentation Markdown** : où ranger quoi dans `docs/ai/`, éviter fiches trop spécifiques |
| **6** | Messages de commit (format, types) |
| **7** | **AI Memory Bank** (lecture, mise à jour, réflexivité) |
| **8** | Post-commit / post-push |

## Invariants et règles d’exécution

Les conventions **obligatoires** (langue, injection, `tokenId` côté API, structure des contrôleurs, granularité doc **§5**, etc.) sont dans [`.cursorrules`](.cursorrules). Ce pivot oriente vers les fiches métier (tests, API, journal).

## Point saillant récent

- **2026-05-05** — **Suite de tests réalignée sur les nouvelles signatures (`tokenId`) — 363 tests OK** : Suite à l'introduction du paramètre **`String utilisateurTokenId`** sur de nombreuses méthodes de services (`listerActivitesDuSejour`, `getActivite`, `listerHorairesDuSejour`, `getHoraire`, `listerLieuxDuSejour`, `getLieu`, `getSejourById`, etc.) et au passage du constructeur **`SejourVerificationService(SejourRepository, UtilisateurRepository)`**, mise à jour systématique de l'ensemble de la suite de tests. Côté **services impl** (`ActiviteServiceImplTest`, `HoraireServiceImplTest`, `LieuServiceImplTest`, `GroupeServiceImplTest`, `PlanningGrilleServiceImplTest`, `SejourServiceImplTest`) : ajout d'un fixture **`appelantAdmin`** (Role.ADMIN, tokenId `"appelant-token"`) qui contourne le contrôle d'appartenance via stub **`utilisateurRepository.findByTokenId(...)`** ; pour les scénarios « séjour absent » utilisation explicite d'un user **non-ADMIN** afin de déclencher `verifierSejourExiste`. Côté **contrôleurs** (`ActiviteControllerTest`, `HoraireControllerTest`, `LieuControllerTest`, `EnfantControllerTest`, `GroupeControllerTest`, `SejourControllerTest`) : injection systématique d'un principal `Utilisateur` via **`UsernamePasswordAuthenticationToken`** + **`.principal(authentication)`** sur les requêtes MockMvc — sans quoi `authentication.getPrincipal()` levait un NPE en `standaloneSetup`. **363 tests passent**. Détail : [contexte-actif.md](docs/ai/contexte-actif.md).
- **2026-05-05** — **API Séjours par utilisateur** : Refonte de l'endpoint de récupération des séjours. Ancien endpoint `/api/v1/sejours/directeur/{tokenId}` remplacé par `/api/v1/sejours/utilisateur/{tokenId}` avec logique de droits : ADMIN récupère tous les séjours, autres utilisateurs récupèrent uniquement les séjours où ils sont directeur ou membre d'équipe. Nouvelle requête JPQL `findSejoursByUtilisateur` avec LEFT JOIN sur `equipeRoles`. Tests mis à jour. Documentation pour le frontend : [API_CHANGEMENT_SEJOURS_PAR_UTILISATEUR.md](docs/API_CHANGEMENT_SEJOURS_PAR_UTILISATEUR.md). Détail : [contexte-actif.md](docs/ai/contexte-actif.md).
- **2026-05-05** — **Renommage privilège et qualité du code** : Renommage de `GESTION_PLANNINGS` → `ACCES_SEJOUR` (nom plus représentatif du privilège d'accès de base aux données d'un séjour). Suppression des imports statiques dans `Role` et `RoleSejour` : utilisation explicite de `Privilege.` pour plus de clarté et de sécurité. Permissions finalisées : DIRECTION (CRUD complet sur équipe/enfants/groupes), AS avec `GESTION_SANITAIRE` (consultation + modification dossiers enfants), autres membres avec `ACCES_SEJOUR` (consultation seule). Vérification d'appartenance au séjour pour toutes les consultations. Détail : [decisions-architecturales.md](docs/ai/decisions-architecturales.md).
- **2026-05-05** — **Privilèges `RoleSejour` indépendants** : Les `RoleSejour` ont leurs propres privilèges, indépendants du `Role` global de l'utilisateur. ANIM/SB/AUTRE → ACCES_SEJOUR, AS → ACCES_SEJOUR + GESTION_SANITAIRE, ADJOINT → GESTION_SEJOURS + GESTION_SANITAIRE + ACCES_SEJOUR. Pas de rôle `SANITAIRE` dans `Role`. Les privilèges du `RoleSejour` ne sont utilisés que dans le contexte du séjour et ne modifient pas le `Role` global de l'utilisateur. Détail : [decisions-architecturales.md](docs/ai/decisions-architecturales.md).
- **2026-05-05** — Amélioration de la suppression de groupe : vérification des activités associées avant suppression avec message d'erreur explicite (HTTP 409). Détail : [contexte-actif.md](docs/ai/contexte-actif.md).
- **2026-05-04** — Menus par séjour (`MenuRepas`), référentiel **`ReferenceAlimentaire`** (allergènes / régimes-préférences), extension **`DossierEnfant`** et endpoint d’**agrégation** des tags enfants du séjour ; commit `feat: add menus CRUD, food references, and aggregated child tags`. Détail : journal [contexte-actif.md](docs/ai/contexte-actif.md), contrats [documentation-api-rest.md](docs/ai/documentation-api-rest.md), modèle [etat-projet.md](docs/ai/etat-projet.md).

## Fiches (`docs/ai/`)

| Fichier | Rôle (aligné **§5**) |
|--------|----------------------|
| [contexte-global-architecture.md](docs/ai/contexte-global-architecture.md) | Stack, build, architecture en couches, flux données |
| [contexte-actif.md](docs/ai/contexte-actif.md) | Phase courante, focus, journal des dernières modifications |
| [decisions-architecturales.md](docs/ai/decisions-architecturales.md) | DI, records/DTOs, services, exceptions, null-safety |
| [etat-projet.md](docs/ai/etat-projet.md) | Tests, entités, persistance, sync frontend |
| [documentation-api-rest.md](docs/ai/documentation-api-rest.md) | Endpoints REST, payloads, formats d’erreur, types |
| [roadmap.md](docs/ai/roadmap.md) | Complété / à faire |

Index narratif du dossier : [docs/ai/README.md](docs/ai/README.md) (inclut le guide réplication **frontend** : [`guide-replication-memory-bank-frontend.md`](docs/ai/guide-replication-memory-bank-frontend.md)).

## Documentation produit (`docs/` hors `ai/`)

Guides pour un autre dépôt ou public (ex. front) : les **lier** depuis la fiche `docs/ai/` la plus pertinente (**§5**). Ex. : `docs/frontend-creation-groupes.md`, `docs/frontend-planning-cellules-multiples.md`.

## Convention de maintenance

- **Résumé / fil** : [contexte-actif.md](docs/ai/contexte-actif.md) ; ajuster ce pivot si la liste des fiches change.
- **Détail** : enrichir la fiche thématique du tableau ci-dessus plutôt que regonfler ce fichier.
- **Nouvelle fiche `docs/ai/*.md`** : uniquement si aucune fiche du tableau ne couvre le domaine ; puis lien ici + [docs/ai/README.md](docs/ai/README.md) (**§5**, **§7.2**).
