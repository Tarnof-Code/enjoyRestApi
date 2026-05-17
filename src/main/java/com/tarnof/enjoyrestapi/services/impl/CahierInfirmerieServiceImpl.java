package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.CahierInfirmerieEntree;
import com.tarnof.enjoyrestapi.entities.Enfant;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.SejourEnfant;
import com.tarnof.enjoyrestapi.entities.SejourEnfantId;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.HistoriqueModificationAction;
import com.tarnof.enjoyrestapi.enums.TypeAppelInfirmerie;
import com.tarnof.enjoyrestapi.enums.TypeSoinInfirmerie;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.SaveCahierInfirmerieEntreeRequest;
import com.tarnof.enjoyrestapi.payload.response.CahierInfirmerieEntreeDto;
import com.tarnof.enjoyrestapi.repositories.CahierInfirmerieEntreeRepository;
import com.tarnof.enjoyrestapi.repositories.SejourEnfantRepository;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.services.CahierInfirmerieService;
import com.tarnof.enjoyrestapi.services.HistoriqueModificationService;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CahierInfirmerieServiceImpl implements CahierInfirmerieService {

    private final CahierInfirmerieEntreeRepository cahierInfirmerieEntreeRepository;
    private final SejourEnfantRepository sejourEnfantRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SejourVerificationService sejourVerificationService;
    private final HistoriqueModificationService historiqueModificationService;

    public CahierInfirmerieServiceImpl(
            CahierInfirmerieEntreeRepository cahierInfirmerieEntreeRepository,
            SejourEnfantRepository sejourEnfantRepository,
            UtilisateurRepository utilisateurRepository,
            SejourVerificationService sejourVerificationService,
            HistoriqueModificationService historiqueModificationService) {
        this.cahierInfirmerieEntreeRepository = cahierInfirmerieEntreeRepository;
        this.sejourEnfantRepository = sejourEnfantRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.sejourVerificationService = sejourVerificationService;
        this.historiqueModificationService = historiqueModificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CahierInfirmerieEntreeDto> listerEntreesDuSejour(int sejourId, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        return cahierInfirmerieEntreeRepository.findBySejourIdWithEnfantOrderByDateHeureDesc(sejourId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CahierInfirmerieEntreeDto getEntree(int sejourId, int entreeId, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        CahierInfirmerieEntree entree = chargerEntreePourLecture(sejourId, entreeId);
        return mapToDto(entree);
    }

    @Override
    @Transactional
    public CahierInfirmerieEntreeDto creerEntree(
            int sejourId, SaveCahierInfirmerieEntreeRequest request, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        validerPrecisionsAutre(request);
        SejourEnfant liaison = chargerLiaisonEnfantSejour(sejourId, request.enfantId());
        Enfant enfant = liaison.getEnfant();
        if (enfant == null) {
            throw new ResourceNotFoundException("Enfant non trouvé avec l'ID: " + request.enfantId());
        }
        Sejour sejour = liaison.getSejour();
        if (sejour == null) {
            throw new ResourceNotFoundException("Séjour non trouvé avec l'ID: " + sejourId);
        }
        Utilisateur createur =
                utilisateurRepository
                        .findByTokenId(utilisateurTokenId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Utilisateur non trouvé avec le token ID: " + utilisateurTokenId));
        Utilisateur soigneur =
                sejourVerificationService.exigerUtilisateurDirecteurOuMembreEquipeDuSejour(
                        sejourId, request.soigneurTokenId());

        CahierInfirmerieEntree entree = new CahierInfirmerieEntree();
        entree.setSejour(sejour);
        entree.setEnfant(enfant);
        entree.setCreateur(createur);
        entree.setSoigneur(soigneur);
        entree.setDateHeure(request.dateHeure());
        entree.setDescription(request.description().trim());
        entree.setLocalisationCorps(blankToNull(request.localisationCorps()));
        entree.setSoins(new LinkedHashSet<>(request.soins()));
        entree.setSoinsAutrePrecision(blankToNull(request.soinsAutrePrecision()));
        entree.setTemperatureCelsius(temperaturePourPersistance(request));
        entree.setAppels(new LinkedHashSet<>(request.appels()));
        entree.setAppelAutrePrecision(blankToNull(request.appelAutrePrecision()));

        CahierInfirmerieEntree sauve = cahierInfirmerieEntreeRepository.save(entree);
        historiqueModificationService.enregistrerCahierInfirmerie(
                utilisateurTokenId,
                HistoriqueModificationAction.CREATION,
                sauve.getId(),
                null,
                libelleEntreePourHistorique(sauve));
        return mapToDto(sauve);
    }

    @Override
    @Transactional
    public CahierInfirmerieEntreeDto modifierEntree(
            int sejourId, int entreeId, SaveCahierInfirmerieEntreeRequest request, String utilisateurTokenId) {
        CahierInfirmerieEntree entree = chargerEntreePourMutation(sejourId, entreeId);
        sejourVerificationService.verifierDroitModificationEntreeCahierInfirmerie(sejourId, entree, utilisateurTokenId);
        validerPrecisionsAutre(request);
        SejourEnfant liaison = chargerLiaisonEnfantSejour(sejourId, request.enfantId());
        Enfant enfant = liaison.getEnfant();
        if (enfant == null) {
            throw new ResourceNotFoundException("Enfant non trouvé avec l'ID: " + request.enfantId());
        }

        Sejour sejour = entree.getSejour();
        if (sejour == null) {
            throw new ResourceNotFoundException("Séjour non trouvé avec l'ID: " + sejourId);
        }
        Utilisateur soigneur =
                sejourVerificationService.exigerUtilisateurDirecteurOuMembreEquipeDuSejour(
                        sejourId, request.soigneurTokenId());

        String signatureAvant = signatureTechniqueEntree(entree);
        String ancienneValeur = libelleEntreePourHistorique(entree);

        entree.setEnfant(enfant);
        entree.setSoigneur(soigneur);
        entree.setDateHeure(request.dateHeure());
        entree.setDescription(request.description().trim());
        entree.setLocalisationCorps(blankToNull(request.localisationCorps()));
        entree.setSoins(new LinkedHashSet<>(request.soins()));
        entree.setSoinsAutrePrecision(blankToNull(request.soinsAutrePrecision()));
        entree.setTemperatureCelsius(temperaturePourPersistance(request));
        entree.setAppels(new LinkedHashSet<>(request.appels()));
        entree.setAppelAutrePrecision(blankToNull(request.appelAutrePrecision()));

        CahierInfirmerieEntree sauve = cahierInfirmerieEntreeRepository.save(entree);
        if (!signatureAvant.equals(signatureTechniqueEntree(sauve))) {
            historiqueModificationService.enregistrerCahierInfirmerie(
                    utilisateurTokenId,
                    HistoriqueModificationAction.MODIFICATION,
                    sauve.getId(),
                    ancienneValeur,
                    libelleEntreePourHistorique(sauve));
        }
        return mapToDto(sauve);
    }

    @Override
    @Transactional
    public void supprimerEntree(int sejourId, int entreeId, String utilisateurTokenId) {
        CahierInfirmerieEntree entree = chargerEntreePourMutation(sejourId, entreeId);
        sejourVerificationService.verifierDroitSuppressionEntreeCahierInfirmerie(sejourId, entree, utilisateurTokenId);
        int idSupprimee = entree.getId();
        String ancienneValeur = libelleEntreePourHistorique(entree);
        historiqueModificationService.enregistrerCahierInfirmerie(
                utilisateurTokenId, HistoriqueModificationAction.SUPPRESSION, idSupprimee, ancienneValeur, null);
        cahierInfirmerieEntreeRepository.delete(entree);
    }

    private CahierInfirmerieEntree chargerEntreePourLecture(int sejourId, int entreeId) {
        return cahierInfirmerieEntreeRepository
                .findByIdAndSejourIdWithEnfantAndCreateur(entreeId, sejourId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Entrée de cahier d'infirmerie non trouvée : " + entreeId));
    }

    private CahierInfirmerieEntree chargerEntreePourMutation(int sejourId, int entreeId) {
        return chargerEntreePourLecture(sejourId, entreeId);
    }

    private SejourEnfant chargerLiaisonEnfantSejour(int sejourId, int enfantId) {
        return sejourEnfantRepository
                .findById(new SejourEnfantId(sejourId, enfantId))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "L'enfant n'est pas inscrit à ce séjour ou est introuvable."));
    }

    private static void validerPrecisionsAutre(SaveCahierInfirmerieEntreeRequest request) {
        if (request.soins().contains(TypeSoinInfirmerie.AUTRE)
                && isBlank(request.soinsAutrePrecision())) {
            throw new IllegalArgumentException(
                    "La précision pour le soin « autre » est obligatoire lorsque « AUTRE » est sélectionné.");
        }
        if (request.appels().contains(TypeAppelInfirmerie.AUTRE)
                && isBlank(request.appelAutrePrecision())) {
            throw new IllegalArgumentException(
                    "La précision pour l'appel « autre » est obligatoire lorsque « AUTRE » est sélectionné.");
        }
        validerMesureTemperature(request);
    }

    private static void validerMesureTemperature(SaveCahierInfirmerieEntreeRequest request) {
        boolean priseTemperature = request.soins().contains(TypeSoinInfirmerie.PRISE_TEMPERATURE);
        BigDecimal mesure = request.temperatureCelsius();
        if (priseTemperature) {
            if (mesure == null) {
                throw new IllegalArgumentException(
                        "La mesure de température en °C (nombre décimal) est obligatoire lorsque « PRISE_TEMPERATURE » est sélectionné.");
            }
            if (mesure.scale() > 2) {
                throw new IllegalArgumentException("La température peut comporter au plus 2 décimales.");
            }
            if (mesure.compareTo(BigDecimal.valueOf(30)) < 0 || mesure.compareTo(BigDecimal.valueOf(45)) > 0) {
                throw new IllegalArgumentException("La température doit être comprise entre 30 et 45 °C.");
            }
        } else if (mesure != null) {
            throw new IllegalArgumentException(
                    "Ne renseignez « temperatureCelsius » que si « PRISE_TEMPERATURE » figure parmi les soins.");
        }
    }

    private static BigDecimal temperaturePourPersistance(SaveCahierInfirmerieEntreeRequest request) {
        if (!request.soins().contains(TypeSoinInfirmerie.PRISE_TEMPERATURE)) {
            return null;
        }
        return request.temperatureCelsius();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String blankToNull(String s) {
        return isBlank(s) ? null : s.trim();
    }

    private static String nullToDash(String s) {
        if (s == null) {
            return "-";
        }
        String t = s.trim();
        return t.isEmpty() ? "-" : t;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private String signatureTechniqueEntree(CahierInfirmerieEntree e) {
        int enfantId = e.getEnfant() == null ? -1 : e.getEnfant().getId();
        String soins =
                e.getSoins() == null
                        ? ""
                        : e.getSoins().stream()
                                .sorted(Comparator.comparing(TypeSoinInfirmerie::name))
                                .map(TypeSoinInfirmerie::name)
                                .collect(Collectors.joining(","));
        String appels =
                e.getAppels() == null
                        ? ""
                        : e.getAppels().stream()
                                .sorted(Comparator.comparing(TypeAppelInfirmerie::name))
                                .map(TypeAppelInfirmerie::name)
                                .collect(Collectors.joining(","));
        String soigneurToken = e.getSoigneur() == null ? "" : nullToEmpty(e.getSoigneur().getTokenId());
        return String.join(
                "|",
                String.valueOf(enfantId),
                String.valueOf(e.getDateHeure()),
                nullToEmpty(e.getDescription()),
                e.getLocalisationCorps() == null ? "" : e.getLocalisationCorps(),
                soins,
                e.getSoinsAutrePrecision() == null ? "" : e.getSoinsAutrePrecision(),
                e.getTemperatureCelsius() == null ? "" : e.getTemperatureCelsius().toPlainString(),
                appels,
                e.getAppelAutrePrecision() == null ? "" : e.getAppelAutrePrecision(),
                soigneurToken);
    }

    private String libelleEntreePourHistorique(CahierInfirmerieEntree e) {
        Enfant enfant = e.getEnfant();
        String soinsStr =
                e.getSoins() == null || e.getSoins().isEmpty()
                        ? "-"
                        : e.getSoins().stream()
                                .sorted(Comparator.comparing(TypeSoinInfirmerie::name))
                                .map(TypeSoinInfirmerie::name)
                                .collect(Collectors.joining(", "));
        String appelsStr =
                e.getAppels() == null || e.getAppels().isEmpty()
                        ? "-"
                        : e.getAppels().stream()
                                .sorted(Comparator.comparing(TypeAppelInfirmerie::name))
                                .map(TypeAppelInfirmerie::name)
                                .collect(Collectors.joining(", "));
        Utilisateur soigneur = e.getSoigneur();
        return "Enfant: "
                + libelleEnfantPourHistorique(enfant)
                + " | Date/heure: "
                + e.getDateHeure()
                + " | Description: "
                + nullToEmpty(e.getDescription())
                + " | Localisation: "
                + nullToDash(e.getLocalisationCorps())
                + " | Soins: "
                + soinsStr
                + (e.getSoinsAutrePrecision() == null || e.getSoinsAutrePrecision().isBlank()
                        ? ""
                        : " (autre: " + e.getSoinsAutrePrecision().trim() + ")")
                + (e.getTemperatureCelsius() == null
                        ? ""
                        : " | Temp. " + e.getTemperatureCelsius().toPlainString() + " °C")
                + " | Appels: "
                + appelsStr
                + (e.getAppelAutrePrecision() == null || e.getAppelAutrePrecision().isBlank()
                        ? ""
                        : " (autre: " + e.getAppelAutrePrecision().trim() + ")")
                + " | Soigneur: "
                + libelleUtilisateurPourHistorique(soigneur);
    }

    private static String libelleEnfantPourHistorique(Enfant enfant) {
        if (enfant == null) {
            return "?";
        }
        String p = enfant.getPrenom() != null ? enfant.getPrenom().trim() : "";
        String n = enfant.getNom() != null ? enfant.getNom().trim() : "";
        String s = (p + " " + n).trim();
        return s.isEmpty() ? "?" : s;
    }

    private static String libelleUtilisateurPourHistorique(Utilisateur u) {
        if (u == null) {
            return "?";
        }
        String p = u.getPrenom() != null ? u.getPrenom().trim() : "";
        String n = u.getNom() != null ? u.getNom().trim() : "";
        String s = (p + " " + n).trim();
        return s.isEmpty() ? "?" : s;
    }

    private CahierInfirmerieEntreeDto mapToDto(CahierInfirmerieEntree entree) {
        Enfant enfant = entree.getEnfant();
        Utilisateur createur = entree.getCreateur();
        Utilisateur soigneur = entree.getSoigneur();
        Set<TypeSoinInfirmerie> soins =
                entree.getSoins() == null ? Set.of() : Set.copyOf(entree.getSoins());
        Set<TypeAppelInfirmerie> appels =
                entree.getAppels() == null ? Set.of() : Set.copyOf(entree.getAppels());
        return new CahierInfirmerieEntreeDto(
                entree.getId(),
                entree.getSejour().getId(),
                enfant.getId(),
                enfant.getNom(),
                enfant.getPrenom(),
                createur != null ? createur.getTokenId() : null,
                createur != null ? createur.getNom() : null,
                createur != null ? createur.getPrenom() : null,
                entree.getDateHeure(),
                entree.getDescription(),
                entree.getLocalisationCorps(),
                soins,
                entree.getSoinsAutrePrecision(),
                entree.getTemperatureCelsius(),
                appels,
                entree.getAppelAutrePrecision(),
                soigneur != null ? soigneur.getTokenId() : null,
                soigneur != null ? soigneur.getNom() : null,
                soigneur != null ? soigneur.getPrenom() : null);
    }
}
