package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.Chambre;
import com.tarnof.enjoyrestapi.entities.ChambreOccupant;
import com.tarnof.enjoyrestapi.entities.Enfant;
import com.tarnof.enjoyrestapi.entities.Groupe;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.SejourEnfantId;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.HistoriqueModificationAction;
import com.tarnof.enjoyrestapi.enums.TypeChambre;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.AffecterOccupantChambreRequest;
import com.tarnof.enjoyrestapi.payload.request.AffecterOccupantEnfantItemRequest;
import com.tarnof.enjoyrestapi.payload.request.AffecterOccupantEquipeItemRequest;
import com.tarnof.enjoyrestapi.payload.request.AffecterOccupantsEnfantsRequest;
import com.tarnof.enjoyrestapi.payload.request.AffecterOccupantsEquipeRequest;
import com.tarnof.enjoyrestapi.payload.request.AjouterReferentRequest;
import com.tarnof.enjoyrestapi.payload.request.SaveChambreRequest;
import com.tarnof.enjoyrestapi.payload.response.ChambreDto;
import com.tarnof.enjoyrestapi.payload.response.ChambreOccupantDto;
import com.tarnof.enjoyrestapi.payload.response.GroupeResumeDto;
import com.tarnof.enjoyrestapi.repositories.ChambreOccupantRepository;
import com.tarnof.enjoyrestapi.repositories.ChambreRepository;
import com.tarnof.enjoyrestapi.repositories.EnfantRepository;
import com.tarnof.enjoyrestapi.repositories.GroupeRepository;
import com.tarnof.enjoyrestapi.repositories.SejourEnfantRepository;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.services.ChambreService;
import com.tarnof.enjoyrestapi.services.HistoriqueModificationService;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
import com.tarnof.enjoyrestapi.utils.ChambreGenreRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class ChambreServiceImpl implements ChambreService {

    private final ChambreRepository chambreRepository;
    private final ChambreOccupantRepository chambreOccupantRepository;
    private final SejourVerificationService sejourVerificationService;
    private final UtilisateurRepository utilisateurRepository;
    private final EnfantRepository enfantRepository;
    private final SejourEnfantRepository sejourEnfantRepository;
    private final GroupeRepository groupeRepository;
    private final HistoriqueModificationService historiqueModificationService;

    public ChambreServiceImpl(
            ChambreRepository chambreRepository,
            ChambreOccupantRepository chambreOccupantRepository,
            SejourVerificationService sejourVerificationService,
            UtilisateurRepository utilisateurRepository,
            EnfantRepository enfantRepository,
            SejourEnfantRepository sejourEnfantRepository,
            GroupeRepository groupeRepository,
            HistoriqueModificationService historiqueModificationService) {
        this.chambreRepository = chambreRepository;
        this.chambreOccupantRepository = chambreOccupantRepository;
        this.sejourVerificationService = sejourVerificationService;
        this.utilisateurRepository = utilisateurRepository;
        this.enfantRepository = enfantRepository;
        this.sejourEnfantRepository = sejourEnfantRepository;
        this.groupeRepository = groupeRepository;
        this.historiqueModificationService = historiqueModificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChambreDto> listerChambresDuSejour(int sejourId, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        List<Chambre> chambres = chambreRepository.findBySejourIdOrderAffichageWithOccupants(sejourId);
        initialiserReferents(chambres);
        return chambres.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ChambreDto getChambre(int sejourId, int chambreId, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        Chambre chambre = getChambreEtVerifierSejourAvecOccupants(sejourId, chambreId);
        initialiserReferents(List.of(chambre));
        return mapToDto(chambre);
    }

    @Override
    @Transactional
    public ChambreDto creerChambre(int sejourId, SaveChambreRequest request, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        Sejour sejour = sejourVerificationService.verifierSejourExiste(sejourId);
        String identifiant = normaliserIdentifiant(request.identifiant());
        verifierIdentifiantChambreUniquePourSejour(sejourId, identifiant, null);
        Chambre chambre = new Chambre();
        appliquerRequete(chambre, request, identifiant);
        chambre.setSejour(sejour);
        appliquerGroupe(chambre, sejourId, request);
        Chambre sauve = chambreRepository.save(chambre);
        historiqueModificationService.enregistrerChambre(
                utilisateurTokenId,
                HistoriqueModificationAction.CREATION,
                sauve.getId(),
                null,
                libelleChambrePourHistorique(sauve));
        return mapToDto(sauve);
    }

    @Override
    @Transactional
    public ChambreDto modifierChambre(
            int sejourId, int chambreId, SaveChambreRequest request, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        Chambre chambre = getChambreEtVerifierSejour(sejourId, chambreId);
        preparerChambrePourSnapshotHistorique(chambre);
        String signatureAvant = signatureTechniqueChambre(chambre);
        String ancienneValeur = libelleChambrePourHistorique(chambre);
        TypeChambre ancienType = chambre.getTypeChambre();
        String identifiant = normaliserIdentifiant(request.identifiant());
        verifierIdentifiantChambreUniquePourSejour(sejourId, identifiant, chambreId);
        appliquerRequete(chambre, request, identifiant);
        appliquerGroupe(chambre, sejourId, request);
        if (ancienType != request.typeChambre()) {
            chambre.getOccupants().clear();
        }
        verifierOccupantsCompatiblesAvecGroupe(chambre);
        verifierOccupantsCompatiblesAvecGenreAutorise(chambre);
        verifierCapaciteCoherenteAvecOccupants(chambre);
        Chambre sauve = chambreRepository.save(chambre);
        preparerChambrePourSnapshotHistorique(sauve);
        if (!signatureAvant.equals(signatureTechniqueChambre(sauve))) {
            historiqueModificationService.enregistrerChambre(
                    utilisateurTokenId,
                    HistoriqueModificationAction.MODIFICATION,
                    sauve.getId(),
                    ancienneValeur,
                    libelleChambrePourHistorique(sauve));
        }
        return mapToDto(sauve);
    }

    @Override
    @Transactional
    public void supprimerChambre(int sejourId, int chambreId, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        Chambre chambre = getChambreEtVerifierSejourAvecOccupants(sejourId, chambreId);
        preparerChambrePourSnapshotHistorique(chambre);
        int idSupprime = chambre.getId();
        String ancienneValeur = libelleChambrePourHistorique(chambre);
        historiqueModificationService.enregistrerChambre(
                utilisateurTokenId,
                HistoriqueModificationAction.SUPPRESSION,
                idSupprime,
                ancienneValeur,
                null);
        chambreRepository.delete(chambre);
    }

    @Override
    @Transactional
    public void ajouterReferent(
            int sejourId, int chambreId, AjouterReferentRequest request, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        Chambre chambre = getChambreEtVerifierSejour(sejourId, chambreId);
        verifierChambreAccepteReferents(chambre);
        preparerChambrePourSnapshotHistorique(chambre);
        String signatureAvant = signatureTechniqueChambre(chambre);
        String ancienneValeur = libelleChambrePourHistorique(chambre);
        Utilisateur referent = utilisateurRepository.findByTokenId(request.referentTokenId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Référent non trouvé avec l'ID: " + request.referentTokenId()));
        if (chambre.getReferents().stream().anyMatch(r -> r.getId() == referent.getId())) {
            throw new ResourceAlreadyExistsException("Ce référent fait déjà partie de la chambre");
        }
        chambre.getReferents().add(referent);
        chambreRepository.save(chambre);
        if (!signatureAvant.equals(signatureTechniqueChambre(chambre))) {
            historiqueModificationService.enregistrerChambre(
                    utilisateurTokenId,
                    HistoriqueModificationAction.MODIFICATION,
                    chambre.getId(),
                    ancienneValeur,
                    libelleChambrePourHistorique(chambre));
        }
    }

    @Override
    @Transactional
    public void retirerReferent(
            int sejourId, int chambreId, String referentTokenId, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        Chambre chambre = getChambreEtVerifierSejour(sejourId, chambreId);
        verifierChambreAccepteReferents(chambre);
        preparerChambrePourSnapshotHistorique(chambre);
        String signatureAvant = signatureTechniqueChambre(chambre);
        String ancienneValeur = libelleChambrePourHistorique(chambre);
        Utilisateur referent = utilisateurRepository.findByTokenId(referentTokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Référent non trouvé avec l'ID: " + referentTokenId));
        boolean removed = chambre.getReferents().removeIf(r -> r.getId() == referent.getId());
        if (!removed) {
            throw new ResourceNotFoundException("Ce référent ne fait pas partie de la chambre");
        }
        chambreRepository.save(chambre);
        if (!signatureAvant.equals(signatureTechniqueChambre(chambre))) {
            historiqueModificationService.enregistrerChambre(
                    utilisateurTokenId,
                    HistoriqueModificationAction.MODIFICATION,
                    chambre.getId(),
                    ancienneValeur,
                    libelleChambrePourHistorique(chambre));
        }
    }

    @Override
    @Transactional
    public ChambreDto affecterEnfant(
            int sejourId,
            int chambreId,
            int enfantId,
            AffecterOccupantChambreRequest request,
            String utilisateurTokenId) {
        return affecterEnfants(
                sejourId,
                chambreId,
                new AffecterOccupantsEnfantsRequest(
                        List.of(new AffecterOccupantEnfantItemRequest(enfantId, numeroLitDepuisRequete(request)))),
                utilisateurTokenId);
    }

    @Override
    @Transactional
    public ChambreDto affecterEnfants(
            int sejourId, int chambreId, AffecterOccupantsEnfantsRequest request, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        Chambre chambre = getChambreEtVerifierSejour(sejourId, chambreId);
        verifierChambreAccepteEnfants(chambre);
        preparerChambrePourSnapshotHistorique(chambre);
        String signatureAvant = signatureTechniqueChambre(chambre);
        String ancienneValeur = libelleChambrePourHistorique(chambre);
        List<AffecterOccupantEnfantItemRequest> items = request.occupants();
        verifierDoublonsEnfantsDansRequete(items);
        verifierCapaciteBatchEnfants(chambre, sejourId, items);
        verifierNumerosLitBatchEnfants(chambre, items);
        for (AffecterOccupantEnfantItemRequest item : items) {
            verifierEnfantInscritAuSejour(sejourId, item.enfantId());
            Enfant enfant = enfantRepository
                    .findById(item.enfantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Enfant non trouvé avec l'ID: " + item.enfantId()));
            if (!ChambreGenreRules.occupantCompatibleAvecChambre(enfant.getGenre(), chambre.getGenreAutorise())) {
                throw new IllegalArgumentException(
                        "Le genre de l'enfant "
                                + enfant.getPrenom()
                                + " "
                                + enfant.getNom()
                                + " n'est pas compatible avec le genre autorisé pour cette chambre");
            }
            verifierEnfantAppartientAuGroupeDeLaChambre(chambre, enfant);
            appliquerAffectationEnfant(chambre, sejourId, enfant, item.numeroLit());
        }
        chambreRepository.save(chambre);
        Chambre reloaded = rechargerChambreAvecOccupants(sejourId, chambreId);
        if (!signatureAvant.equals(signatureTechniqueChambre(reloaded))) {
            historiqueModificationService.enregistrerChambre(
                    utilisateurTokenId,
                    HistoriqueModificationAction.MODIFICATION,
                    reloaded.getId(),
                    ancienneValeur,
                    libelleChambrePourHistorique(reloaded));
        }
        return mapToDto(reloaded);
    }

    @Override
    @Transactional
    public void retirerEnfant(int sejourId, int chambreId, int enfantId, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        Chambre chambre = getChambreEtVerifierSejour(sejourId, chambreId);
        verifierChambreAccepteEnfants(chambre);
        preparerChambrePourSnapshotHistorique(chambre);
        String signatureAvant = signatureTechniqueChambre(chambre);
        String ancienneValeur = libelleChambrePourHistorique(chambre);
        ChambreOccupant occupant = trouverOccupantEnfantDansChambre(chambre, enfantId);
        chambre.getOccupants().remove(occupant);
        chambreOccupantRepository.delete(occupant);
        chambreRepository.save(chambre);
        if (!signatureAvant.equals(signatureTechniqueChambre(chambre))) {
            historiqueModificationService.enregistrerChambre(
                    utilisateurTokenId,
                    HistoriqueModificationAction.MODIFICATION,
                    chambre.getId(),
                    ancienneValeur,
                    libelleChambrePourHistorique(chambre));
        }
    }

    @Override
    @Transactional
    public ChambreDto affecterMembreEquipe(
            int sejourId,
            int chambreId,
            String membreTokenId,
            AffecterOccupantChambreRequest request,
            String utilisateurTokenId) {
        return affecterMembresEquipe(
                sejourId,
                chambreId,
                new AffecterOccupantsEquipeRequest(
                        List.of(new AffecterOccupantEquipeItemRequest(
                                normaliserMembreTokenId(membreTokenId), numeroLitDepuisRequete(request)))),
                utilisateurTokenId);
    }

    @Override
    @Transactional
    public ChambreDto affecterMembresEquipe(
            int sejourId, int chambreId, AffecterOccupantsEquipeRequest request, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        Chambre chambre = getChambreEtVerifierSejour(sejourId, chambreId);
        verifierChambreAccepteEquipe(chambre);
        preparerChambrePourSnapshotHistorique(chambre);
        String signatureAvant = signatureTechniqueChambre(chambre);
        String ancienneValeur = libelleChambrePourHistorique(chambre);
        List<AffecterOccupantEquipeItemRequest> items = request.occupants().stream()
                .map(item -> new AffecterOccupantEquipeItemRequest(
                        normaliserMembreTokenId(item.membreTokenId()), item.numeroLit()))
                .toList();
        verifierDoublonsMembresDansRequete(items);
        verifierCapaciteBatchMembres(chambre, sejourId, items);
        verifierNumerosLitBatchMembres(chambre, items);
        for (AffecterOccupantEquipeItemRequest item : items) {
            Utilisateur membre = sejourVerificationService.exigerUtilisateurDirecteurOuMembreEquipeDuSejour(
                    sejourId, item.membreTokenId());
            if (!ChambreGenreRules.occupantCompatibleAvecChambre(membre.getGenre(), chambre.getGenreAutorise())) {
                throw new IllegalArgumentException(
                        "Le genre du membre "
                                + membre.getPrenom()
                                + " "
                                + membre.getNom()
                                + " n'est pas compatible avec le genre autorisé pour cette chambre");
            }
            appliquerAffectationMembreEquipe(chambre, sejourId, membre, item.numeroLit());
        }
        chambreRepository.save(chambre);
        Chambre reloaded = rechargerChambreAvecOccupants(sejourId, chambreId);
        if (!signatureAvant.equals(signatureTechniqueChambre(reloaded))) {
            historiqueModificationService.enregistrerChambre(
                    utilisateurTokenId,
                    HistoriqueModificationAction.MODIFICATION,
                    reloaded.getId(),
                    ancienneValeur,
                    libelleChambrePourHistorique(reloaded));
        }
        return mapToDto(reloaded);
    }

    @Override
    @Transactional
    public void retirerMembreEquipe(int sejourId, int chambreId, String membreTokenId, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        Chambre chambre = getChambreEtVerifierSejour(sejourId, chambreId);
        verifierChambreAccepteEquipe(chambre);
        preparerChambrePourSnapshotHistorique(chambre);
        String signatureAvant = signatureTechniqueChambre(chambre);
        String ancienneValeur = libelleChambrePourHistorique(chambre);
        Utilisateur membre = utilisateurRepository.findByTokenId(membreTokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Membre non trouvé avec l'ID: " + membreTokenId));
        ChambreOccupant occupant = chambre.getOccupants().stream()
                .filter(o -> o.getUtilisateur() != null && o.getUtilisateur().getId() == membre.getId())
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Ce membre n'est pas affecté à cette chambre"));
        chambre.getOccupants().remove(occupant);
        chambreOccupantRepository.delete(occupant);
        chambreRepository.save(chambre);
        if (!signatureAvant.equals(signatureTechniqueChambre(chambre))) {
            historiqueModificationService.enregistrerChambre(
                    utilisateurTokenId,
                    HistoriqueModificationAction.MODIFICATION,
                    chambre.getId(),
                    ancienneValeur,
                    libelleChambrePourHistorique(chambre));
        }
    }

    private Chambre getChambreEtVerifierSejour(int sejourId, int chambreId) {
        Chambre chambre = chambreRepository.findById(chambreId)
                .orElseThrow(() -> new ResourceNotFoundException("Chambre non trouvée avec l'ID: " + chambreId));
        if (chambre.getSejour().getId() != sejourId) {
            throw new ResourceNotFoundException("La chambre n'appartient pas à ce séjour");
        }
        return chambre;
    }

    private Chambre getChambreEtVerifierSejourAvecOccupants(int sejourId, int chambreId) {
        return chambreRepository
                .findByIdAndSejourIdWithOccupants(chambreId, sejourId)
                .orElseThrow(() -> new ResourceNotFoundException("Chambre non trouvée avec l'ID: " + chambreId));
    }

    private Chambre rechargerChambreAvecOccupants(int sejourId, int chambreId) {
        Chambre chambre = getChambreEtVerifierSejourAvecOccupants(sejourId, chambreId);
        initialiserReferents(List.of(chambre));
        return chambre;
    }

    /** Charge les référents en requête séparée (évite MultipleBagFetchException avec occupants). */
    private void initialiserReferents(List<Chambre> chambres) {
        List<Integer> idsChambresEnfant = chambres.stream()
                .filter(c -> c.getTypeChambre() == TypeChambre.ENFANT && c.getId() != null)
                .map(Chambre::getId)
                .distinct()
                .toList();
        if (!idsChambresEnfant.isEmpty()) {
            chambreRepository.fetchReferentsByChambreIds(idsChambresEnfant);
        }
    }

    private static ChambreOccupant trouverOccupantEnfantDansChambre(Chambre chambre, int enfantId) {
        return chambre.getOccupants().stream()
                .filter(o -> o.getEnfant() != null && o.getEnfant().getId() == enfantId)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cet enfant n'est pas affecté à cette chambre"));
    }

    private void verifierEnfantInscritAuSejour(int sejourId, int enfantId) {
        if (!sejourEnfantRepository.existsById(new SejourEnfantId(sejourId, enfantId))) {
            throw new IllegalArgumentException("L'enfant doit d'abord être inscrit au séjour");
        }
    }

    private void verifierEnfantAppartientAuGroupeDeLaChambre(Chambre chambre, Enfant enfant) {
        Groupe groupe = chambre.getGroupe();
        if (groupe == null) {
            return;
        }
        if (!groupeRepository.existsEnfantInGroupe(groupe.getId(), enfant.getId())) {
            throw new IllegalArgumentException(
                    "Seuls les enfants du groupe « "
                            + groupe.getNom()
                            + " » peuvent dormir dans cette chambre");
        }
    }

    private void verifierOccupantsCompatiblesAvecGroupe(Chambre chambre) {
        Groupe groupe = chambre.getGroupe();
        if (groupe == null || chambre.getOccupants() == null) {
            return;
        }
        for (ChambreOccupant occupant : chambre.getOccupants()) {
            if (occupant.getEnfant() == null) {
                continue;
            }
            Enfant enfant = occupant.getEnfant();
            if (!groupeRepository.existsEnfantInGroupe(groupe.getId(), enfant.getId())) {
                throw new IllegalArgumentException(
                        "Impossible d'associer ce groupe : l'enfant "
                                + enfant.getPrenom()
                                + " "
                                + enfant.getNom()
                                + " n'en fait pas partie");
            }
        }
    }

    private static void verifierOccupantsCompatiblesAvecGenreAutorise(Chambre chambre) {
        if (chambre.getOccupants() == null) {
            return;
        }
        for (ChambreOccupant occupant : chambre.getOccupants()) {
            if (occupant.getEnfant() != null) {
                Enfant enfant = occupant.getEnfant();
                if (!ChambreGenreRules.occupantCompatibleAvecChambre(enfant.getGenre(), chambre.getGenreAutorise())) {
                    throw new IllegalArgumentException(
                            "Impossible de modifier le genre autorisé : l'enfant "
                                    + enfant.getPrenom()
                                    + " "
                                    + enfant.getNom()
                                    + " n'est pas compatible avec ce genre");
                }
            } else if (occupant.getUtilisateur() != null) {
                Utilisateur membre = occupant.getUtilisateur();
                if (!ChambreGenreRules.occupantCompatibleAvecChambre(membre.getGenre(), chambre.getGenreAutorise())) {
                    throw new IllegalArgumentException(
                            "Impossible de modifier le genre autorisé : le membre "
                                    + membre.getPrenom()
                                    + " "
                                    + membre.getNom()
                                    + " n'est pas compatible avec ce genre");
                }
            }
        }
    }

    private void appliquerGroupe(Chambre chambre, int sejourId, SaveChambreRequest request) {
        if (request.typeChambre() == TypeChambre.EQUIPE) {
            if (request.groupeId() != null) {
                throw new IllegalArgumentException("Les chambres équipe n'acceptent pas de groupe");
            }
            chambre.setGroupe(null);
            return;
        }
        if (request.groupeId() == null) {
            chambre.setGroupe(null);
            return;
        }
        Groupe groupe = groupeRepository
                .findByIdAndSejourId(request.groupeId(), sejourId)
                .orElseThrow(() -> new ResourceNotFoundException("Groupe non trouvé avec l'ID: " + request.groupeId()));
        chambre.setGroupe(groupe);
    }

    private static void verifierChambreAccepteEnfants(Chambre chambre) {
        if (chambre.getTypeChambre() != TypeChambre.ENFANT) {
            throw new IllegalArgumentException("Seules les chambres enfants acceptent l'affectation d'enfants");
        }
    }

    private static void verifierChambreAccepteEquipe(Chambre chambre) {
        if (chambre.getTypeChambre() != TypeChambre.EQUIPE) {
            throw new IllegalArgumentException("Seules les chambres équipe acceptent l'affectation de membres d'équipe");
        }
    }

    private static void verifierChambreAccepteReferents(Chambre chambre) {
        if (chambre.getTypeChambre() == TypeChambre.EQUIPE) {
            throw new IllegalArgumentException("Les chambres équipe n'acceptent pas de référents.");
        }
    }

    private void appliquerAffectationEnfant(Chambre chambre, int sejourId, Enfant enfant, Integer numeroLit) {
        Optional<ChambreOccupant> existantOpt =
                chambreOccupantRepository.findByEnfantIdAndSejourId(enfant.getId(), sejourId);
        if (existantOpt.isPresent()) {
            ChambreOccupant existant = existantOpt.get();
            if (existant.getChambre().getId().equals(chambre.getId())) {
                existant.setNumeroLit(numeroLit);
                return;
            }
            chambreOccupantRepository.delete(existant);
            existant.getChambre().getOccupants().remove(existant);
        }
        ChambreOccupant occupant = new ChambreOccupant();
        occupant.setChambre(chambre);
        occupant.setEnfant(enfant);
        occupant.setNumeroLit(numeroLit);
        chambre.getOccupants().add(occupant);
    }

    private void appliquerAffectationMembreEquipe(
            Chambre chambre, int sejourId, Utilisateur membre, Integer numeroLit) {
        Optional<ChambreOccupant> existantOpt =
                chambreOccupantRepository.findByUtilisateurIdAndSejourId(membre.getId(), sejourId);
        if (existantOpt.isPresent()) {
            ChambreOccupant existant = existantOpt.get();
            if (existant.getChambre().getId().equals(chambre.getId())) {
                existant.setNumeroLit(numeroLit);
                return;
            }
            chambreOccupantRepository.delete(existant);
            existant.getChambre().getOccupants().remove(existant);
        }
        ChambreOccupant occupant = new ChambreOccupant();
        occupant.setChambre(chambre);
        occupant.setUtilisateur(membre);
        occupant.setNumeroLit(numeroLit);
        chambre.getOccupants().add(occupant);
    }

    private static void verifierDoublonsEnfantsDansRequete(List<AffecterOccupantEnfantItemRequest> items) {
        if (items.stream().map(AffecterOccupantEnfantItemRequest::enfantId).distinct().count() != items.size()) {
            throw new IllegalArgumentException("Un même enfant ne peut pas être sélectionné plusieurs fois");
        }
    }

    private static void verifierDoublonsMembresDansRequete(List<AffecterOccupantEquipeItemRequest> items) {
        if (items.stream().map(AffecterOccupantEquipeItemRequest::membreTokenId).distinct().count() != items.size()) {
            throw new IllegalArgumentException("Un même membre d'équipe ne peut pas être sélectionné plusieurs fois");
        }
    }

    private void verifierCapaciteBatchEnfants(
            Chambre chambre, int sejourId, List<AffecterOccupantEnfantItemRequest> items) {
        int nouveauxOccupants = compterNouveauxEnfantsSurChambre(chambre, sejourId, items);
        if (chambre.getOccupants().size() + nouveauxOccupants > chambre.getCapaciteMax()) {
            throw new IllegalArgumentException(
                    "La capacité maximale de la chambre est insuffisante ("
                            + chambre.getCapaciteMax()
                            + " place(s) pour "
                            + (chambre.getOccupants().size() + nouveauxOccupants)
                            + " occupant(s))");
        }
    }

    private void verifierCapaciteBatchMembres(
            Chambre chambre, int sejourId, List<AffecterOccupantEquipeItemRequest> items) {
        int nouveauxOccupants = compterNouveauxMembresSurChambre(chambre, sejourId, items);
        if (chambre.getOccupants().size() + nouveauxOccupants > chambre.getCapaciteMax()) {
            throw new IllegalArgumentException(
                    "La capacité maximale de la chambre est insuffisante ("
                            + chambre.getCapaciteMax()
                            + " place(s) pour "
                            + (chambre.getOccupants().size() + nouveauxOccupants)
                            + " occupant(s))");
        }
    }

    private int compterNouveauxEnfantsSurChambre(
            Chambre chambre, int sejourId, List<AffecterOccupantEnfantItemRequest> items) {
        int nouveaux = 0;
        for (AffecterOccupantEnfantItemRequest item : items) {
            if (!estEnfantDejaSurChambre(chambre, sejourId, item.enfantId())) {
                nouveaux++;
            }
        }
        return nouveaux;
    }

    private int compterNouveauxMembresSurChambre(
            Chambre chambre, int sejourId, List<AffecterOccupantEquipeItemRequest> items) {
        int nouveaux = 0;
        for (AffecterOccupantEquipeItemRequest item : items) {
            Utilisateur membre = utilisateurRepository
                    .findByTokenId(item.membreTokenId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Membre non trouvé avec l'ID: " + item.membreTokenId()));
            if (!estMembreDejaSurChambre(chambre, sejourId, membre.getId())) {
                nouveaux++;
            }
        }
        return nouveaux;
    }

    private boolean estEnfantDejaSurChambre(Chambre chambre, int sejourId, int enfantId) {
        return chambreOccupantRepository
                .findByEnfantIdAndSejourId(enfantId, sejourId)
                .map(o -> o.getChambre().getId().equals(chambre.getId()))
                .orElse(false);
    }

    private boolean estMembreDejaSurChambre(Chambre chambre, int sejourId, int utilisateurId) {
        return chambreOccupantRepository
                .findByUtilisateurIdAndSejourId(utilisateurId, sejourId)
                .map(o -> o.getChambre().getId().equals(chambre.getId()))
                .orElse(false);
    }

    private void verifierNumerosLitBatchEnfants(Chambre chambre, List<AffecterOccupantEnfantItemRequest> items) {
        Set<Integer> litsOccupes = litsOccupesActuelsExcluantMisesAJour(chambre, items.stream()
                .map(AffecterOccupantEnfantItemRequest::enfantId)
                .collect(Collectors.toSet()));
        for (AffecterOccupantEnfantItemRequest item : items) {
            reserverNumeroLit(chambre, item.numeroLit(), litsOccupes);
        }
    }

    private void verifierNumerosLitBatchMembres(Chambre chambre, List<AffecterOccupantEquipeItemRequest> items) {
        Set<Integer> idsMembresMisesAJour = new HashSet<>();
        for (AffecterOccupantEquipeItemRequest item : items) {
            utilisateurRepository
                    .findByTokenId(item.membreTokenId())
                    .ifPresent(m -> idsMembresMisesAJour.add(m.getId()));
        }
        Set<Integer> litsOccupes = litsOccupesActuelsExcluantMembres(chambre, idsMembresMisesAJour);
        for (AffecterOccupantEquipeItemRequest item : items) {
            reserverNumeroLit(chambre, item.numeroLit(), litsOccupes);
        }
    }

    private Set<Integer> litsOccupesActuelsExcluantMisesAJour(Chambre chambre, Set<Integer> enfantIdsMisesAJour) {
        Set<Integer> litsOccupes = new HashSet<>();
        for (ChambreOccupant occupant : chambre.getOccupants()) {
            if (occupant.getNumeroLit() == null || occupant.getEnfant() == null) {
                continue;
            }
            if (!enfantIdsMisesAJour.contains(occupant.getEnfant().getId())) {
                litsOccupes.add(occupant.getNumeroLit());
            }
        }
        return litsOccupes;
    }

    private Set<Integer> litsOccupesActuelsExcluantMembres(Chambre chambre, Set<Integer> utilisateurIdsMisesAJour) {
        Set<Integer> litsOccupes = new HashSet<>();
        for (ChambreOccupant occupant : chambre.getOccupants()) {
            if (occupant.getNumeroLit() == null || occupant.getUtilisateur() == null) {
                continue;
            }
            if (!utilisateurIdsMisesAJour.contains(occupant.getUtilisateur().getId())) {
                litsOccupes.add(occupant.getNumeroLit());
            }
        }
        return litsOccupes;
    }

    private void reserverNumeroLit(Chambre chambre, Integer numeroLit, Set<Integer> litsOccupes) {
        if (numeroLit == null) {
            return;
        }
        if (numeroLit > chambre.getCapaciteMax()) {
            throw new IllegalArgumentException(
                    "Le numéro de lit ne peut pas dépasser la capacité maximale de la chambre");
        }
        if (!litsOccupes.add(numeroLit)) {
            throw new ResourceAlreadyExistsException("Ce numéro de lit est déjà occupé dans cette chambre");
        }
    }

    private static String normaliserMembreTokenId(String membreTokenId) {
        return membreTokenId == null ? "" : membreTokenId.trim();
    }

    private static void verifierCapaciteCoherenteAvecOccupants(Chambre chambre) {
        int occupants = chambre.getOccupants() == null ? 0 : chambre.getOccupants().size();
        if (occupants > chambre.getCapaciteMax()) {
            throw new IllegalArgumentException(
                    "La capacité maximale (" + chambre.getCapaciteMax()
                            + ") est inférieure au nombre d'occupants déjà affectés (" + occupants + ")");
        }
    }

    private static Integer numeroLitDepuisRequete(AffecterOccupantChambreRequest request) {
        return request == null ? null : request.numeroLit();
    }

    private static void appliquerRequete(Chambre chambre, SaveChambreRequest request, String identifiant) {
        chambre.setTypeChambre(request.typeChambre());
        chambre.setIdentifiant(identifiant);
        chambre.setNom(normaliserTexteOptionnel(request.nom()));
        chambre.setCapaciteMax(request.capaciteMax());
        chambre.setGenreAutorise(request.genreAutorise());
        chambre.setDescription(normaliserTexteOptionnel(request.description()));
        chambre.setBatiment(normaliserTexteOptionnel(request.batiment()));
        chambre.setCouloir(normaliserTexteOptionnel(request.couloir()));
        chambre.setEtage(request.etage());
        if (request.typeChambre() == TypeChambre.EQUIPE) {
            chambre.getReferents().clear();
            chambre.setGroupe(null);
        }
    }

    private static String normaliserIdentifiant(String identifiant) {
        return identifiant == null ? "" : identifiant.trim();
    }

    private static String normaliserTexteOptionnel(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void verifierIdentifiantChambreUniquePourSejour(int sejourId, String identifiant, Integer excludeChambreId) {
        if (identifiant.isEmpty()) {
            throw new IllegalArgumentException("L'identifiant de la chambre ne peut pas être vide");
        }
        boolean doublon = excludeChambreId == null
                ? chambreRepository.existsBySejourIdAndIdentifiantIgnoreCase(sejourId, identifiant)
                : chambreRepository.existsBySejourIdAndIdentifiantIgnoreCaseAndIdNot(
                        sejourId, identifiant, excludeChambreId);
        if (doublon) {
            throw new ResourceAlreadyExistsException(
                    "Une chambre avec cet identifiant existe déjà pour ce séjour");
        }
    }

    private ChambreDto mapToDto(Chambre chambre) {
        List<ChambreDto.ReferentInfos> referents =
                chambre.getTypeChambre() == TypeChambre.ENFANT && chambre.getReferents() != null
                        ? chambre.getReferents().stream()
                                .map(r -> new ChambreDto.ReferentInfos(
                                        r.getTokenId(), r.getNom(), r.getPrenom()))
                                .collect(Collectors.toList())
                        : List.of();
        List<ChambreOccupantDto> occupants =
                chambre.getOccupants() == null
                        ? List.of()
                        : chambre.getOccupants().stream()
                                .sorted(Comparator.comparing(
                                        ChambreOccupant::getNumeroLit,
                                        Comparator.nullsLast(Integer::compareTo)))
                                .map(this::mapOccupantToDto)
                                .collect(Collectors.toList());
        GroupeResumeDto groupe = mapGroupeToDto(chambre);
        return new ChambreDto(
                chambre.getId(),
                chambre.getSejour().getId(),
                chambre.getTypeChambre(),
                chambre.getIdentifiant(),
                chambre.getNom(),
                chambre.getCapaciteMax(),
                chambre.getGenreAutorise(),
                chambre.getDescription(),
                chambre.getBatiment(),
                chambre.getCouloir(),
                chambre.getEtage(),
                groupe,
                referents,
                occupants);
    }

    private static GroupeResumeDto mapGroupeToDto(Chambre chambre) {
        if (chambre.getTypeChambre() != TypeChambre.ENFANT || chambre.getGroupe() == null) {
            return null;
        }
        Groupe groupe = chambre.getGroupe();
        return new GroupeResumeDto(groupe.getId(), groupe.getNom());
    }

    private ChambreOccupantDto mapOccupantToDto(ChambreOccupant occupant) {
        if (occupant.getEnfant() != null) {
            Enfant enfant = occupant.getEnfant();
            return new ChambreOccupantDto(
                    occupant.getId(),
                    TypeChambre.ENFANT,
                    enfant.getId(),
                    null,
                    enfant.getNom(),
                    enfant.getPrenom(),
                    occupant.getNumeroLit());
        }
        Utilisateur membre = occupant.getUtilisateur();
        if (membre == null) {
            throw new IllegalStateException("Occupant de chambre sans enfant ni membre d'équipe");
        }
        return new ChambreOccupantDto(
                occupant.getId(),
                TypeChambre.EQUIPE,
                null,
                membre.getTokenId(),
                membre.getNom(),
                membre.getPrenom(),
                occupant.getNumeroLit());
    }

    private void preparerChambrePourSnapshotHistorique(Chambre chambre) {
        if (chambre.getTypeChambre() == TypeChambre.ENFANT && chambre.getId() != null) {
            initialiserReferents(List.of(chambre));
        }
    }

    private String signatureTechniqueChambre(Chambre c) {
        String groupePart = c.getGroupe() == null ? "-" : String.valueOf(c.getGroupe().getId());
        String referents =
                c.getReferents() == null
                        ? ""
                        : c.getReferents().stream()
                                .map(Utilisateur::getId)
                                .sorted()
                                .map(String::valueOf)
                                .collect(Collectors.joining(","));
        String occupants =
                c.getOccupants() == null
                        ? ""
                        : c.getOccupants().stream()
                                .map(this::signatureOccupantChambre)
                                .sorted()
                                .collect(Collectors.joining(","));
        return c.getTypeChambre()
                + "|"
                + nullToVide(c.getIdentifiant())
                + "|"
                + nullToVide(c.getNom())
                + "|"
                + c.getCapaciteMax()
                + "|"
                + c.getGenreAutorise()
                + "|"
                + nullToVide(c.getDescription())
                + "|"
                + nullToVide(c.getBatiment())
                + "|"
                + nullToVide(c.getCouloir())
                + "|"
                + (c.getEtage() == null ? "" : c.getEtage())
                + "|"
                + groupePart
                + "|"
                + referents
                + "|"
                + occupants;
    }

    private String signatureOccupantChambre(ChambreOccupant o) {
        String lit = o.getNumeroLit() == null ? "" : String.valueOf(o.getNumeroLit());
        if (o.getEnfant() != null) {
            return "E" + o.getEnfant().getId() + ":" + lit;
        }
        if (o.getUtilisateur() != null) {
            return "U" + o.getUtilisateur().getId() + ":" + lit;
        }
        return "?";
    }

    private String libelleChambrePourHistorique(Chambre c) {
        String groupePart = c.getGroupe() == null ? "-" : nullToDash(c.getGroupe().getNom());
        String referents =
                c.getReferents() == null || c.getReferents().isEmpty()
                        ? "-"
                        : c.getReferents().stream()
                                .map(ChambreServiceImpl::libelleUtilisateurPourHistorique)
                                .sorted()
                                .collect(Collectors.joining(", "));
        String occupants =
                c.getOccupants() == null || c.getOccupants().isEmpty()
                        ? "-"
                        : c.getOccupants().stream()
                                .sorted(Comparator.comparing(
                                        ChambreOccupant::getNumeroLit,
                                        Comparator.nullsLast(Integer::compareTo)))
                                .map(this::libelleOccupantPourHistorique)
                                .collect(Collectors.joining(", "));
        return "Type: "
                + c.getTypeChambre()
                + " | Identifiant: "
                + nullToDash(c.getIdentifiant())
                + " | Nom: "
                + nullToDash(c.getNom())
                + " | Capacité: "
                + c.getCapaciteMax()
                + " | Genre: "
                + c.getGenreAutorise()
                + " | Description: "
                + nullToDash(c.getDescription())
                + " | Bâtiment: "
                + nullToDash(c.getBatiment())
                + " | Couloir: "
                + nullToDash(c.getCouloir())
                + " | Étage: "
                + (c.getEtage() == null ? "-" : c.getEtage())
                + " | Groupe: "
                + groupePart
                + " | Référents: "
                + referents
                + " | Occupants: "
                + occupants;
    }

    private String libelleOccupantPourHistorique(ChambreOccupant o) {
        String nom =
                o.getEnfant() != null
                        ? libelleEnfantPourHistorique(o.getEnfant())
                        : libelleUtilisateurPourHistorique(o.getUtilisateur());
        if (o.getNumeroLit() == null) {
            return nom;
        }
        return "lit " + o.getNumeroLit() + ": " + nom;
    }

    private static String libelleEnfantPourHistorique(Enfant e) {
        String p = e.getPrenom() != null ? e.getPrenom().trim() : "";
        String n = e.getNom() != null ? e.getNom().trim() : "";
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

    private static String nullToDash(String s) {
        if (s == null) {
            return "-";
        }
        String t = s.trim();
        return t.isEmpty() ? "-" : t;
    }

    private static String nullToVide(String s) {
        return s == null ? "" : s.trim();
    }
}
