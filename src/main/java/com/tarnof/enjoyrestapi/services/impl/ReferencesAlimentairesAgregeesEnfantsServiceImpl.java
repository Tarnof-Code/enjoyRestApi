package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.DossierEnfant;
import com.tarnof.enjoyrestapi.entities.Enfant;
import com.tarnof.enjoyrestapi.entities.ReferenceAlimentaire;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.SejourEnfant;
import com.tarnof.enjoyrestapi.payload.response.ReferenceAlimentaireDto;
import com.tarnof.enjoyrestapi.payload.response.ReferencesAlimentairesAgregeesEnfantsDto;
import com.tarnof.enjoyrestapi.repositories.DossierEnfantRepository;
import com.tarnof.enjoyrestapi.services.ReferencesAlimentairesAgregeesEnfantsService;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReferencesAlimentairesAgregeesEnfantsServiceImpl implements ReferencesAlimentairesAgregeesEnfantsService {

    private static final Comparator<ReferenceAlimentaire> TRI_REFERENCE =
            Comparator.comparing(ReferenceAlimentaire::getOrdre, Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(ReferenceAlimentaire::getId);

    private final SejourVerificationService sejourVerificationService;
    private final DossierEnfantRepository dossierEnfantRepository;

    public ReferencesAlimentairesAgregeesEnfantsServiceImpl(
            SejourVerificationService sejourVerificationService,
            DossierEnfantRepository dossierEnfantRepository) {
        this.sejourVerificationService = sejourVerificationService;
        this.dossierEnfantRepository = dossierEnfantRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ReferencesAlimentairesAgregeesEnfantsDto agregerPourSejour(int sejourId) {
        Sejour sejour = sejourVerificationService.verifierSejourExiste(sejourId);
        List<Integer> enfantIds =
                Optional.ofNullable(sejour.getEnfants()).orElse(List.of()).stream()
                        .map(SejourEnfant::getEnfant)
                        .mapToInt(Enfant::getId)
                        .boxed()
                        .distinct()
                        .collect(Collectors.toList());

        if (enfantIds.isEmpty()) {
            return new ReferencesAlimentairesAgregeesEnfantsDto(List.of(), List.of());
        }

        List<DossierEnfant> dossiers = dossierEnfantRepository.findByEnfantIdInFetchingReferences(enfantIds);
        Set<ReferenceAlimentaire> allergenes = new HashSet<>();
        Set<ReferenceAlimentaire> regimes = new HashSet<>();
        for (DossierEnfant d : dossiers) {
            allergenes.addAll(d.getAllergenes());
            regimes.addAll(d.getRegimesEtPreferences());
        }

        return new ReferencesAlimentairesAgregeesEnfantsDto(
                allergenes.stream().sorted(TRI_REFERENCE).map(this::mapReferenceDto).collect(Collectors.toList()),
                regimes.stream().sorted(TRI_REFERENCE).map(this::mapReferenceDto).collect(Collectors.toList()));
    }

    private ReferenceAlimentaireDto mapReferenceDto(ReferenceAlimentaire r) {
        return new ReferenceAlimentaireDto(r.getId(), r.getType(), r.getLibelle(), r.getOrdre(), r.isActif());
    }
}
