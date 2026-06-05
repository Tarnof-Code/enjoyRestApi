package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.enums.HistoriqueModificationAction;
import com.tarnof.enjoyrestapi.payload.response.HistoriqueModificationActiviteDto;
import com.tarnof.enjoyrestapi.payload.response.HistoriqueModificationCahierInfirmerieDto;
import com.tarnof.enjoyrestapi.payload.response.HistoriqueModificationChambreDto;
import com.tarnof.enjoyrestapi.payload.response.HistoriqueModificationPlanningCelluleDto;

import java.time.LocalDate;
import java.util.List;

public interface HistoriqueModificationService {

    void enregistrerPlanningCellule(
            String modificateurTokenId,
            HistoriqueModificationAction action,
            int planningLigneId,
            LocalDate jour,
            int planningCelluleId,
            String ancienneValeur,
            String nouvelleValeur);

    void enregistrerActivite(
            String modificateurTokenId,
            HistoriqueModificationAction action,
            int activiteId,
            String ancienneValeur,
            String nouvelleValeur);

    void enregistrerCahierInfirmerie(
            String modificateurTokenId,
            HistoriqueModificationAction action,
            int entreeId,
            String ancienneValeur,
            String nouvelleValeur);

    void enregistrerChambre(
            String modificateurTokenId,
            HistoriqueModificationAction action,
            int chambreId,
            String ancienneValeur,
            String nouvelleValeur);

    List<HistoriqueModificationPlanningCelluleDto> listerHistoriquePlanningCellules(
            int sejourId, int grilleId, int ligneId, LocalDate jour, String utilisateurTokenId);

    List<HistoriqueModificationActiviteDto> listerHistoriqueActivite(
            int sejourId, int activiteId, String utilisateurTokenId);

    List<HistoriqueModificationCahierInfirmerieDto> listerHistoriqueCahierInfirmerie(
            int sejourId, int entreeId, String utilisateurTokenId);

    List<HistoriqueModificationChambreDto> listerHistoriqueChambre(
            int sejourId, int chambreId, String utilisateurTokenId);
}
