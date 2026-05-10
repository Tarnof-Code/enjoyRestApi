package com.tarnof.enjoyrestapi.payload.response;

import com.tarnof.enjoyrestapi.enums.HistoriqueModificationAction;
import com.tarnof.enjoyrestapi.enums.HistoriqueModificationType;

import java.time.Instant;

public record HistoriqueModificationBaseDto(
        int id,
        HistoriqueModificationType type,
        Instant dateModification,
        String modificateurTokenId,
        String modificateurNom,
        String modificateurPrenom,
        HistoriqueModificationAction action,
        String ancienneValeur,
        String nouvelleValeur) {}
