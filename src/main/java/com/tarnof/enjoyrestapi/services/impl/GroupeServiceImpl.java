package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.*;
import com.tarnof.enjoyrestapi.enums.TypeGroupe;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.AjouterReferentRequest;
import com.tarnof.enjoyrestapi.payload.request.CreateGroupeRequest;
import com.tarnof.enjoyrestapi.payload.response.EnfantDto;
import com.tarnof.enjoyrestapi.payload.response.GroupeDto;
import com.tarnof.enjoyrestapi.repositories.*;
import com.tarnof.enjoyrestapi.services.GroupeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null") // save(), findById().orElseThrow() garantissent des retours non-null à l'exécution
public class GroupeServiceImpl implements GroupeService {

    private final GroupeRepository groupeRepository;
    private final SejourRepository sejourRepository;
    private final EnfantRepository enfantRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SejourEnfantRepository sejourEnfantRepository;

    @Override
    public List<GroupeDto> getGroupesDuSejour(int sejourId) {
        verifierSejourExiste(sejourId);
        return groupeRepository.findBySejourId(sejourId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public GroupeDto getGroupeById(int sejourId, int groupeId) {
        Groupe groupe = getGroupeEtVerifierSejour(sejourId, groupeId);
        return mapToDto(groupe);
    }

    @Override
    @Transactional
    public GroupeDto creerGroupe(int sejourId, CreateGroupeRequest request) {
        Sejour sejour = verifierSejourExiste(sejourId);
        validerTranche(request);

        Groupe groupe = Groupe.builder()
                .nom(request.nom())
                .description(request.description())
                .typeGroupe(request.typeGroupe())
                .ageMin(request.ageMin())
                .ageMax(request.ageMax())
                .niveauScolaireMin(request.niveauScolaireMin())
                .niveauScolaireMax(request.niveauScolaireMax())
                .sejour(sejour)
                .build();
        Groupe saved = groupeRepository.save(groupe);

        ajouterEnfantsSelonTranche(saved, sejour);
        groupeRepository.save(saved);
        return mapToDto(groupeRepository.findById(saved.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public GroupeDto modifierGroupe(int sejourId, int groupeId, CreateGroupeRequest request) {
        Groupe groupe = getGroupeEtVerifierSejour(sejourId, groupeId);
        validerTranche(request);

        groupe.setNom(request.nom());
        groupe.setDescription(request.description());
        groupe.setTypeGroupe(request.typeGroupe());
        groupe.setAgeMin(request.ageMin());
        groupe.setAgeMax(request.ageMax());
        groupe.setNiveauScolaireMin(request.niveauScolaireMin());
        groupe.setNiveauScolaireMax(request.niveauScolaireMax());
        return mapToDto(groupeRepository.save(groupe));
    }

    @Override
    @Transactional
    public void supprimerGroupe(int sejourId, int groupeId) {
        Groupe groupe = getGroupeEtVerifierSejour(sejourId, groupeId);
        groupeRepository.delete(groupe);
    }

    @Override
    @Transactional
    public void ajouterEnfantAuGroupe(int sejourId, int groupeId, int enfantId) {
        Groupe groupe = getGroupeEtVerifierSejour(sejourId, groupeId);
        Enfant enfant = enfantRepository.findById(enfantId)
                .orElseThrow(() -> new ResourceNotFoundException("Enfant non trouvé avec l'ID: " + enfantId));
        verifierEnfantDansSejour(sejourId, enfantId);
        if (groupe.getEnfants().stream().anyMatch(e -> e.getId() == enfantId)) {
            throw new ResourceAlreadyExistsException("Cet enfant fait déjà partie du groupe");
        }
        groupe.getEnfants().add(enfant);
        groupeRepository.save(groupe);
    }

    @Override
    @Transactional
    public void retirerEnfantDuGroupe(int sejourId, int groupeId, int enfantId) {
        Groupe groupe = getGroupeEtVerifierSejour(sejourId, groupeId);
        boolean removed = groupe.getEnfants().removeIf(e -> e.getId() == enfantId);
        if (!removed) {
            throw new ResourceNotFoundException("Cet enfant ne fait pas partie du groupe");
        }
        groupeRepository.save(groupe);
    }

    @Override
    @Transactional
    public void ajouterReferent(int sejourId, int groupeId, AjouterReferentRequest request) {
        Groupe groupe = getGroupeEtVerifierSejour(sejourId, groupeId);
        Utilisateur referent = utilisateurRepository.findByTokenId(request.referentTokenId())
                .orElseThrow(() -> new ResourceNotFoundException("Référent non trouvé avec l'ID: " + request.referentTokenId()));
        if (groupe.getReferents().stream().anyMatch(r -> r.getId() == referent.getId())) {
            throw new ResourceAlreadyExistsException("Ce référent fait déjà partie du groupe");
        }
        groupe.getReferents().add(referent);
        groupeRepository.save(groupe);
    }

    @Override
    @Transactional
    public void retirerReferent(int sejourId, int groupeId, String referentTokenId) {
        Groupe groupe = getGroupeEtVerifierSejour(sejourId, groupeId);
        Utilisateur referent = utilisateurRepository.findByTokenId(referentTokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Référent non trouvé avec l'ID: " + referentTokenId));
        boolean removed = groupe.getReferents().removeIf(r -> r.getId() == referent.getId());
        if (!removed) {
            throw new ResourceNotFoundException("Ce référent ne fait pas partie du groupe");
        }
        groupeRepository.save(groupe);
    }

    private void validerTranche(CreateGroupeRequest request) {
        switch (request.typeGroupe()) {
            case THEMATIQUE -> { /* Aucune tranche requise, enfants ajoutés manuellement */ }
            case AGE -> {
                if (request.ageMin() == null || request.ageMax() == null) {
                    throw new IllegalArgumentException("Pour un groupe par âge, ageMin et ageMax sont obligatoires");
                }
                if (request.ageMin() > request.ageMax()) {
                    throw new IllegalArgumentException("ageMin doit être inférieur ou égal à ageMax");
                }
            }
            case NIVEAU_SCOLAIRE -> {
                if (request.niveauScolaireMin() == null || request.niveauScolaireMax() == null) {
                    throw new IllegalArgumentException("Pour un groupe par niveau scolaire, niveauScolaireMin et niveauScolaireMax sont obligatoires");
                }
                if (request.niveauScolaireMin().ordinal() > request.niveauScolaireMax().ordinal()) {
                    throw new IllegalArgumentException("niveauScolaireMin doit être antérieur ou égal à niveauScolaireMax");
                }
            }
        }
    }

    private void ajouterEnfantsSelonTranche(Groupe groupe, Sejour sejour) {
        List<SejourEnfant> sejourEnfants = sejourEnfantRepository.findBySejourIdWithEnfant(sejour.getId());
        Date dateReference = sejour.getDateDebut() != null ? sejour.getDateDebut() : new Date();
        LocalDate refDate = toLocalDate(dateReference);

        for (SejourEnfant se : sejourEnfants) {
            Enfant enfant = se.getEnfant();
            boolean correspond = false;

            if (groupe.getTypeGroupe() == TypeGroupe.AGE && groupe.getAgeMin() != null && groupe.getAgeMax() != null) {
                int age = (int) ChronoUnit.YEARS.between(
                        toLocalDate(enfant.getDateNaissance()),
                        refDate
                );
                correspond = age >= groupe.getAgeMin() && age <= groupe.getAgeMax();
            } else if (groupe.getTypeGroupe() == TypeGroupe.NIVEAU_SCOLAIRE && groupe.getNiveauScolaireMin() != null && groupe.getNiveauScolaireMax() != null) {
                int ord = enfant.getNiveauScolaire().ordinal();
                correspond = ord >= groupe.getNiveauScolaireMin().ordinal()
                        && ord <= groupe.getNiveauScolaireMax().ordinal();
            }

            if (correspond && groupe.getEnfants().stream().noneMatch(e -> e.getId() == enfant.getId())) {
                groupe.getEnfants().add(enfant);
            }
        }
    }

    /** Conversion Date -> LocalDate compatible avec java.sql.Date (évite UnsupportedOperationException sur toInstant()) */
    private LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Sejour verifierSejourExiste(int sejourId) {
        return sejourRepository.findById(sejourId)
                .orElseThrow(() -> new ResourceNotFoundException("Séjour non trouvé avec l'ID: " + sejourId));
    }

    private Groupe getGroupeEtVerifierSejour(int sejourId, int groupeId) {
        Groupe groupe = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new ResourceNotFoundException("Groupe non trouvé avec l'ID: " + groupeId));
        if (groupe.getSejour().getId() != sejourId) {
            throw new ResourceNotFoundException("Le groupe n'appartient pas à ce séjour");
        }
        return groupe;
    }

    private void verifierEnfantDansSejour(int sejourId, int enfantId) {
        if (!sejourEnfantRepository.existsById(new SejourEnfantId(sejourId, enfantId))) {
            throw new IllegalArgumentException("L'enfant doit d'abord être inscrit au séjour");
        }
    }

    private GroupeDto mapToDto(Groupe groupe) {
        List<EnfantDto> enfants = groupe.getEnfants() == null ? List.of() : groupe.getEnfants().stream()
                .map(e -> new EnfantDto(
                        e.getId(),
                        e.getNom(),
                        e.getPrenom(),
                        e.getGenre(),
                        e.getDateNaissance(),
                        e.getNiveauScolaire()
                ))
                .collect(Collectors.toList());
        List<GroupeDto.ReferentInfos> referents = groupe.getReferents() == null ? List.of() : groupe.getReferents().stream()
                .map(r -> new GroupeDto.ReferentInfos(
                        r.getTokenId(),
                        r.getNom(),
                        r.getPrenom()
                ))
                .collect(Collectors.toList());
        return new GroupeDto(
                groupe.getId(),
                groupe.getNom(),
                groupe.getDescription(),
                groupe.getTypeGroupe(),
                groupe.getAgeMin(),
                groupe.getAgeMax(),
                groupe.getNiveauScolaireMin(),
                groupe.getNiveauScolaireMax(),
                groupe.getSejour().getId(),
                enfants,
                referents
        );
    }
}
