package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.TypeActiviteLibellesParDefaut;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.TypeActivite;
import com.tarnof.enjoyrestapi.payload.request.SaveTypeActiviteRequest;
import com.tarnof.enjoyrestapi.payload.response.TypeActiviteDto;
import com.tarnof.enjoyrestapi.repositories.ActiviteRepository;
import com.tarnof.enjoyrestapi.repositories.TypeActiviteRepository;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TypeActiviteServiceImpl")
@SuppressWarnings("null")
class TypeActiviteServiceImplTest {

    private static final int SEJOUR_ID = 10;

    @Mock
    private TypeActiviteRepository typeActiviteRepository;
    @Mock
    private ActiviteRepository activiteRepository;
    @Mock
    private SejourVerificationService sejourVerificationService;

    private TypeActiviteServiceImpl service;
    private Sejour sejour;

    @BeforeEach
    void setUp() {
        service = new TypeActiviteServiceImpl(typeActiviteRepository, activiteRepository, sejourVerificationService);
        sejour = new Sejour();
        sejour.setId(SEJOUR_ID);
    }

    @Test
    @DisplayName("creerTypeActivite — type ajouté : predefini false, rattaché au séjour")
    void creer_shouldPersistPredefiniFalseAndSejour() {
        when(sejourVerificationService.verifierSejourExiste(SEJOUR_ID)).thenReturn(sejour);
        when(typeActiviteRepository.existsBySejourIdAndLibelleIgnoreCase(SEJOUR_ID, "Danse")).thenReturn(false);
        when(typeActiviteRepository.save(any(TypeActivite.class))).thenAnswer(inv -> {
            TypeActivite e = inv.getArgument(0);
            e.setId(99);
            return e;
        });

        TypeActiviteDto dto = service.creerTypeActivite(SEJOUR_ID, new SaveTypeActiviteRequest("Danse"));

        assertThat(dto.predefini()).isFalse();
        assertThat(dto.sejourId()).isEqualTo(SEJOUR_ID);
        ArgumentCaptor<TypeActivite> cap = ArgumentCaptor.forClass(TypeActivite.class);
        verify(typeActiviteRepository).save(cap.capture());
        assertThat(cap.getValue().isPredefini()).isFalse();
        assertThat(cap.getValue().getSejour()).isEqualTo(sejour);
    }

    @Test
    @DisplayName("modifierTypeActivite — type predefini : refus")
    void modifier_whenPredefini_shouldThrow() {
        TypeActivite sport = new TypeActivite();
        sport.setId(1);
        sport.setLibelle("Sport");
        sport.setPredefini(true);
        sport.setSejour(sejour);
        when(typeActiviteRepository.findByIdAndSejourId(1, SEJOUR_ID)).thenReturn(Optional.of(sport));

        assertThatThrownBy(
                        () -> service.modifierTypeActivite(SEJOUR_ID, 1, new SaveTypeActiviteRequest("Sport 2")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("types fournis par défaut");
    }

    @Test
    @DisplayName("modifierTypeActivite — type utilisateur : OK")
    void modifier_whenNotPredefini_shouldSucceed() {
        TypeActivite danse = new TypeActivite();
        danse.setId(2);
        danse.setLibelle("Danse");
        danse.setPredefini(false);
        danse.setSejour(sejour);
        when(typeActiviteRepository.findByIdAndSejourId(2, SEJOUR_ID)).thenReturn(Optional.of(danse));
        when(typeActiviteRepository.existsBySejourIdAndLibelleIgnoreCaseAndIdNot(SEJOUR_ID, "Danse moderne", 2))
                .thenReturn(false);
        when(typeActiviteRepository.save(any(TypeActivite.class))).thenAnswer(inv -> inv.getArgument(0));

        TypeActiviteDto dto =
                service.modifierTypeActivite(SEJOUR_ID, 2, new SaveTypeActiviteRequest("Danse moderne"));

        assertThat(dto.libelle()).isEqualTo("Danse moderne");
        assertThat(dto.predefini()).isFalse();
    }

    @Test
    @DisplayName("supprimerTypeActivite — type utilisateur sans activité : OK")
    void supprimer_whenNotPredefiniAndNoActivite_shouldDelete() {
        TypeActivite danse = new TypeActivite();
        danse.setId(2);
        danse.setLibelle("Danse");
        danse.setPredefini(false);
        danse.setSejour(sejour);
        when(typeActiviteRepository.findByIdAndSejourId(2, SEJOUR_ID)).thenReturn(Optional.of(danse));
        when(activiteRepository.countByTypeActivite_Id(2)).thenReturn(0L);

        service.supprimerTypeActivite(SEJOUR_ID, 2);

        verify(typeActiviteRepository).delete(danse);
    }

    @Test
    @DisplayName("supprimerTypeActivite — type predefini : refus")
    void supprimer_whenPredefini_shouldThrow() {
        TypeActivite sport = new TypeActivite();
        sport.setId(1);
        sport.setPredefini(true);
        sport.setSejour(sejour);
        when(typeActiviteRepository.findByIdAndSejourId(1, SEJOUR_ID)).thenReturn(Optional.of(sport));

        assertThatThrownBy(() -> service.supprimerTypeActivite(SEJOUR_ID, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("types fournis par défaut");
    }

    @Test
    @DisplayName("assurerTypesParDefautPourSejour — insère un libellé manquant")
    void assurerTypesParDefaut_shouldInsertWhenAbsent() {
        when(sejourVerificationService.verifierSejourExiste(SEJOUR_ID)).thenReturn(sejour);
        when(typeActiviteRepository.findBySejourIdAndLibelleIgnoreCase(eq(SEJOUR_ID), any()))
                .thenReturn(Optional.empty());
        when(typeActiviteRepository.save(any(TypeActivite.class))).thenAnswer(inv -> inv.getArgument(0));

        service.assurerTypesParDefautPourSejour(SEJOUR_ID);

        verify(typeActiviteRepository, times(TypeActiviteLibellesParDefaut.LIBELLES.size()))
                .save(any(TypeActivite.class));
    }
}
