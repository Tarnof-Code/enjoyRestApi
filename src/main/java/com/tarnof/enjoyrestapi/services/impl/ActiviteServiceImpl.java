package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.Activite;
import com.tarnof.enjoyrestapi.entities.Groupe;
import com.tarnof.enjoyrestapi.entities.Lieu;
import com.tarnof.enjoyrestapi.entities.Moment;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.SejourEquipeId;
import com.tarnof.enjoyrestapi.entities.TypeActivite;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.CreateActiviteRequest;
import com.tarnof.enjoyrestapi.payload.request.UpdateActiviteRequest;
import com.tarnof.enjoyrestapi.payload.response.ActiviteDto;
import com.tarnof.enjoyrestapi.payload.response.LieuDto;
import com.tarnof.enjoyrestapi.payload.response.MomentDto;
import com.tarnof.enjoyrestapi.payload.response.TypeActiviteDto;
import com.tarnof.enjoyrestapi.repositories.ActiviteRepository;
import com.tarnof.enjoyrestapi.repositories.GroupeRepository;
import com.tarnof.enjoyrestapi.repositories.LieuRepository;
import com.tarnof.enjoyrestapi.repositories.MomentRepository;
import com.tarnof.enjoyrestapi.repositories.SejourEquipeRepository;
import com.tarnof.enjoyrestapi.repositories.TypeActiviteRepository;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.services.ActiviteService;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
import com.tarnof.enjoyrestapi.utils.DateFormatHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class ActiviteServiceImpl implements ActiviteService {

    private final ActiviteRepository activiteRepository;
    private final SejourVerificationService sejourVerificationService;
    private final UtilisateurRepository utilisateurRepository;
    private final SejourEquipeRepository sejourEquipeRepository;
    private final GroupeRepository groupeRepository;
    private final LieuRepository lieuRepository;
    private final MomentRepository momentRepository;
    private final TypeActiviteRepository typeActiviteRepository;

    public ActiviteServiceImpl(ActiviteRepository activiteRepository, SejourVerificationService sejourVerificationService,
                               UtilisateurRepository utilisateurRepository, SejourEquipeRepository sejourEquipeRepository,
                               GroupeRepository groupeRepository, LieuRepository lieuRepository,
                               MomentRepository momentRepository, TypeActiviteRepository typeActiviteRepository) {
        this.activiteRepository = activiteRepository;
        this.sejourVerificationService = sejourVerificationService;
        this.utilisateurRepository = utilisateurRepository;
        this.sejourEquipeRepository = sejourEquipeRepository;
        this.groupeRepository = groupeRepository;
        this.lieuRepository = lieuRepository;
        this.momentRepository = momentRepository;
        this.typeActiviteRepository = typeActiviteRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActiviteDto> listerActivitesDuSejour(int sejourId) {
        sejourVerificationService.verifierSejourExiste(sejourId);
        return activiteRepository.findBySejourIdOrderByDateAscIdAsc(sejourId).stream()
                .map(a -> toDto(a, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ActiviteDto getActivite(int sejourId, int activiteId) {
        Activite activite = activiteRepository.findByIdAndSejourId(activiteId, sejourId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Activité non trouvée pour ce séjour (id: " + activiteId + ")"));
        return toDto(activite, null);
    }

    @Override
    @Transactional
    public ActiviteDto creerActivite(int sejourId, CreateActiviteRequest request) {
        Sejour sejour = sejourVerificationService.verifierSejourExiste(sejourId);
        verifierMomentsEtResolution(sejourId, request.momentId());
        Moment moment = resoudreMomentPourSejour(sejourId, request.momentId());
        verifierDateActiviteDansSejour(sejour, request.date());
        List<Utilisateur> membres = resoudreEtVerifierMembresEquipe(sejour, request.membreTokenIds());
        List<Groupe> groupes = resoudreGroupesDuSejour(sejourId, request.groupeIds());
        Lieu lieu = resoudreLieuPourSejour(sejourId, request.lieuId());
        TypeActivite typeActivite = resoudreTypeActivite(sejourId, request.typeActiviteId());
        String avertissementLieu = verifierDisponibiliteLieuPourActivite(
                lieu, request.date(), sejourId, null, moment);

        Activite activite = new Activite();
        activite.setDate(request.date());
        activite.setNom(request.nom());
        activite.setDescription(request.description());
        activite.setLieu(lieu);
        activite.setMoment(moment);
        activite.setTypeActivite(typeActivite);
        activite.setSejour(sejour);
        activite.setMembres(new ArrayList<>(membres));
        activite.setGroupes(new ArrayList<>(groupes));
        activite = activiteRepository.save(activite);
        return toDto(activite, avertissementLieu);
    }

    @Override
    @Transactional
    public ActiviteDto modifierActivite(int sejourId, int activiteId, UpdateActiviteRequest request) {
        Activite activite = activiteRepository.findByIdAndSejourId(activiteId, sejourId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Activité non trouvée pour ce séjour (id: " + activiteId + ")"));

        verifierMomentsEtResolution(sejourId, request.momentId());
        Moment moment = resoudreMomentPourSejour(sejourId, request.momentId());
        verifierDateActiviteDansSejour(activite.getSejour(), request.date());
        List<Utilisateur> membres = resoudreEtVerifierMembresEquipe(activite.getSejour(), request.membreTokenIds());
        List<Groupe> groupes = resoudreGroupesDuSejour(sejourId, request.groupeIds());
        Lieu lieu = resoudreLieuPourSejour(sejourId, request.lieuId());
        TypeActivite typeActivite = resoudreTypeActivite(sejourId, request.typeActiviteId());
        String avertissementLieu = verifierDisponibiliteLieuPourActivite(
                lieu, request.date(), sejourId, activite.getId(), moment);

        activite.setDate(request.date());
        activite.setNom(request.nom());
        activite.setDescription(request.description());
        activite.setLieu(lieu);
        activite.setMoment(moment);
        activite.setTypeActivite(typeActivite);
        activite.getMembres().clear();
        activite.getMembres().addAll(membres);
        activite.getGroupes().clear();
        activite.getGroupes().addAll(groupes);
        activite = activiteRepository.save(activite);
        return toDto(activite, avertissementLieu);
    }

    @Override
    @Transactional
    public void supprimerActivite(int sejourId, int activiteId) {
        Activite activite = activiteRepository.findByIdAndSejourId(activiteId, sejourId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Activité non trouvée pour ce séjour (id: " + activiteId + ")"));
        activiteRepository.delete(activite);
    }

    private void verifierDateActiviteDansSejour(Sejour sejour, LocalDate dateActivite) {
        Date debut = sejour.getDateDebut();
        Date fin = sejour.getDateFin();
        if (debut == null || fin == null) {
            throw new IllegalArgumentException(
                    "Le séjour doit avoir une date de début et une date de fin pour planifier une activité");
        }
        LocalDate jourDebut = dateVersJourCalendaire(debut);
        LocalDate jourFin = dateVersJourCalendaire(fin);
        if (dateActivite.isBefore(jourDebut) || dateActivite.isAfter(jourFin)) {
            throw new IllegalArgumentException(
                    "La date de l'activité doit être comprise entre le "
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

    private List<Utilisateur> resoudreEtVerifierMembresEquipe(Sejour sejour, List<String> membreTokenIds) {
        LinkedHashSet<String> uniques = new LinkedHashSet<>(membreTokenIds);
        List<Utilisateur> result = new ArrayList<>();
        int sejourId = sejour.getId();
        Utilisateur directeur = sejour.getDirecteur();
        for (String tokenId : uniques) {
            Utilisateur u = utilisateurRepository.findByTokenId(tokenId)
                    .orElseThrow(() -> new ResourceNotFoundException("Membre non trouvé avec l'ID: " + tokenId));
            boolean estDirecteurDuSejour = directeur != null && directeur.getId() == u.getId();
            if (!estDirecteurDuSejour) {
                SejourEquipeId eqId = new SejourEquipeId(sejourId, u.getId());
                if (!sejourEquipeRepository.existsById(eqId)) {
                    throw new IllegalArgumentException(
                            "L'utilisateur « " + tokenId + " » ne fait pas partie de l'équipe de ce séjour");
                }
            }
            result.add(u);
        }
        return result;
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
        LinkedHashSet<Integer> uniques = new LinkedHashSet<>(groupeIds);
        List<Groupe> result = new ArrayList<>();
        for (int gid : uniques) {
            result.add(resoudreGroupeDuSejour(sejourId, gid));
        }
        return result;
    }

    private Lieu resoudreLieuPourSejour(int sejourId, Integer lieuId) {
        if (lieuId == null) {
            return null;
        }
        return lieuRepository.findByIdAndSejourId(lieuId, sejourId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lieu non trouvé pour ce séjour (id: " + lieuId + ")"));
    }

    private TypeActivite resoudreTypeActivite(int sejourId, Integer typeActiviteId) {
        if (typeActiviteId == null) {
            throw new IllegalArgumentException("Le type d'activité est obligatoire pour chaque activité.");
        }
        return typeActiviteRepository
                .findByIdAndSejourId(typeActiviteId, sejourId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Type d'activité non trouvé pour ce séjour (id: " + typeActiviteId + ")"));
    }

    private void verifierMomentsEtResolution(int sejourId, Integer momentId) {
        if (momentRepository.countBySejourId(sejourId) == 0) {
            throw new IllegalArgumentException(
                    "Aucun moment n'est défini pour ce séjour. Demandez à la direction de créer des moments avant "
                            + "de planifier des activités.");
        }
        if (momentId == null) {
            throw new IllegalArgumentException("Le moment est obligatoire pour chaque activité.");
        }
    }

    private Moment resoudreMomentPourSejour(int sejourId, Integer momentId) {
        return momentRepository.findByIdAndSejourId(momentId, sejourId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Moment non trouvé pour ce séjour (id: " + momentId + ")"));
    }

    private String verifierDisponibiliteLieuPourActivite(
            Lieu lieu, LocalDate date, int sejourId, Integer excludeActiviteId, Moment moment) {
        if (lieu == null) {
            return null;
        }
        int momentId = moment.getId();
        long autres = excludeActiviteId == null
                ? activiteRepository.countBySejour_IdAndLieu_IdAndDateAndMoment_Id(
                        sejourId, lieu.getId(), date, momentId)
                : activiteRepository.countBySejour_IdAndLieu_IdAndDateAndMoment_IdAndIdNot(
                        sejourId, lieu.getId(), date, momentId, excludeActiviteId);

        if (autres == 0) {
            return null;
        }

        if (!lieu.isPartageableEntreAnimateurs()) {
            throw new IllegalArgumentException(
                    "Ce lieu est déjà utilisé par une autre activité le "
                            + DateFormatHelper.formatDdMmYyyy(date)
                            + " pour le moment « "
                            + moment.getNom()
                            + " ». Il n’est pas configuré pour être partagé entre animateurs.");
        }

        Integer max = lieu.getNombreMaxActivitesSimultanees();
        if (max == null) {
            throw new IllegalStateException(
                    "Lieu partageable sans nombre maximal d’activités simultanées : configuration invalide (lieu id "
                            + lieu.getId()
                            + ").");
        }

        if (autres >= max) {
            throw new IllegalArgumentException(
                    "Vous ne pouvez pas utiliser ce lieu le "
                            + DateFormatHelper.formatDdMmYyyy(date)
                            + " pour le moment « "
                            + moment.getNom()
                            + " » : la limite de partage ("
                            + max
                            + " activité(s) au maximum) est déjà atteinte.");
        }

        return "Ce lieu est déjà affecté à "
                + autres
                + " autre(s) activité(s) le "
                + DateFormatHelper.formatDdMmYyyy(date)
                + " pour le moment « "
                + moment.getNom()
                + " ». L’affectation est acceptée car le lieu autorise le partage et la limite n’est pas encore atteinte.";
    }

    private static LieuDto lieuVersDto(Lieu lieu) {
        if (lieu == null) {
            return null;
        }
        return new LieuDto(
                lieu.getId(),
                lieu.getNom(),
                lieu.getEmplacement(),
                lieu.getNombreMax(),
                lieu.isPartageableEntreAnimateurs(),
                lieu.getNombreMaxActivitesSimultanees(),
                lieu.getSejour().getId());
    }

    private static MomentDto momentVersDto(Moment moment) {
        int ordreAffiche =
                moment.getOrdre() != null ? moment.getOrdre() : moment.getId();
        return new MomentDto(moment.getId(), moment.getNom(), moment.getSejour().getId(), ordreAffiche);
    }

    private static TypeActiviteDto typeActiviteVersDto(TypeActivite typeActivite) {
        Objects.requireNonNull(typeActivite, "typeActivite");
        return new TypeActiviteDto(
                typeActivite.getId(),
                typeActivite.getLibelle(),
                typeActivite.isPredefini(),
                typeActivite.getSejour().getId());
    }

    private ActiviteDto toDto(Activite a, String avertissementLieu) {
        List<ActiviteDto.MembreEquipeInfo> membresInfos = a.getMembres().stream()
                .map(u -> new ActiviteDto.MembreEquipeInfo(u.getTokenId(), u.getNom(), u.getPrenom()))
                .collect(Collectors.toList());
        List<Integer> groupeIds = a.getGroupes() == null ? List.of() : a.getGroupes().stream()
                .map(Groupe::getId)
                .sorted()
                .collect(Collectors.toList());
        return new ActiviteDto(
                a.getId(),
                a.getDate(),
                a.getNom(),
                a.getDescription(),
                a.getSejour().getId(),
                momentVersDto(a.getMoment()),
                lieuVersDto(a.getLieu()),
                typeActiviteVersDto(a.getTypeActivite()),
                membresInfos,
                groupeIds,
                avertissementLieu);
    }
}
