# Guide : Memory Bank IA pour le dépôt frontend

Ce document est **autonome** : vous pouvez le copier dans le dépôt frontend (ex. `docs/GUIDE_MEMORY_BANK.md` ou le garder tel quel) et l’utiliser comme cahier des charges pour aligner la documentation « contexte IA » sur le modèle du backend **enjoyApi** (pivot `AI_MEMORY.md` + dossier `docs/ai/`).

## Objectif

- **Un fichier pivot** à la racine (`AI_MEMORY.md`) : court, toujours lisible en une fois, avec liens vers le détail.
- **Des fiches thématiques** dans `docs/ai/` : historique, conventions, état des écrans, sync API, roadmap — tout ce qui est trop long pour le pivot.
- **Même logique de mise à jour** : quand quelqu’un demande « mets à jour `AI_MEMORY.md` », cela signifie mettre à jour **le pivot et les fiches concernées**, ou **créer** une nouvelle fiche + la référencer dans le pivot et dans `docs/ai/README.md`.

Cela améliore la **maintenabilité** et donne aux assistants IA un **chemin de lecture** clair (pivot → fiches), sans un seul fichier gigantesque.

## Arborescence cible

```text
frontend-repo/
├── AI_MEMORY.md              # Pivot (table des matières + rappels)
├── .cursorrules              # Règles + protocole Memory Bank (recommandé)
├── docs/
│   └── ai/
│       ├── README.md         # Index des fiches
│       ├── contexte-global-stack.md    # à adapter : nom des fichiers ci-dessous
│       ├── contexte-actif.md
│       ├── decisions-architecturales.md
│       ├── etat-projet.md
│       ├── documentation-ui-routing.md   # ou api-consommation.md, etc.
│       └── roadmap.md
│   └── ...                   # autres docs produit (specs, maquettes)
```

Les **noms** des fiches sont indicatifs : adaptez-les à votre stack (ex. Next.js, Vite, React Router, TanStack Query).

## Contenu suggéré des fiches (frontend)

| Fichier | Rôle typique |
|--------|----------------|
| `contexte-global-stack.md` | Versions (Node, React, TypeScript), bundler, librairies UI, tests (Vitest, Playwright…), structure des dossiers (`src/`, features, composants). |
| `contexte-actif.md` | Sprint / phase en cours, **dernières modifications** (journal court), liens vers tickets si utile. |
| `decisions-architecturales.md` | Conventions de code (imports, état global, formulaires, erreurs réseau), nommage, règles d’accessibilité, i18n. |
| `etat-projet.md` | Couverture des tests, dette technique, modules sensibles, **alignement `api.d.ts` / contrat enjoyApi**. |
| `documentation-ui-routing.md` | Routes, gardes d’auth, pages clés, flux utilisateur direction / admin. |
| `roadmap.md` | Tâches faites / à faire côté front. |

Vous pouvez **fusionner** ou **éclater** (ex. `sync-backend-api.md` uniquement pour les types et endpoints).

## Modèle minimal pour `AI_MEMORY.md` (pivot)

À copier-coller et adapter :

```markdown
# AI MEMORY BANK (frontend)

Point d’entrée pour le contexte projet. Le détail est dans [`docs/ai/`](docs/ai/).

**Important (IA)** : ce pivot seul ne suffit pas. Lire aussi `docs/ai/contexte-actif.md` et `docs/ai/decisions-architecturales.md`, puis les autres fiches selon la tâche.

## Invariants

Les règles obligatoires (langue, lint, patterns) sont dans [`.cursorrules`](.cursorrules).

## Fiches (`docs/ai/`)

| Fichier | Rôle |
|--------|------|
| [contexte-global-stack.md](docs/ai/contexte-global-stack.md) | Stack, tooling, structure du repo |
| [contexte-actif.md](docs/ai/contexte-actif.md) | Phase, journal des changements récents |
| [decisions-architecturales.md](docs/ai/decisions-architecturales.md) | Conventions et choix techniques |
| [etat-projet.md](docs/ai/etat-projet.md) | Tests, dette, sync types API |
| [documentation-ui-routing.md](docs/ai/documentation-ui-routing.md) | Routes, écrans, auth |
| [roadmap.md](docs/ai/roadmap.md) | Suivi |

Index : [docs/ai/README.md](docs/ai/README.md).

## Alignement backend (enjoyApi)

- Types partagés : `src/types/api.d.ts` (ou équivalent) — rester aligné avec les DTO / payloads documentés côté API.
- Références utilisateur côté JSON : **`tokenId`**, pas l’id SQL interne (voir conventions enjoyApi).

## Convention de maintenance

- **« Mets à jour `AI_MEMORY.md` »** = mise à jour du **pivot + fiches concernées** ; créer une nouvelle fiche si besoin et la lister ici + dans `docs/ai/README.md`.
```

## Modèle minimal pour `docs/ai/README.md`

```markdown
# Memory Bank — fiches IA (`docs/ai/`)

Point d’entrée racine : [`AI_MEMORY.md`](../../AI_MEMORY.md).

(liste numérotée des fiches avec une phrase de description chacune)
```

## `.cursorrules` : protocole à calquer

Ajoutez une section du même esprit que le backend, par exemple :

1. **Lecture** : début de session ou tâche majeure → lire `AI_MEMORY.md`, puis **systématiquement** `docs/ai/contexte-actif.md` et `docs/ai/decisions-architecturales.md`, puis les fiches pertinentes.
2. **Mise à jour** : décision ou fin de tâche → mettre à jour le pivot (liens, résumé) et la **fiche** qui porte le détail.
3. **Demande explicite** : « mets à jour `AI_MEMORY.md` » → mettre à jour **toute** la Memory Bank (pivot + fiches), **créer** une fiche si aucune n’existe, **référencer** dans `AI_MEMORY.md` et `docs/ai/README.md`.
4. **Post-commit / post-push** : même discipline si des changements structurels le justifient.

Adaptez la langue (ex. réponses en français, commits en anglais) selon votre équipe.

## Fichier `.gitignore` : point d’attention

Si votre dépôt ignore tous les `*.md` sauf quelques exceptions (comme sur enjoyApi), **ajoutez une exception** pour que `AI_MEMORY.md` et `docs/ai/*.md` soient bien versionnés, par exemple :

```gitignore
*.md
!AI_MEMORY.md
!docs/ai/*.md
```

Ajustez si vous versionnez d’autres Markdown sous `docs/`.

## Synchronisation avec enjoyApi

- Quand l’API ajoute ou change des endpoints / DTOs, la fiche **`etat-projet.md`** (ou une fiche dédiée **`sync-api-enjoy.md`**) doit rappeler ce qui reste à faire côté front (`api.d.ts`, services, écrans).
- Vous pouvez **lier** vers la doc API du backend (`documentation-api-rest.md` dans le repo enjoyApi) sans dupliquer tout le contenu.

## Checklist de mise en place

1. Créer `docs/ai/` et `docs/ai/README.md`.
2. Créer `AI_MEMORY.md` à la racine du frontend (pivot).
3. Créer les fiches vides ou avec un premier contenu minimal (stack + contexte actif).
4. Ajouter / mettre à jour `.cursorrules` (protocole Memory Bank).
5. Vérifier `.gitignore` pour ne pas exclure ces fichiers.
6. Commit : `docs: introduce AI Memory Bank (pivot + docs/ai)`.

---

*Document fourni depuis le dépôt **enjoyApi** — à dupliquer ou déplacer librement dans le repo frontend.*
