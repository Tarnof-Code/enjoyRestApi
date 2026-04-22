package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.Horaire;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.SaveHoraireRequest;
import com.tarnof.enjoyrestapi.payload.response.HoraireDto;
import com.tarnof.enjoyrestapi.repositories.HoraireRepository;
import com.tarnof.enjoyrestapi.services.HoraireService;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class HoraireServiceImpl implements HoraireService {

    private final HoraireRepository horaireRepository;
    private final SejourVerificationService sejourVerificationService;

    public HoraireServiceImpl(
            HoraireRepository horaireRepository, SejourVerificationService sejourVerificationService) {
        this.horaireRepository = horaireRepository;
        this.sejourVerificationService = sejourVerificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HoraireDto> listerHorairesDuSejour(int sejourId) {
        sejourVerificationService.verifierSejourExiste(sejourId);
        return horaireRepository.findBySejourIdOrderByIdAsc(sejourId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HoraireDto getHoraire(int sejourId, int horaireId) {
        return mapToDto(getHoraireEtVerifierSejour(sejourId, horaireId));
    }

    @Override
    @Transactional
    public HoraireDto creerHoraire(int sejourId, SaveHoraireRequest request) {
        Sejour sejour = sejourVerificationService.verifierSejourExiste(sejourId);
        String libelle = normaliserLibelle(request.libelle());
        verifierLibelleHoraireUniquePourSejour(sejourId, libelle, null);
        Horaire horaire = new Horaire();
        horaire.setLibelle(libelle);
        horaire.setSejour(sejour);
        return mapToDto(horaireRepository.save(horaire));
    }

    @Override
    @Transactional
    public HoraireDto modifierHoraire(int sejourId, int horaireId, SaveHoraireRequest request) {
        Horaire horaire = getHoraireEtVerifierSejour(sejourId, horaireId);
        String libelle = normaliserLibelle(request.libelle());
        verifierLibelleHoraireUniquePourSejour(sejourId, libelle, horaireId);
        horaire.setLibelle(libelle);
        return mapToDto(horaireRepository.save(horaire));
    }

    @Override
    @Transactional
    public void supprimerHoraire(int sejourId, int horaireId) {
        Horaire horaire = getHoraireEtVerifierSejour(sejourId, horaireId);
        horaireRepository.delete(horaire);
    }

    private Horaire getHoraireEtVerifierSejour(int sejourId, int horaireId) {
        return horaireRepository
                .findByIdAndSejourId(horaireId, sejourId)
                .orElseThrow(() -> new ResourceNotFoundException("Horaire non trouvé avec l'ID: " + horaireId));
    }

    private static String normaliserLibelle(String libelle) {
        return libelle == null ? "" : libelle.trim();
    }

    private void verifierLibelleHoraireUniquePourSejour(int sejourId, String libelle, Integer excludeHoraireId) {
        boolean doublon = excludeHoraireId == null
                ? horaireRepository.existsBySejourIdAndLibelleIgnoreCase(sejourId, libelle)
                : horaireRepository.existsBySejourIdAndLibelleIgnoreCaseAndIdNot(sejourId, libelle, excludeHoraireId);
        if (doublon) {
            throw new ResourceAlreadyExistsException("Un horaire avec ce libellé existe déjà pour ce séjour");
        }
    }

    private HoraireDto mapToDto(Horaire horaire) {
        return new HoraireDto(horaire.getId(), horaire.getLibelle(), horaire.getSejour().getId());
    }
}
