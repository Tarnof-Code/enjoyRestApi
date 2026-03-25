package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.Activite;
import com.tarnof.enjoyrestapi.entities.Groupe;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.SejourEquipeId;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.CreateActiviteRequest;
import com.tarnof.enjoyrestapi.payload.request.UpdateActiviteRequest;
import com.tarnof.enjoyrestapi.payload.response.ActiviteDto;
import com.tarnof.enjoyrestapi.repositories.ActiviteRepository;
import com.tarnof.enjoyrestapi.repositories.GroupeRepository;
import com.tarnof.enjoyrestapi.repositories.SejourEquipeRepository;
import com.tarnof.enjoyrestapi.repositories.SejourRepository;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.services.ActiviteService;
import com.tarnof.enjoyrestapi.utils.DateFormatHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ActiviteServiceImpl implements ActiviteService {

    private final ActiviteRepository activiteRepository;
    private final SejourRepository sejourRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SejourEquipeRepository sejourEquipeRepository;
    private final GroupeRepository groupeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ActiviteDto> listerActivitesDuSejour(int sejourId) {
        verifierSejourExiste(sejourId);
        return activiteRepository.findBySejourIdOrderByDateAscIdAsc(sejourId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ActiviteDto getActivite(int sejourId, int activiteId) {
        Activite activite = activiteRepository.findByIdAndSejourId(activiteId, sejourId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Activité non trouvée pour ce séjour (id: " + activiteId + ")"));
        return toDto(activite);
    }

    @Override
    @Transactional
    public ActiviteDto creerActivite(int sejourId, CreateActiviteRequest request) {
        Sejour sejour = verifierSejourExiste(sejourId);
        verifierDateActiviteDansSejour(sejour, request.date());
        List<Utilisateur> membres = resoudreEtVerifierMembresEquipe(sejour, request.membreTokenIds());
        List<Groupe> groupes = resoudreGroupesDuSejour(sejourId, request.groupeIds());

        Activite activite = new Activite();
        activite.setDate(request.date());
        activite.setNom(request.nom());
        activite.setDescription(request.description());
        activite.setSejour(sejour);
        activite.setMembres(new ArrayList<>(membres));
        activite.setGroupes(new ArrayList<>(groupes));
        return toDto(activiteRepository.save(activite));
    }

    @Override
    @Transactional
    public ActiviteDto modifierActivite(int sejourId, int activiteId, UpdateActiviteRequest request) {
        Activite activite = activiteRepository.findByIdAndSejourId(activiteId, sejourId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Activité non trouvée pour ce séjour (id: " + activiteId + ")"));

        verifierDateActiviteDansSejour(activite.getSejour(), request.date());
        List<Utilisateur> membres = resoudreEtVerifierMembresEquipe(activite.getSejour(), request.membreTokenIds());
        List<Groupe> groupes = resoudreGroupesDuSejour(sejourId, request.groupeIds());

        activite.setDate(request.date());
        activite.setNom(request.nom());
        activite.setDescription(request.description());
        activite.getMembres().clear();
        activite.getMembres().addAll(membres);
        activite.getGroupes().clear();
        activite.getGroupes().addAll(groupes);
        return toDto(activiteRepository.save(activite));
    }

    @Override
    @Transactional
    public void supprimerActivite(int sejourId, int activiteId) {
        Activite activite = activiteRepository.findByIdAndSejourId(activiteId, sejourId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Activité non trouvée pour ce séjour (id: " + activiteId + ")"));
        activiteRepository.delete(activite);
    }

    private Sejour verifierSejourExiste(int sejourId) {
        return sejourRepository.findById(sejourId)
                .orElseThrow(() -> new ResourceNotFoundException("Séjour non trouvé avec l'ID: " + sejourId));
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

    private ActiviteDto toDto(Activite a) {
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
                membresInfos,
                groupeIds);
    }
}
