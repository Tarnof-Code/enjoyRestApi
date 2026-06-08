# AI MEMORY BANK

Index court du contexte IA du dépôt Enjoy API. Pour une question ciblée, lire directement la fiche concernée ; ne pas pré-lire toute la Memory Bank.

## Quand Lire Quoi
- Architecture, stack, couches : `docs/ai/contexte-global-architecture.md`.
- Journal récent et focus courant : `docs/ai/contexte-actif.md`.
- Décisions transverses : `docs/ai/decisions-architecturales.md`.
- Modèle, tests, synchronisation frontend : `docs/ai/etat-projet.md`.
- Contrats HTTP et payloads : `docs/ai/documentation-api-rest.md`.
- Stockage photos (R2, config) : `docs/ai/stockage-photos-cloudflare-r2.md`.
- Suivi des tâches : `docs/ai/roadmap.md`.

## Rappels Essentiels
- Ne jamais lire ni documenter de secrets, `.env*`, credentials ou dumps.
- Références utilisateur dans les échanges JSON : `tokenId`, jamais l’id SQL.
- Code Java : injection par constructeur, contrôleurs minces, services sans erreurs masquées.
- **Photos profil animateur** : `POST/GET/DELETE .../utilisateurs/{tokenId}/photo-profil` ; **`ProfilDto.photoProfilUrl`** ; stockage R2 ou `./uploads` — détail : `documentation-api-rest.md`. ⚠️ Front : fetch blob + JWT, pas `<img src>` direct ; **`api.d.ts`**.
- **Activités** (internes) : **`enfantIds`** / **`enfants[]`**, conflit **`ENFANT_DEJA_AFFECTE_CRENEAU`** — `documentation-api-rest.md`.
- Mise à jour mémoire : `.cursor/rules/20-memory-bank.mdc` ; ce fichier reste entre 10 et 20 lignes.
