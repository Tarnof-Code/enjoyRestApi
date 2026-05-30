package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.ActivitePrestataire;
import com.tarnof.enjoyrestapi.entities.ActivitePrestataireNonParticipation;
import com.tarnof.enjoyrestapi.entities.Groupe;
import com.tarnof.enjoyrestapi.entities.Moment;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.SaveActivitePrestataireRequest;
import com.tarnof.enjoyrestapi.payload.response.ActivitePrestataireDto;
import com.tarnof.enjoyrestapi.payload.response.MomentDto;
import com.tarnof.enjoyrestapi.payload.response.NonParticipationPrestataireDto;
import com.tarnof.enjoyrestapi.repositories.ActivitePrestataireRepository;
import com.tarnof.enjoyrestapi.repositories.GroupeRepository;
import com.tarnof.enjoyrestapi.repositories.MomentRepository;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.services.ActivitePrestataireService;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
import com.tarnof.enjoyrestapi.utils.DateFormatHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class ActivitePrestataireServiceImpl implements ActivitePrestataireService {

    private final ActivitePrestataireRepository activitePrestataireRepository;
    private final SejourVerificationService sejourVerificationService;
    private final MomentRepository momentRepository;
    private final GroupeRepository groupeRepository;
    private final UtilisateurRepository utilisateurRepository;

    public ActivitePrestataireServiceImpl(
            ActivitePrestataireRepository activitePrestataireRepository,
            SejourVerificationService sejourVerificationService,
            MomentRepository momentRepository,
            GroupeRepository groupeRepository,
            UtilisateurRepository utilisateurRepository) {
        this.activitePrestataireRepository = activitePrestataireRepository;
        this.sejourVerificationService = sejourVerificationService;
        this.momentRepository = momentRepository;
        this.groupeRepository = groupeRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivitePrestataireDto> listerActivitesPrestatairesDuSejour(int sejourId, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        return activitePrestataireRepository.findBySejour_IdOrderByDateAscIdAsc(sejourId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ActivitePrestataireDto getActivitePrestataire(
            int sejourId, int activitePrestataireId, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        ActivitePrestataire activite = findByIdAndSejourOrThrow(activitePrestataireId, sejourId);
        return mapToDto(activite);
    }

    @Override
    @Transactional
    public ActivitePrestataireDto creerActivitePrestataire(int sejourId, SaveActivitePrestataireRequest request) {
        Sejour sejour = sejourVerificationService.verifierSejourExiste(sejourId);
        List<Moment> moments = resoudreMomentsDuSejour(sejourId, request.momentIds());
        verifierDateDansSejour(sejour, request.date());
        List<Groupe> groupes = resoudreGroupesDuSejour(sejourId, request.groupeIds());
        verifierAbsenceConflitSortieMemeDateMomentGroupe(sejourId, request.date(), moments, groupes, null);

        ActivitePrestataire activite = new ActivitePrestataire();
        activite.setSejour(sejour);
        appliquerChamps(activite, request, moments, groupes);
        appliquerNonParticipations(activite, sejourId, request.nonParticipations(), moments, groupes, true);
        return mapToDto(activitePrestataireRepository.save(activite));
    }

    @Override
    @Transactional
    public ActivitePrestataireDto modifierActivitePrestataire(
            int sejourId, int activitePrestataireId, SaveActivitePrestataireRequest request) {
        ActivitePrestataire activite = findByIdAndSejourOrThrow(activitePrestataireId, sejourId);
        List<Moment> moments = resoudreMomentsDuSejour(sejourId, request.momentIds());
        verifierDateDansSejour(activite.getSejour(), request.date());
        List<Groupe> groupes = resoudreGroupesDuSejour(sejourId, request.groupeIds());
        verifierAbsenceConflitSortieMemeDateMomentGroupe(
                sejourId, request.date(), moments, groupes, activitePrestataireId);
        appliquerChamps(activite, request, moments, groupes);
        appliquerNonParticipations(
                activite, sejourId, request.nonParticipations(), moments, groupes, request.nonParticipations() != null);
        return mapToDto(activitePrestataireRepository.save(activite));
    }

    @Override
    @Transactional
    public void supprimerActivitePrestataire(int sejourId, int activitePrestataireId) {
        ActivitePrestataire activite = findByIdAndSejourOrThrow(activitePrestataireId, sejourId);
        activitePrestataireRepository.delete(activite);
    }

    private ActivitePrestataire findByIdAndSejourOrThrow(int activitePrestataireId, int sejourId) {
        return activitePrestataireRepository
                .findByIdAndSejour_Id(activitePrestataireId, sejourId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Activité prestataire non trouvée pour ce séjour (id: " + activitePrestataireId + ")"));
    }

    private void appliquerChamps(
            ActivitePrestataire activite,
            SaveActivitePrestataireRequest request,
            List<Moment> moments,
            List<Groupe> groupes) {
        activite.setNom(request.nom().trim());
        activite.setDate(request.date());
        activite.getMoments().clear();
        activite.getMoments().addAll(moments);
        activite.setHeureDepart(request.heureDepart());
        activite.setHeureRetour(request.heureRetour());
        activite.setInformations(normaliserTexteOptionnel(request.informations()));
        activite.setTelephone(normaliserTexteOptionnel(request.telephone()));
        activite.getGroupes().clear();
        activite.getGroupes().addAll(groupes);
    }

    private void appliquerNonParticipations(
            ActivitePrestataire activite,
            int sejourId,
            List<NonParticipationPrestataireDto> nonParticipationsRequest,
            List<Moment> moments,
            List<Groupe> groupes,
            boolean remplacerListeComplete) {
        if (groupes.isEmpty()) {
            if (nonParticipationsRequest != null && !nonParticipationsRequest.isEmpty()) {
                throw new IllegalArgumentException(
                        "Impossible de déclarer des non-participations sans groupe associé à la sortie.");
            }
            activite.getNonParticipations().clear();
            return;
        }

        Set<String> animateursConcernes = calculerAnimateursConcernes(groupes);
        Map<Integer, Moment> momentsParId =
                moments.stream().collect(Collectors.toMap(Moment::getId, m -> m, (a, b) -> a, LinkedHashMap::new));
        Set<Integer> momentIdsValides = momentsParId.keySet();

        if (remplacerListeComplete) {
            remplacerNonParticipations(
                    activite, sejourId, nonParticipationsRequest, animateursConcernes, momentsParId, momentIdsValides);
            return;
        }

        elaguerNonParticipationsInvalides(activite, animateursConcernes, momentIdsValides);
    }

    /**
     * Remplace la liste sans {@code clear()} + réinsertion : évite la violation de contrainte unique
     * ({@code uk_ap_non_participation}) lorsque Hibernate insère avant de supprimer les orphelins.
     */
    private void remplacerNonParticipations(
            ActivitePrestataire activite,
            int sejourId,
            List<NonParticipationPrestataireDto> nonParticipationsRequest,
            Set<String> animateursConcernes,
            Map<Integer, Moment> momentsParId,
            Set<Integer> momentIdsValides) {
        List<NonParticipationPrestataireDto> demandees =
                nonParticipationsRequest == null || nonParticipationsRequest.isEmpty()
                        ? List.of()
                        : dedoublonnerNonParticipations(nonParticipationsRequest);

        Set<String> clesDemandees = demandees.stream()
                .map(dto -> cleNonParticipation(dto.tokenId().trim(), dto.momentId()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        activite.getNonParticipations().removeIf(np -> {
            String token = np.getUtilisateur() != null ? np.getUtilisateur().getTokenId() : null;
            Integer momentId = np.getMoment() != null ? np.getMoment().getId() : null;
            if (token == null || momentId == null) {
                return true;
            }
            return !clesDemandees.contains(cleNonParticipation(token, momentId));
        });

        Set<String> clesExistantes = activite.getNonParticipations().stream()
                .map(np -> cleNonParticipation(np.getUtilisateur().getTokenId(), np.getMoment().getId()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (NonParticipationPrestataireDto dto : demandees) {
            String cle = cleNonParticipation(dto.tokenId().trim(), dto.momentId());
            if (!clesExistantes.contains(cle)) {
                activite.getNonParticipations().add(creerNonParticipation(
                        activite, sejourId, dto, animateursConcernes, momentsParId, momentIdsValides));
            }
        }
    }

    private static String cleNonParticipation(String tokenId, int momentId) {
        return tokenId + "|" + momentId;
    }

    private void elaguerNonParticipationsInvalides(
            ActivitePrestataire activite, Set<String> animateursConcernes, Set<Integer> momentIdsValides) {
        activite.getNonParticipations().removeIf(np -> {
            String token = np.getUtilisateur() != null ? np.getUtilisateur().getTokenId() : null;
            Integer momentId = np.getMoment() != null ? np.getMoment().getId() : null;
            return token == null
                    || !animateursConcernes.contains(token)
                    || momentId == null
                    || !momentIdsValides.contains(momentId);
        });
    }

    private ActivitePrestataireNonParticipation creerNonParticipation(
            ActivitePrestataire activite,
            int sejourId,
            NonParticipationPrestataireDto dto,
            Set<String> animateursConcernes,
            Map<Integer, Moment> momentsParId,
            Set<Integer> momentIdsValides) {
        if (dto.tokenId() == null || dto.tokenId().isBlank()) {
            throw new IllegalArgumentException("Le tokenId est obligatoire pour une non-participation.");
        }
        String tokenId = dto.tokenId().trim();
        if (!animateursConcernes.contains(tokenId)) {
            throw new IllegalArgumentException(
                    "L'animateur « " + tokenId + " » n'est pas référent d'un groupe concerné par cette sortie.");
        }
        if (!momentIdsValides.contains(dto.momentId())) {
            throw new IllegalArgumentException(
                    "Le moment « " + dto.momentId() + " » n'est pas rattaché à cette sortie.");
        }
        Utilisateur utilisateur = utilisateurRepository
                .findByTokenId(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec le token ID: " + tokenId));
        Moment moment = momentsParId.get(dto.momentId());
        if (moment == null) {
            throw new ResourceNotFoundException("Moment non trouvé pour ce séjour (id: " + dto.momentId() + ")");
        }
        if (moment.getSejour() == null || moment.getSejour().getId() != sejourId) {
            throw new IllegalArgumentException("Le moment n'appartient pas à ce séjour");
        }

        ActivitePrestataireNonParticipation nonParticipation = new ActivitePrestataireNonParticipation();
        nonParticipation.setActivitePrestataire(activite);
        nonParticipation.setUtilisateur(utilisateur);
        nonParticipation.setMoment(moment);
        return nonParticipation;
    }

    private static List<NonParticipationPrestataireDto> dedoublonnerNonParticipations(
            List<NonParticipationPrestataireDto> nonParticipations) {
        Map<String, NonParticipationPrestataireDto> uniques = new LinkedHashMap<>();
        for (NonParticipationPrestataireDto dto : nonParticipations) {
            if (dto == null) {
                continue;
            }
            String token = dto.tokenId() != null ? dto.tokenId().trim() : "";
            uniques.putIfAbsent(token + "|" + dto.momentId(), new NonParticipationPrestataireDto(token, dto.momentId()));
        }
        return new ArrayList<>(uniques.values());
    }

    private static Set<String> calculerAnimateursConcernes(List<Groupe> groupes) {
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        for (Groupe groupe : groupes) {
            if (groupe.getReferents() == null) {
                continue;
            }
            for (Utilisateur referent : groupe.getReferents()) {
                if (referent.getTokenId() != null && !referent.getTokenId().isBlank()) {
                    tokens.add(referent.getTokenId().trim());
                }
            }
        }
        return tokens;
    }

    private static String normaliserTexteOptionnel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Moment resoudreMomentDuSejour(int sejourId, int momentId) {
        return momentRepository.findByIdAndSejourId(momentId, sejourId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Moment non trouvé pour ce séjour (id: " + momentId + ")"));
    }

    private List<Moment> resoudreMomentsDuSejour(int sejourId, List<Integer> momentIds) {
        if (momentRepository.countBySejourId(sejourId) == 0) {
            throw new IllegalArgumentException(
                    "Aucun moment n'est défini pour ce séjour. Demandez à la direction de créer des moments avant "
                            + "de planifier des activités prestataires.");
        }
        if (momentIds == null || momentIds.isEmpty()) {
            throw new IllegalArgumentException("Au moins un moment est requis.");
        }
        LinkedHashSet<Integer> uniques = new LinkedHashSet<>(momentIds);
        List<Moment> result = new ArrayList<>();
        for (int mid : uniques) {
            result.add(resoudreMomentDuSejour(sejourId, mid));
        }
        result.sort(this::comparerMomentsChronologique);
        return result;
    }

    private int comparerMomentsChronologique(Moment a, Moment b) {
        int ordreA = a.getOrdre() != null ? a.getOrdre() : a.getId();
        int ordreB = b.getOrdre() != null ? b.getOrdre() : b.getId();
        int cmp = Integer.compare(ordreA, ordreB);
        if (cmp != 0) {
            return cmp;
        }
        return Integer.compare(a.getId(), b.getId());
    }

    private Groupe resoudreGroupeDuSejour(int sejourId, int groupeId) {
        Groupe groupe = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new ResourceNotFoundException("Groupe non trouvé avec l'ID: " + groupeId));
        if (groupe.getSejour() == null || groupe.getSejour().getId() != sejourId) {
            throw new IllegalArgumentException("Le groupe n'appartient pas à ce séjour");
        }
        return groupe;
    }

    private List<Groupe> resoudreGroupesDuSejour(int sejourId, List<Integer> groupeIds) {
        if (groupeIds == null || groupeIds.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Integer> uniques = new LinkedHashSet<>(groupeIds);
        List<Groupe> result = new ArrayList<>();
        for (int gid : uniques) {
            result.add(resoudreGroupeDuSejour(sejourId, gid));
        }
        return result;
    }

    private void verifierAbsenceConflitSortieMemeDateMomentGroupe(
            int sejourId,
            LocalDate date,
            List<Moment> moments,
            List<Groupe> groupes,
            Integer excludeActivitePrestataireId) {
        if (groupes.isEmpty()) {
            return;
        }
        for (Groupe groupe : groupes) {
            for (Moment moment : moments) {
                long conflits = activitePrestataireRepository.countAutreSortieMemeDateMomentGroupe(
                        sejourId, date, moment.getId(), groupe.getId(), excludeActivitePrestataireId);
                if (conflits > 0) {
                    throw new IllegalArgumentException(
                            "Une sortie est déjà planifiée pour le groupe « "
                                    + nullToDash(groupe.getNom())
                                    + " » le "
                                    + DateFormatHelper.formatDdMmYyyy(date)
                                    + " au moment « "
                                    + nullToDash(moment.getNom())
                                    + " ».");
                }
            }
        }
    }

    private static String nullToDash(String value) {
        if (value == null || value.isBlank()) {
            return "?";
        }
        return value.trim();
    }

    private void verifierDateDansSejour(Sejour sejour, LocalDate dateActivite) {
        Date debut = sejour.getDateDebut();
        Date fin = sejour.getDateFin();
        if (debut == null || fin == null) {
            throw new IllegalArgumentException(
                    "Le séjour doit avoir une date de début et une date de fin pour planifier une activité prestataire");
        }
        LocalDate jourDebut = dateVersJourCalendaire(debut);
        LocalDate jourFin = dateVersJourCalendaire(fin);
        if (dateActivite.isBefore(jourDebut) || dateActivite.isAfter(jourFin)) {
            throw new IllegalArgumentException(
                    "La date de l'activité prestataire doit être comprise entre le "
                            + DateFormatHelper.formatDdMmYyyy(jourDebut) + " et le "
                            + DateFormatHelper.formatDdMmYyyy(jourFin) + " (dates du séjour)");
        }
    }

    private static LocalDate dateVersJourCalendaire(Date date) {
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private ActivitePrestataireDto mapToDto(ActivitePrestataire activite) {
        List<MomentDto> moments = activite.getMoments() == null ? List.of() : activite.getMoments().stream()
                .sorted(this::comparerMomentsChronologique)
                .map(ActivitePrestataireServiceImpl::momentVersDto)
                .collect(Collectors.toList());
        List<Integer> groupeIds = activite.getGroupes() == null ? List.of() : activite.getGroupes().stream()
                .map(Groupe::getId)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
        List<NonParticipationPrestataireDto> nonParticipations =
                activite.getNonParticipations() == null ? List.of() : activite.getNonParticipations().stream()
                        .map(this::nonParticipationVersDto)
                        .sorted(Comparator.comparing(NonParticipationPrestataireDto::tokenId)
                                .thenComparingInt(NonParticipationPrestataireDto::momentId))
                        .collect(Collectors.toList());
        return new ActivitePrestataireDto(
                activite.getId(),
                activite.getNom(),
                activite.getDate(),
                moments,
                activite.getSejour().getId(),
                activite.getHeureDepart(),
                activite.getHeureRetour(),
                activite.getInformations(),
                activite.getTelephone(),
                groupeIds,
                nonParticipations);
    }

    private NonParticipationPrestataireDto nonParticipationVersDto(ActivitePrestataireNonParticipation np) {
        return new NonParticipationPrestataireDto(np.getUtilisateur().getTokenId(), np.getMoment().getId());
    }

    private static MomentDto momentVersDto(Moment moment) {
        int ordreAffiche = moment.getOrdre() != null ? moment.getOrdre() : moment.getId();
        return new MomentDto(moment.getId(), moment.getNom(), moment.getSejour().getId(), ordreAffiche);
    }
}
