package com.tarnof.enjoyrestapi.payload.response;

import java.util.List;

/**
 * Spécification d'une colonne pour l'import Excel.
 * Utilisé pour documenter les colonnes attendues à l'API frontend.
 */
public record ExcelImportColumnSpec(
    String champ,
    String libelle,
    List<String> motsCles,
    boolean obligatoire
) {}
