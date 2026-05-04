package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.ReferenceAlimentaire;
import com.tarnof.enjoyrestapi.enums.TypeReferenceAlimentaire;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.SaveReferenceAlimentaireRequest;
import com.tarnof.enjoyrestapi.payload.request.UpdateReferenceAlimentaireRequest;
import com.tarnof.enjoyrestapi.payload.response.ReferenceAlimentaireDto;
import com.tarnof.enjoyrestapi.repositories.ReferenceAlimentaireRepository;
import com.tarnof.enjoyrestapi.services.ReferenceAlimentaireService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReferenceAlimentaireServiceImpl implements ReferenceAlimentaireService {

    private final ReferenceAlimentaireRepository referenceAlimentaireRepository;

    public ReferenceAlimentaireServiceImpl(ReferenceAlimentaireRepository referenceAlimentaireRepository) {
        this.referenceAlimentaireRepository = referenceAlimentaireRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReferenceAlimentaireDto> lister(TypeReferenceAlimentaire type) {
        List<ReferenceAlimentaire> liste =
                type != null
                        ? referenceAlimentaireRepository.findByTypeOrderByOrdreAscIdAsc(type)
                        : referenceAlimentaireRepository.findAllByOrderByTypeAscOrdreAscIdAsc();
        return liste.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReferenceAlimentaireDto getById(int id) {
        return mapToDto(
                referenceAlimentaireRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Référence alimentaire introuvable : " + id)));
    }

    @Override
    @Transactional
    public ReferenceAlimentaireDto creer(SaveReferenceAlimentaireRequest request) {
        String libelle = normaliserLibelle(request.libelle());
        if (referenceAlimentaireRepository.existsByTypeAndLibelleIgnoreCase(request.type(), libelle)) {
            throw new ResourceAlreadyExistsException(
                    "Une entrée avec ce libellé existe déjà pour le type " + request.type());
        }
        ReferenceAlimentaire r = new ReferenceAlimentaire();
        r.setType(request.type());
        r.setLibelle(libelle);
        r.setOrdre(request.ordre());
        r.setActif(true);
        return mapToDto(referenceAlimentaireRepository.save(r));
    }

    @Override
    @Transactional
    public ReferenceAlimentaireDto modifier(int id, UpdateReferenceAlimentaireRequest request) {
        ReferenceAlimentaire r =
                referenceAlimentaireRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Référence alimentaire introuvable : " + id));
        String libelle = normaliserLibelle(request.libelle());
        referenceAlimentaireRepository
                .findByTypeAndLibelleIgnoreCase(r.getType(), libelle)
                .filter(existing -> existing.getId() != id)
                .ifPresent(
                        x -> {
                            throw new ResourceAlreadyExistsException(
                                    "Une entrée avec ce libellé existe déjà pour ce type.");
                        });
        r.setLibelle(libelle);
        r.setOrdre(request.ordre());
        r.setActif(request.actif());
        return mapToDto(referenceAlimentaireRepository.save(r));
    }

    @Override
    @Transactional
    public void supprimer(int id) {
        if (!referenceAlimentaireRepository.existsById(id)) {
            throw new ResourceNotFoundException("Référence alimentaire introuvable : " + id);
        }
        long usages =
                referenceAlimentaireRepository.countUsageAsAllergene(id)
                        + referenceAlimentaireRepository.countUsageAsRegime(id)
                        + referenceAlimentaireRepository.countUsageMenuAsAllergene(id)
                        + referenceAlimentaireRepository.countUsageMenuAsRegime(id);
        if (usages > 0) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer cette référence : elle est encore utilisée dans un dossier enfant ou un menu.");
        }
        referenceAlimentaireRepository.deleteById(id);
    }

    private static String normaliserLibelle(String libelle) {
        return libelle == null ? "" : libelle.trim().replaceAll("\\s+", " ");
    }

    private ReferenceAlimentaireDto mapToDto(ReferenceAlimentaire r) {
        return new ReferenceAlimentaireDto(r.getId(), r.getType(), r.getLibelle(), r.getOrdre(), r.isActif());
    }
}
