package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.TypeActiviteLibellesParDefaut;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.TypeActivite;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.SaveTypeActiviteRequest;
import com.tarnof.enjoyrestapi.payload.response.TypeActiviteDto;
import com.tarnof.enjoyrestapi.repositories.ActiviteRepository;
import com.tarnof.enjoyrestapi.repositories.TypeActiviteRepository;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
import com.tarnof.enjoyrestapi.services.TypeActiviteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@SuppressWarnings("null")
public class TypeActiviteServiceImpl implements TypeActiviteService {

    private final TypeActiviteRepository typeActiviteRepository;
    private final ActiviteRepository activiteRepository;
    private final SejourVerificationService sejourVerificationService;

    public TypeActiviteServiceImpl(
            TypeActiviteRepository typeActiviteRepository,
            ActiviteRepository activiteRepository,
            SejourVerificationService sejourVerificationService) {
        this.typeActiviteRepository = typeActiviteRepository;
        this.activiteRepository = activiteRepository;
        this.sejourVerificationService = sejourVerificationService;
    }

    @Override
    @Transactional
    public void assurerTypesParDefautPourSejour(int sejourId) {
        Sejour sejour = sejourVerificationService.verifierSejourExiste(sejourId);
        for (String libelle : TypeActiviteLibellesParDefaut.LIBELLES) {
            typeActiviteRepository
                    .findBySejourIdAndLibelleIgnoreCase(sejourId, libelle)
                    .ifPresentOrElse(
                            t -> {
                                if (!t.isPredefini()) {
                                    t.setPredefini(true);
                                    typeActiviteRepository.save(t);
                                }
                            },
                            () -> {
                                TypeActivite t = new TypeActivite();
                                t.setLibelle(libelle);
                                t.setPredefini(true);
                                t.setSejour(sejour);
                                typeActiviteRepository.save(t);
                            });
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TypeActiviteDto> listerTypesActivite(int sejourId) {
        sejourVerificationService.verifierSejourExiste(sejourId);
        return typeActiviteRepository.findBySejourIdOrderByLibelleAsc(sejourId).stream()
                .map(TypeActiviteServiceImpl::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TypeActiviteDto getTypeActivite(int sejourId, int id) {
        return toDto(typeActiviteParIdEtSejour(id, sejourId));
    }

    @Override
    @Transactional
    public TypeActiviteDto creerTypeActivite(int sejourId, SaveTypeActiviteRequest request) {
        Sejour sejour = sejourVerificationService.verifierSejourExiste(sejourId);
        String libelle = normaliserLibelle(request.libelle());
        verifierLibelleUnique(sejourId, libelle, null);
        TypeActivite entity = new TypeActivite();
        entity.setLibelle(libelle);
        entity.setPredefini(false);
        entity.setSejour(sejour);
        return toDto(typeActiviteRepository.save(entity));
    }

    @Override
    @Transactional
    public TypeActiviteDto modifierTypeActivite(int sejourId, int id, SaveTypeActiviteRequest request) {
        TypeActivite entity = typeActiviteParIdEtSejour(id, sejourId);
        verifierModifiableOuSupprimable(entity);
        String libelle = normaliserLibelle(request.libelle());
        verifierLibelleUnique(sejourId, libelle, id);
        entity.setLibelle(libelle);
        return toDto(typeActiviteRepository.save(entity));
    }

    @Override
    @Transactional
    public void supprimerTypeActivite(int sejourId, int id) {
        TypeActivite entity = typeActiviteParIdEtSejour(id, sejourId);
        verifierModifiableOuSupprimable(entity);
        long usages = activiteRepository.countByTypeActivite_Id(id);
        if (usages > 0) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer ce type d'activité : "
                            + usages
                            + " activité(s) y sont encore associée(s). Retirez le type sur ces activités d'abord.");
        }
        typeActiviteRepository.delete(entity);
    }

    private TypeActivite typeActiviteParIdEtSejour(int id, int sejourId) {
        return typeActiviteRepository
                .findByIdAndSejourId(id, sejourId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Type d'activité non trouvé pour ce séjour (id: " + id + ")"));
    }

    private static void verifierModifiableOuSupprimable(TypeActivite entity) {
        if (entity.isPredefini()) {
            throw new IllegalArgumentException(
                    "Ce type d'activité fait partie des types fournis par défaut et ne peut pas être modifié ni "
                            + "supprimé. Les types que vous avez ajoutés restent modifiables et supprimables.");
        }
    }

    private static String normaliserLibelle(String libelle) {
        return libelle.trim();
    }

    private void verifierLibelleUnique(int sejourId, String libelle, Integer excludeId) {
        boolean taken =
                excludeId == null
                        ? typeActiviteRepository.existsBySejourIdAndLibelleIgnoreCase(sejourId, libelle)
                        : typeActiviteRepository.existsBySejourIdAndLibelleIgnoreCaseAndIdNot(
                                sejourId, libelle, excludeId);
        if (taken) {
            throw new IllegalArgumentException("Un type d'activité avec ce libellé existe déjà pour ce séjour.");
        }
    }

    private static TypeActiviteDto toDto(TypeActivite t) {
        return new TypeActiviteDto(t.getId(), t.getLibelle(), t.isPredefini(), t.getSejour().getId());
    }
}
