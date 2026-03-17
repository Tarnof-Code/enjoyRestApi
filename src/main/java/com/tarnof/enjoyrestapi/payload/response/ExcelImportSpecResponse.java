package com.tarnof.enjoyrestapi.payload.response;

import java.util.List;

/**
 * Réponse contenant la spécification complète pour l'import Excel des enfants.
 * Permet au frontend d'afficher la notice (colonnes obligatoires, noms possibles).
 */
public record ExcelImportSpecResponse(
    List<ExcelImportColumnSpec> colonnesObligatoires,
    List<ExcelImportColumnSpec> colonnesOptionnelles,
    List<String> formatsAcceptes
) {}
