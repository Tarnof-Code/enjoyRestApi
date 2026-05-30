# AI MEMORY BANK

Index court du contexte IA du dépôt Enjoy API. Pour une question ciblée, lire directement la fiche concernée ; ne pas pré-lire toute la Memory Bank.

## Quand Lire Quoi
- Architecture, stack, couches : `docs/ai/contexte-global-architecture.md`.
- Journal récent et focus courant : `docs/ai/contexte-actif.md`.
- Décisions transverses : `docs/ai/decisions-architecturales.md`.
- Modèle, tests, synchronisation frontend : `docs/ai/etat-projet.md`.
- Contrats HTTP et payloads : `docs/ai/documentation-api-rest.md`.
- Suivi des tâches : `docs/ai/roadmap.md`.

## Rappels Essentiels
- Ne jamais lire ni documenter de secrets, `.env*`, credentials ou dumps.
- Références utilisateur dans les échanges JSON : `tokenId`, jamais l’id SQL.
- Code Java : injection par constructeur, contrôleurs minces, services sans erreurs masquées.
- **Activités prestataires** (sorties) : CRUD `/sejours/{id}/activites-prestataires`, `moments[]`, `nonParticipations`, anti-doublon date+moment+groupe — détail : `docs/ai/documentation-api-rest.md`.
- **Chambres** (hébergement séjour) : CRUD + affectation occupants ; **écriture** **`ACCES_SEJOUR`** + appartenance au séjour (toute l’équipe) — détail : `docs/ai/documentation-api-rest.md`. ⚠️ Front : aligner `api.d.ts`.
- Mise à jour mémoire : appliquer `.cursor/rules/20-memory-bank.mdc` et garder ce fichier entre 10 et 20 lignes.
