package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.*;
import com.tarnof.enjoyrestapi.enums.PlanningLigneLibelleSource;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.*;
import com.tarnof.enjoyrestapi.repositories.*;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlanningGrilleServiceImpl")
@SuppressWarnings("null")
class PlanningGrilleServiceImplTest {

    @Mock
    private PlanningGrilleRepository planningGrilleRepository;
    @Mock
    private PlanningLigneRepository planningLigneRepository;
    @Mock
    private PlanningCelluleRepository planningCelluleRepository;
    @Mock
    private SejourRepository sejourRepository;
    @Mock
    private MomentRepository momentRepository;
    @Mock
    private HoraireRepository horaireRepository;
    @Mock
    private GroupeRepository groupeRepository;
    @Mock
    private LieuRepository lieuRepository;
    @Mock
    private SejourEquipeRepository sejourEquipeRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;

    private PlanningGrilleServiceImpl service;

    private Sejour sejour;

    @BeforeEach
    void setUp() {
        service = new PlanningGrilleServiceImpl(
                planningGrilleRepository,
                planningLigneRepository,
                planningCelluleRepository,
                new SejourVerificationService(sejourRepository),
                momentRepository,
                horaireRepository,
                groupeRepository,
                lieuRepository,
                sejourEquipeRepository,
                utilisateurRepository);
        sejour = new Sejour();
        sejour.setId(1);
    }

    @Test
    @DisplayName("creerGrille - persiste titre et consigne")
    void creerGrille_success() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(planningGrilleRepository.save(any(PlanningGrille.class)))
                .thenAnswer(
                        inv -> {
                            PlanningGrille g = inv.getArgument(0);
                            g.setId(100);
                            return g;
                        });
        when(planningLigneRepository.findByGrille_Id(100)).thenReturn(List.of());

        var dto =
                service.creerGrille(
                        1,
                        new SavePlanningGrilleRequest(
                                "Congés",
                                "  note  ",
                                PlanningLigneLibelleSource.HORAIRE,
                                PlanningLigneLibelleSource.HORAIRE));

        assertThat(dto.titre()).isEqualTo("Congés");
        assertThat(dto.consigneGlobale()).isEqualTo("note");
        assertThat(dto.sourceLibelleLignes()).isEqualTo(PlanningLigneLibelleSource.HORAIRE);
        assertThat(dto.sourceContenuCellules()).isEqualTo(PlanningLigneLibelleSource.HORAIRE);
        verify(planningGrilleRepository).save(any(PlanningGrille.class));
    }

    @Test
    @DisplayName("creerGrille - sourceLibelleLignes null reste absent (pas de défaut SAISIE_LIBRE)")
    void creerGrille_sourceLibelleLignesNull_resteNull() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(planningGrilleRepository.save(any(PlanningGrille.class)))
                .thenAnswer(
                        inv -> {
                            PlanningGrille g = inv.getArgument(0);
                            g.setId(101);
                            return g;
                        });
        when(planningLigneRepository.findByGrille_Id(101)).thenReturn(List.of());

        var dto =
                service.creerGrille(
                        1, new SavePlanningGrilleRequest("Sans source", null, null, null));

        assertThat(dto.sourceLibelleLignes()).isNull();
        assertThat(dto.sourceContenuCellules()).isEqualTo(PlanningLigneLibelleSource.SAISIE_LIBRE);
    }

    @Test
    @DisplayName("creerGrille - accepte MEMBRE_EQUIPE pour sourceLibelleLignes")
    void creerGrille_membreEquipeCommeLibelleLignes_ok() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(planningGrilleRepository.save(any(PlanningGrille.class)))
                .thenAnswer(
                        inv -> {
                            PlanningGrille g = inv.getArgument(0);
                            g.setId(102);
                            return g;
                        });
        when(planningLigneRepository.findByGrille_Id(102)).thenReturn(List.of());

        var dto =
                service.creerGrille(
                        1,
                        new SavePlanningGrilleRequest(
                                "Par animateur", null, PlanningLigneLibelleSource.MEMBRE_EQUIPE, null));

        assertThat(dto.sourceLibelleLignes()).isEqualTo(PlanningLigneLibelleSource.MEMBRE_EQUIPE);
        assertThat(dto.sourceContenuCellules()).isEqualTo(PlanningLigneLibelleSource.SAISIE_LIBRE);
        verify(planningGrilleRepository).save(any(PlanningGrille.class));
    }

    @Test
    @DisplayName("remplacerCellules - refuse un animateur hors équipe du séjour")
    void remplacerCellules_horsEquipe() {
        PlanningGrille grille = new PlanningGrille();
        grille.setId(10);
        grille.setSejour(sejour);
        grille.setSourceLibelleLignes(PlanningLigneLibelleSource.SAISIE_LIBRE);
        grille.setSourceContenuCellules(PlanningLigneLibelleSource.SAISIE_LIBRE);
        grille.setTitre("G");
        PlanningLigne ligne = new PlanningLigne();
        ligne.setId(20);
        ligne.setGrille(grille);
        ligne.setOrdre(0);
        ligne.setLibelleSaisieLibre("L");

        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(planningGrilleRepository.findByIdAndSejour_Id(10, 1)).thenReturn(Optional.of(grille));
        when(planningLigneRepository.findByIdAndGrille_Id(20, 10)).thenReturn(Optional.of(ligne));
        Utilisateur horsEquipe = new Utilisateur();
        horsEquipe.setId(5);
        horsEquipe.setTokenId("tok-hors-equipe");
        when(utilisateurRepository.findByTokenId("tok-hors-equipe")).thenReturn(Optional.of(horsEquipe));
        when(sejourEquipeRepository.existsBySejour_IdAndUtilisateur_Id(1, 5)).thenReturn(false);

        var req =
                new UpsertPlanningCellulesRequest(
                        List.of(
                                new PlanningCellulePayload(
                                        LocalDate.of(2026, 7, 11),
                                        List.of("tok-hors-equipe"),
                                        null,
                                        null,
                                        null,
                                        null,
                                        null)));

        assertThatThrownBy(() -> service.remplacerCellules(1, 10, 20, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("équipe");

        verify(planningCelluleRepository, never()).save(any());
    }

    @Test
    @DisplayName("getGrille - 404 si planning absent pour le séjour")
    void getGrille_notFound() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(planningGrilleRepository.findByIdAndSejour_Id(99, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getGrille(1, 99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
