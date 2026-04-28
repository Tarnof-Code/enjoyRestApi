<!-- Fiche Memory Bank — point d'entrée : [AI_MEMORY.md](../../AI_MEMORY.md) -->

## Contexte Global
- **Langage** : Java 21
- **Framework** : Spring Boot 3.5.6
- **Build Tool** : Maven
- **Packaging** : WAR (`<packaging>war</packaging>`)
- **Base de Données** : MySQL
- **Sécurité** : Spring Security avec JWT (jjwt)
- **Outils** : Jakarta Validation, Java Records — **pas de Lombok** dans le dépôt (`pom.xml` sans dépendance Lombok ; aucune annotation Lombok dans `src/`). Entités / services / contrôleurs : **constructeurs explicites** et POJO JPA classiques où pertinent (**`Lieu`**, **`Moment`**, **`Horaire`**, etc.).
- **Build** : `pom.xml` — Spring Boot parent **3.5.6**, `maven-compiler-plugin` hérité du parent (Java 21), **sans** `annotationProcessorPaths` Lombok.
- **Utilitaires** : `ExcelHelper` (import Excel), `DateFormatHelper` (`formatDdMmYyyy(LocalDate)` pour messages utilisateur en `dd/MM/yyyy`, refuse `null`)
- **Tests** : JUnit 5, Mockito

## Architecture
- **Pattern** : Layered Architecture (Controller -> Service Interface -> Service Impl -> Repository)
- **Data Flow** : 
  - `Controller` reçoit `RequestPayload` / renvoie `DTO` ou `ResponseEntity`
  - `Service` manipule `Entity` et convertit en `DTO`
  - `Repository` (JPA) gère la persistance
- **Plannings (direction)** : grilles par séjour (`PlanningGrille` / lignes / cellules) — endpoints et payloads dans [documentation-api-rest.md](./documentation-api-rest.md) (*Plannings — grilles*).
- **Gestion des Erreurs** : Centralisée via `@ControllerAdvice` (`GlobalExceptionHandler` dans `handlers/`)
- **Structure des packages** : `exceptions/` = classes d'exception lancées (`throw`), `handlers/` = handlers qui attrapent et formatent les réponses d'erreur HTTP
