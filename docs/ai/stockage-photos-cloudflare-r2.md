<!-- Fiche Memory Bank — point d'entrée : [AI_MEMORY.md](../../AI_MEMORY.md) -->

# Stockage photos — Cloudflare R2

Guide pas à pas pour créer un compte et brancher **Enjoy API** au stockage en ligne des photos de profil animateur.

## Pourquoi Cloudflare R2 ?

- **Gratuit durablement** : ~10 Go de stockage / mois, sans limite de durée (contrairement à AWS S3).
- **Pas de frais de sortie** : le front peut afficher les photos sans surcoût bande passante.
- **Compatible S3** : le backend Java utilise le SDK AWS S3 standard.
- **Évolutif** : la même abstraction servira plus tard pour les **photos dossier enfant** (`enfants/{id}/photo-profil.ext`).

Alternative si besoin de redimensionnement automatique : **Cloudinary** (tier gratuit plus limité).

---

## Étape 1 — Créer un compte Cloudflare

1. Ouvrir [https://dash.cloudflare.com/sign-up](https://dash.cloudflare.com/sign-up).
2. S'inscrire avec une adresse e-mail et un mot de passe.
3. Confirmer l'e-mail reçu de Cloudflare.
4. Se connecter au [tableau de bord Cloudflare](https://dash.cloudflare.com/).

> Aucune carte bancaire n'est requise pour R2 dans le cadre du tier gratuit.

---

## Étape 2 — Activer R2

1. Dans le menu latéral gauche, cliquer sur **R2 Object Storage**.
2. Si c'est la première fois, accepter les conditions d'utilisation R2.
3. Choisir le **plan Workers Free** si Cloudflare le propose (R2 est inclus).

---

## Étape 3 — Créer un bucket (dossier racine)

1. Dans **R2**, cliquer sur **Create bucket**.
2. Nom du bucket : `enjoy-photos` (ou autre nom, à reporter dans la config).
3. **Location** : choisir **Automatic** ou une région **Europe (EU)** si disponible (RGPD).
4. Laisser le bucket **privé** (ne pas activer l'accès public).
5. Valider avec **Create bucket**.

Les fichiers seront organisés ainsi dans le bucket :

```
utilisateurs/{tokenId}/photo-profil.jpg
enfants/{enfantId}/photo-profil.jpg   ← futur
```

---

## Étape 4 — Créer des clés d'API (identifiants S3)

1. Dans **R2**, ouvrir **Manage R2 API Tokens** (lien en haut à droite de la page R2).
2. Cliquer **Create API token**.
3. Nom : `enjoy-api-dev` (ou `enjoy-api-prod`).
4. Permissions : **Object Read & Write** sur le bucket `enjoy-photos` (ou **Admin Read & Write** pour simplifier en dev).
5. Créer le token.
6. **Noter immédiatement** (affiché une seule fois) :
   - **Access Key ID**
   - **Secret Access Key**
   - **Endpoint S3** (format : `https://<ACCOUNT_ID>.r2.cloudflarestorage.com`)

---

## Étape 5 — Configurer Enjoy API

### Variables d'environnement (recommandé)

Ne jamais committer les clés. Définir :

| Variable | Exemple | Description |
|---|---|---|
| `R2_ENABLED` | `true` | Active le stockage R2 (sinon : disque local `./uploads`) |
| `R2_ENDPOINT` | `https://abc123.r2.cloudflarestorage.com` | Endpoint S3 Cloudflare |
| `R2_BUCKET` | `enjoy-photos` | Nom du bucket |
| `R2_ACCESS_KEY` | *(Access Key ID)* | Clé publique |
| `R2_SECRET_KEY` | *(Secret Access Key)* | Clé secrète |

Sous Windows (PowerShell, session courante) :

```powershell
$env:R2_ENABLED="true"
$env:R2_ENDPOINT="https://VOTRE_ACCOUNT_ID.r2.cloudflarestorage.com"
$env:R2_BUCKET="enjoy-photos"
$env:R2_ACCESS_KEY="votre_access_key"
$env:R2_SECRET_KEY="votre_secret_key"
```

### Sans compte R2 (développement local)

Par défaut `R2_ENABLED=false` : les photos sont stockées dans `./uploads/` sur le disque. Aucun compte cloud requis pour coder et tester.

---

## Étape 6 — Vérifier que ça fonctionne

1. Démarrer l'API Spring Boot.
2. Se connecter (JWT).
3. Envoyer une photo :

```http
POST /api/v1/utilisateurs/{tokenId}/photo-profil
Content-Type: multipart/form-data
Authorization: Bearer <token>

file: (image JPEG/PNG/WebP, max 2 Mo)
```

4. Vérifier la réponse `ProfilDto` : le champ `photoProfilUrl` doit valoir `/api/v1/utilisateurs/{tokenId}/photo-profil`.
5. Afficher la photo :

```http
GET /api/v1/utilisateurs/{tokenId}/photo-profil
Authorization: Bearer <token>
```

6. Dans le dashboard Cloudflare R2 → bucket `enjoy-photos` → **Objects** : le fichier `utilisateurs/.../photo-profil.jpg` doit apparaître.

---

## Étape 7 — Suite : photo dossier enfant

Le service `ObjectStorageService` est prévu pour être étendu :

- Ajouter `buildPhotoProfilEnfantKey(enfantId, extension)` → `enfants/{id}/photo-profil.ext`
- Colonnes `photo_profil_cle` / `photo_profil_mime_type` sur `Enfant` ou `DossierEnfant`
- Endpoints miroir sous `/api/v1/sejours/{sejourId}/enfants/{enfantId}/photo-profil`
- Droits : `verifierDroitGestionSejour` + `GESTION_SANITAIRE` selon le cas métier

---

## Sécurité / RGPD

- Bucket **privé** : les photos ne sont accessibles que via l'API authentifiée.
- Suppression utilisateur : la photo R2 est supprimée avec le compte.
- Hébergement **EU** si possible pour les données de mineurs.
- Taille max **2 Mo**, formats **JPEG / PNG / WebP** uniquement.
