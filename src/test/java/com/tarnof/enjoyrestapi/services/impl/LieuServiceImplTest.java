package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.Lieu;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.enums.EmplacementLieu;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.SaveLieuRequest;
import com.tarnof.enjoyrestapi.repositories.LieuRepository;
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
@DisplayName("LieuServiceImpl")
@SuppressWarnings("null")
class LieuServiceImplTest {

    @Mock
    private LieuRepository lieuRepository;
    @Mock
    private SejourRepository sejourRepository;

    private LieuServiceImpl lieuService;

    private Sejour sejour;

    @BeforeEach
    void setUp() {
        lieuService = new LieuServiceImpl(lieuRepository, new SejourVerificationService(sejourRepository));
        sejour = new Sejour();
        sejour.setId(1);
    }

    @Test
    @DisplayName("creerLieu - refuse un doublon de nom (même séjour, casse différente)")
    void creerLieu_whenDuplicateNom_shouldThrow409() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(lieuRepository.existsBySejourIdAndNomIgnoreCase(1, "salle a")).thenReturn(true);

        assertThatThrownBy(() -> lieuService.creerLieu(1,
                new SaveLieuRequest("  salle a  ", EmplacementLieu.INTERIEUR, null, false, null)))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("déjà");

        verify(lieuRepository, never()).save(any());
    }

    @Test
    @DisplayName("creerLieu - persiste avec nom trimé")
    void creerLieu_success_trimsNom() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(lieuRepository.existsBySejourIdAndNomIgnoreCase(1, "Terrain")).thenReturn(false);
        Lieu saved = new Lieu();
        saved.setId(10);
        saved.setNom("Terrain");
        saved.setEmplacement(EmplacementLieu.EXTERIEUR);
        saved.setNombreMax(null);
        saved.setSejour(sejour);
        when(lieuRepository.save(any(Lieu.class))).thenReturn(saved);

        var dto = lieuService.creerLieu(1,
                new SaveLieuRequest("  Terrain  ", EmplacementLieu.EXTERIEUR, null, false, null));

        assertThat(dto.nom()).isEqualTo("Terrain");
        assertThat(dto.id()).isEqualTo(10);
    }

    @Test
    @DisplayName("modifierLieu - refuse si le nouveau nom existe déjà sur un autre lieu")
    void modifierLieu_whenNomTakenByOther_shouldThrow409() {
        Lieu lieu = new Lieu();
        lieu.setId(5);
        lieu.setNom("Ancien");
        lieu.setEmplacement(EmplacementLieu.INTERIEUR);
        lieu.setSejour(sejour);
        when(lieuRepository.findById(5)).thenReturn(Optional.of(lieu));
        when(lieuRepository.existsBySejourIdAndNomIgnoreCaseAndIdNot(1, "Autre", 5)).thenReturn(true);

        assertThatThrownBy(() -> lieuService.modifierLieu(1, 5,
                new SaveLieuRequest("Autre", EmplacementLieu.EXTERIEUR, 20, false, null)))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(lieuRepository, never()).save(any());
    }

    @Test
    @DisplayName("modifierLieu - autorise de garder le même nom (casse différente)")
    void modifierLieu_sameNomDifferentCase_shouldSucceed() {
        Lieu lieu = new Lieu();
        lieu.setId(5);
        lieu.setNom("Salle");
        lieu.setEmplacement(EmplacementLieu.INTERIEUR);
        lieu.setSejour(sejour);
        when(lieuRepository.findById(5)).thenReturn(Optional.of(lieu));
        when(lieuRepository.existsBySejourIdAndNomIgnoreCaseAndIdNot(1, "SALLE", 5)).thenReturn(false);
        when(lieuRepository.save(any(Lieu.class))).thenAnswer(inv -> inv.getArgument(0));

        lieuService.modifierLieu(1, 5,
                new SaveLieuRequest("SALLE", EmplacementLieu.HORS_CENTRE, null, false, null));

        verify(lieuRepository).save(lieu);
        assertThat(lieu.getNom()).isEqualTo("SALLE");
        assertThat(lieu.getEmplacement()).isEqualTo(EmplacementLieu.HORS_CENTRE);
    }

    @Test
    @DisplayName("listerLieuxDuSejour - 404 si séjour absent")
    void lister_whenSejourMissing_shouldThrow404() {
        when(sejourRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lieuService.listerLieuxDuSejour(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("creerLieu - partage activé sans nombre max")
    void creerLieu_whenPartageSansMax_shouldThrow400() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));

        assertThatThrownBy(() -> lieuService.creerLieu(1,
                new SaveLieuRequest("Salle", EmplacementLieu.INTERIEUR, null, true, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("simultanées");

        verify(lieuRepository, never()).save(any());
    }

    @Test
    @DisplayName("creerLieu - partage avec max 2 OK")
    void creerLieu_whenPartageAvecMax2_shouldSucceed() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(lieuRepository.existsBySejourIdAndNomIgnoreCase(1, "Grande salle")).thenReturn(false);
        when(lieuRepository.save(any(Lieu.class))).thenAnswer(inv -> {
            Lieu l = inv.getArgument(0);
            l.setId(9);
            return l;
        });

        var dto = lieuService.creerLieu(1,
                new SaveLieuRequest("Grande salle", EmplacementLieu.INTERIEUR, null, true, 2));

        assertThat(dto.partageableEntreAnimateurs()).isTrue();
        assertThat(dto.nombreMaxActivitesSimultanees()).isEqualTo(2);
    }

    @Test
    @DisplayName("listerLieuxDuSejour - liste vide OK")
    void lister_whenEmpty_shouldReturnEmpty() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(lieuRepository.findBySejourId(1)).thenReturn(List.of());

        assertThat(lieuService.listerLieuxDuSejour(1)).isEmpty();
    }
}
