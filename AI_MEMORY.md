# AI MEMORY BANK

Point d’entrée unique pour le **contexte projet** (assistants IA et humains). Le détail volumineux est découpé en fiches dans [`docs/ai/`](docs/ai/) — fichiers **versionnés** avec le dépôt, prêts à être commités et poussés.

**Important (IA)** : ce pivot seul ne remplace pas les fiches. Pour un contexte exploitable, suivre le §6 de `.cursorrules` (lecture du pivot **puis** au minimum `docs/ai/contexte-actif.md` et `docs/ai/decisions-architecturales.md`, puis les fiches utiles à la tâche).

## Invariants et règles d’exécution

Les conventions **obligatoires** (langue, injection, `tokenId` côté API, structure des contrôleurs, etc.) sont dans [`.cursorrules`](.cursorrules). Ce pivot complète par l’historique métier, l’état des tests et la doc d’API.

## Fiches (`docs/ai/`)

| Fichier | Rôle |
|--------|------|
| [contexte-global-architecture.md](docs/ai/contexte-global-architecture.md) | Stack, build, architecture en couches, flux données |
| [contexte-actif.md](docs/ai/contexte-actif.md) | Phase courante, focus, journal des dernières modifications |
| [decisions-architecturales.md](docs/ai/decisions-architecturales.md) | DI, records/DTOs, services, exceptions, null-safety |
| [etat-projet.md](docs/ai/etat-projet.md) | Tests (liste et commandes), entités, sync frontend, exceptions |
| [documentation-api-rest.md](docs/ai/documentation-api-rest.md) | Endpoints REST, formats d’erreur, rappel types |
| [roadmap.md](docs/ai/roadmap.md) | Complété / à faire |

Index narratif du dossier : [docs/ai/README.md](docs/ai/README.md).

## Documentation produit (autres fichiers)

Selon les besoins : `docs/planning-grilles-api.md`, `docs/frontend-creation-groupes.md`, etc. (à la racine de `docs/`).

## Convention de maintenance

- **Résumé** (phase, dernières modifs en bref) : tenir [contexte-actif.md](docs/ai/contexte-actif.md) à jour ; ajuster ce pivot si la structure des fiches change.
- **Détail** : enrichir la fiche thématique concernée (`docs/ai/…`) plutôt que de regonfler ce fichier.
- **Demande « mets à jour `AI_MEMORY.md` »** : interpréter comme une mise à jour de **l’ensemble du dispositif** (pivot + fiches). Mettre à jour les fiches `docs/ai/` qui correspondent au sujet ; **créer** une nouvelle fiche si le contenu ne rentre dans aucune fiche existante, puis ajouter son lien dans ce fichier et dans [docs/ai/README.md](docs/ai/README.md). Voir §6.2 de `.cursorrules`.
- Après commit / push significatif : voir §6–7 de `.cursorrules` (Memory Bank + règles).
