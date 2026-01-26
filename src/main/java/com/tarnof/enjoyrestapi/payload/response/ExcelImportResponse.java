package com.tarnof.enjoyrestapi.payload.response;

import java.util.List;

public record ExcelImportResponse(
    int totalLignes,
    int enfantsCrees,
    int enfantsDejaExistants,
    int erreurs,
    List<String> messagesErreur
) {}
