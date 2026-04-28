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
