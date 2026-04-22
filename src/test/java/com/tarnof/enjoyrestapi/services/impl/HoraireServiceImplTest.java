package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.Horaire;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.SaveHoraireRequest;
import com.tarnof.enjoyrestapi.repositories.HoraireRepository;
import com.tarnof.enjoyrestapi.repositories.SejourRepository;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HoraireServiceImpl")
@SuppressWarnings("null")
class HoraireServiceImplTest {

    @Mock
    private HoraireRepository horaireRepository;
    @Mock
    private SejourRepository sejourRepository;

    private HoraireServiceImpl horaireService;

    private Sejour sejour;

    @BeforeEach
    void setUp() {
        horaireService = new HoraireServiceImpl(horaireRepository, new SejourVerificationService(sejourRepository));
        sejour = new Sejour();
        sejour.setId(1);
    }

    @Test
    @DisplayName("creerHoraire - refuse un doublon de libellé (même séjour, casse différente)")
    void creerHoraire_whenDuplicateLibelle_shouldThrow409() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(horaireRepository.existsBySejourIdAndLibelleIgnoreCase(1, "7h00")).thenReturn(true);

        assertThatThrownBy(
                        () -> horaireService.creerHoraire(1, new SaveHoraireRequest("  7h00  ")))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("déjà");

        verify(horaireRepository, never()).save(any());
    }

    @Test
    @DisplayName("creerHoraire - persiste avec libellé trimé")
    void creerHoraire_success_trimsLibelle() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(horaireRepository.existsBySejourIdAndLibelleIgnoreCase(1, "18h30")).thenReturn(false);
        Horaire saved = new Horaire();
        saved.setId(10);
        saved.setLibelle("18h30");
        saved.setSejour(sejour);
        when(horaireRepository.save(any(Horaire.class))).thenReturn(saved);

        var dto = horaireService.creerHoraire(1, new SaveHoraireRequest("  18h30  "));

        assertThat(dto.libelle()).isEqualTo("18h30");
        assertThat(dto.id()).isEqualTo(10);
    }

    @Test
    @DisplayName("modifierHoraire - refuse si le nouveau libellé existe déjà sur un autre horaire")
    void modifierHoraire_whenLibelleTakenByOther_shouldThrow409() {
        Horaire horaire = new Horaire();
        horaire.setId(5);
        horaire.setLibelle("6h00");
        horaire.setSejour(sejour);
        when(horaireRepository.findByIdAndSejourId(5, 1)).thenReturn(Optional.of(horaire));
        when(horaireRepository.existsBySejourIdAndLibelleIgnoreCaseAndIdNot(1, "9h15", 5)).thenReturn(true);

        assertThatThrownBy(() -> horaireService.modifierHoraire(1, 5, new SaveHoraireRequest("9h15")))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(horaireRepository, never()).save(any());
    }

    @Test
    @DisplayName("modifierHoraire - autorise de conserver le même libellé (pas de doublon avec soi-même)")
    void modifierHoraire_keepsSameLibelle_shouldSucceed() {
        Horaire horaire = new Horaire();
        horaire.setId(5);
        horaire.setLibelle("12h00");
        horaire.setSejour(sejour);
        when(horaireRepository.findByIdAndSejourId(5, 1)).thenReturn(Optional.of(horaire));
        when(horaireRepository.existsBySejourIdAndLibelleIgnoreCaseAndIdNot(1, "12h00", 5)).thenReturn(false);
        when(horaireRepository.save(any(Horaire.class))).thenAnswer(inv -> inv.getArgument(0));

        horaireService.modifierHoraire(1, 5, new SaveHoraireRequest("12h00"));

        verify(horaireRepository).save(horaire);
        assertThat(horaire.getLibelle()).isEqualTo("12h00");
    }

    @Test
    @DisplayName("listerHorairesDuSejour - 404 si séjour absent")
    void lister_whenSejourMissing_shouldThrow404() {
        when(sejourRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> horaireService.listerHorairesDuSejour(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("listerHorairesDuSejour - liste vide OK")
    void lister_whenEmpty_shouldReturnEmpty() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(horaireRepository.findBySejourIdOrderByIdAsc(1)).thenReturn(List.of());

        assertThat(horaireService.listerHorairesDuSejour(1)).isEmpty();
    }

    @Test
    @DisplayName("getHoraire - 404 si mauvais séjour")
    void get_whenWrongSejour_shouldThrow404() {
        when(horaireRepository.findByIdAndSejourId(3, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> horaireService.getHoraire(1, 3))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Horaire non trouvé");
    }
}
