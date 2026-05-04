package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.MenuRepas;
import com.tarnof.enjoyrestapi.entities.ReferenceAlimentaire;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.enums.TypeReferenceAlimentaire;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.SaveMenuRepasRequest;
import com.tarnof.enjoyrestapi.payload.response.MenuRepasDto;
import com.tarnof.enjoyrestapi.payload.response.ReferenceAlimentaireDto;
import com.tarnof.enjoyrestapi.repositories.MenuRepasRepository;
import com.tarnof.enjoyrestapi.repositories.ReferenceAlimentaireRepository;
import com.tarnof.enjoyrestapi.services.MenuRepasService;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class MenuRepasServiceImpl implements MenuRepasService {

    private final MenuRepasRepository menuRepasRepository;
    private final SejourVerificationService sejourVerificationService;
    private final ReferenceAlimentaireRepository referenceAlimentaireRepository;

    public MenuRepasServiceImpl(
            MenuRepasRepository menuRepasRepository,
            SejourVerificationService sejourVerificationService,
            ReferenceAlimentaireRepository referenceAlimentaireRepository) {
        this.menuRepasRepository = menuRepasRepository;
        this.sejourVerificationService = sejourVerificationService;
        this.referenceAlimentaireRepository = referenceAlimentaireRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuRepasDto> listerParJour(int sejourId, LocalDate date) {
        sejourVerificationService.verifierSejourExiste(sejourId);
        return menuRepasRepository.findBySejour_IdAndDateRepasOrderByTypeRepasAsc(sejourId, date).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuRepasDto> listerParPeriode(int sejourId, LocalDate dateDebutInclusive, LocalDate dateFinInclusive) {
        sejourVerificationService.verifierSejourExiste(sejourId);
        if (dateDebutInclusive.isAfter(dateFinInclusive)) {
            throw new IllegalArgumentException("La date de début doit être antérieure ou égale à la date de fin.");
        }
        return menuRepasRepository
                .findBySejour_IdAndDateRepasBetweenOrderByDateRepasAscTypeRepasAsc(
                        sejourId, dateDebutInclusive, dateFinInclusive)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MenuRepasDto get(int sejourId, int menuId) {
        return mapToDto(getMenuDuSejour(sejourId, menuId));
    }

    @Override
    @Transactional
    public MenuRepasDto creer(int sejourId, SaveMenuRepasRequest request) {
        Sejour sejour = sejourVerificationService.verifierSejourExiste(sejourId);
        menuRepasRepository
                .findBySejour_IdAndDateRepasAndTypeRepas(sejourId, request.dateRepas(), request.typeRepas())
                .ifPresent(
                        m -> {
                            throw new ResourceAlreadyExistsException(
                                    "Un menu existe déjà pour cette date et ce type de repas.");
                        });
        MenuRepas menu = new MenuRepas();
        menu.setSejour(sejour);
        appliquerChamps(menu, request);
        appliquerReferences(menu, request, true);
        MenuRepas sauve = menuRepasRepository.save(menu);
        return mapToDto(rechargerAvecReferences(sauve.getId()));
    }

    @Override
    @Transactional
    public MenuRepasDto modifier(int sejourId, int menuId, SaveMenuRepasRequest request) {
        MenuRepas menu = getMenuDuSejour(sejourId, menuId);
        menuRepasRepository
                .findBySejour_IdAndDateRepasAndTypeRepas(sejourId, request.dateRepas(), request.typeRepas())
                .filter(autre -> !autre.getId().equals(menu.getId()))
                .ifPresent(
                        m -> {
                            throw new ResourceAlreadyExistsException(
                                    "Un menu existe déjà pour cette date et ce type de repas.");
                        });
        appliquerChamps(menu, request);
        appliquerReferences(menu, request, false);
        menuRepasRepository.save(menu);
        return mapToDto(rechargerAvecReferences(menu.getId()));
    }

    @Override
    @Transactional
    public void supprimer(int sejourId, int menuId) {
        MenuRepas menu = getMenuDuSejour(sejourId, menuId);
        menuRepasRepository.delete(menu);
    }

    private MenuRepas getMenuDuSejour(int sejourId, int menuId) {
        MenuRepas menu =
                menuRepasRepository
                        .findByIdFetchingReferences(menuId)
                        .orElseThrow(() -> new ResourceNotFoundException("Menu non trouvé avec l'ID: " + menuId));
        if (menu.getSejour().getId() != sejourId) {
            throw new ResourceNotFoundException("Menu non trouvé pour ce séjour.");
        }
        return menu;
    }

    private MenuRepas rechargerAvecReferences(Integer id) {
        return menuRepasRepository
                .findByIdFetchingReferences(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu non trouvé avec l'ID: " + id));
    }

    private static void appliquerChamps(MenuRepas menu, SaveMenuRepasRequest request) {
        menu.setDateRepas(request.dateRepas());
        menu.setTypeRepas(request.typeRepas());
        menu.setDetailPetitDejeunerOuGouter(emptyToNull(request.detailPetitDejeunerOuGouter()));
        menu.setEntree(emptyToNull(request.entree()));
        menu.setPlat(emptyToNull(request.plat()));
        menu.setFromageOuEntremet(emptyToNull(request.fromageOuEntremet()));
        menu.setDessert(emptyToNull(request.dessert()));
    }

    private void appliquerReferences(MenuRepas menu, SaveMenuRepasRequest request, boolean creation) {
        if (creation) {
            menu.getAllergenes().clear();
            menu.getRegimesEtPreferences().clear();
            if (request.allergeneIds() != null) {
                menu.getAllergenes().addAll(resoudreReferences(request.allergeneIds(), TypeReferenceAlimentaire.ALLERGENE));
            }
            if (request.regimePreferenceIds() != null) {
                menu.getRegimesEtPreferences()
                        .addAll(resoudreReferences(request.regimePreferenceIds(), TypeReferenceAlimentaire.REGIME_PREFERENCE));
            }
            return;
        }
        if (request.allergeneIds() != null) {
            menu.getAllergenes().clear();
            menu.getAllergenes().addAll(resoudreReferences(request.allergeneIds(), TypeReferenceAlimentaire.ALLERGENE));
        }
        if (request.regimePreferenceIds() != null) {
            menu.getRegimesEtPreferences().clear();
            menu.getRegimesEtPreferences()
                    .addAll(resoudreReferences(request.regimePreferenceIds(), TypeReferenceAlimentaire.REGIME_PREFERENCE));
        }
    }

    private Set<ReferenceAlimentaire> resoudreReferences(List<Integer> ids, TypeReferenceAlimentaire typeAttendu) {
        Set<ReferenceAlimentaire> ensemble = new HashSet<>();
        if (ids == null || ids.isEmpty()) {
            return ensemble;
        }
        LinkedHashSet<Integer> idsUniques = new LinkedHashSet<>();
        for (Integer refId : ids) {
            if (refId == null) {
                throw new IllegalArgumentException("Identifiant de référence alimentaire invalide.");
            }
            idsUniques.add(refId);
        }
        List<ReferenceAlimentaire> chargees = referenceAlimentaireRepository.findAllById(idsUniques);
        if (chargees.size() != idsUniques.size()) {
            Set<Integer> trouves =
                    chargees.stream().map(ReferenceAlimentaire::getId).collect(Collectors.toSet());
            for (Integer refId : idsUniques) {
                if (!trouves.contains(refId)) {
                    throw new ResourceNotFoundException("Référence alimentaire introuvable : " + refId);
                }
            }
        }
        Map<Integer, ReferenceAlimentaire> parId =
                chargees.stream().collect(Collectors.toMap(ReferenceAlimentaire::getId, r -> r));
        for (Integer refId : idsUniques) {
            ReferenceAlimentaire ref = parId.get(refId);
            if (ref.getType() != typeAttendu) {
                throw new IllegalArgumentException(
                        "La référence "
                                + refId
                                + " n'est pas du type attendu ("
                                + typeAttendu
                                + ").");
            }
            ensemble.add(ref);
        }
        return ensemble;
    }

    private static String emptyToNull(String value) {
        return value != null && value.isBlank() ? null : value;
    }

    private MenuRepasDto mapToDto(MenuRepas m) {
        Comparator<ReferenceAlimentaire> ordre =
                Comparator.comparing(ReferenceAlimentaire::getOrdre, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ReferenceAlimentaire::getId);
        List<ReferenceAlimentaireDto> allergenes =
                m.getAllergenes().stream().sorted(ordre).map(this::mapReferenceDto).collect(Collectors.toList());
        List<ReferenceAlimentaireDto> regimes =
                m.getRegimesEtPreferences().stream()
                        .sorted(ordre)
                        .map(this::mapReferenceDto)
                        .collect(Collectors.toList());
        return new MenuRepasDto(
                m.getId(),
                m.getSejour().getId(),
                m.getDateRepas(),
                m.getTypeRepas(),
                m.getDetailPetitDejeunerOuGouter(),
                m.getEntree(),
                m.getPlat(),
                m.getFromageOuEntremet(),
                m.getDessert(),
                allergenes,
                regimes);
    }

    private ReferenceAlimentaireDto mapReferenceDto(ReferenceAlimentaire r) {
        return new ReferenceAlimentaireDto(r.getId(), r.getType(), r.getLibelle(), r.getOrdre(), r.isActif());
    }
}
