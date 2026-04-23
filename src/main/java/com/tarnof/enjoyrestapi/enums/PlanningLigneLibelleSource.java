package com.tarnof.enjoyrestapi.enums;

/**
 * Type de libellé pour les lignes et type de contenu pour les cellules : défini sur {@link
 * com.tarnof.enjoyrestapi.entities.PlanningGrille} ({@code sourceLibelleLignes}, {@code sourceContenuCellules}). Pour
 * {@link #MEMBRE_EQUIPE} : contenu de cellule via {@code membreTokenIds} ; libellé de ligne via
 * {@code libelleUtilisateurTokenId} lorsque {@code sourceLibelleLignes} est {@link #MEMBRE_EQUIPE}.
 */
public enum PlanningLigneLibelleSource {
    SAISIE_LIBRE,
    GROUPE,
    LIEU,
    HORAIRE,
    MOMENT,
    /**
     * Cellule : au moins un token dans {@code membreTokenIds} ; libellé de ligne (grille) : {@code
     * libelleUtilisateurTokenId} ({@code Utilisateur#tokenId}).
     */
    MEMBRE_EQUIPE
}
