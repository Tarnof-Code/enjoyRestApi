package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.*;
import com.tarnof.enjoyrestapi.enums.HistoriqueModificationAction;
import com.tarnof.enjoyrestapi.enums.PlanningLigneLibelleSource;
import com.tarnof.enjoyrestapi.enums.UsageLieu;
import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.*;
import com.tarnof.enjoyrestapi.repositories.*;
import com.tarnof.enjoyrestapi.services.HistoriqueModificationService;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
import org.springframework.security.access.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    @Mock
    private HistoriqueModificationService historiqueModificationService;

    private PlanningGrilleServiceImpl service;

    private Sejour sejour;
    private Utilisateur appelantAdmin;

    @BeforeEach
    void setUp() {
        service = new PlanningGrilleServiceImpl(
                planningGrilleRepository,
                planningLigneRepository,
                planningCelluleRepository,
                new SejourVerificationService(sejourRepository, utilisateurRepository),
                momentRepository,
                horaireRepository,
                groupeRepository,
                lieuRepository,
                sejourEquipeRepository,
                utilisateurRepository,
                historiqueModificationService);
        sejour = new Sejour();
        sejour.setId(1);
        appelantAdmin = Utilisateur.builder()
                .id(99)
                .tokenId("appelant-token")
                .role(Role.ADMIN)
                .build();
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

        assertThatThrownBy(() -> service.remplacerCellules(1, 10, 20, req, "appelant-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("équipe");

        verify(planningCelluleRepository, never()).save(any());
    }

    @Test
    @DisplayName("getGrille - 404 si planning absent pour le séjour")
    void getGrille_notFound() {
        when(utilisateurRepository.findByTokenId("appelant-token")).thenReturn(Optional.of(appelantAdmin));
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(planningGrilleRepository.findByIdAndSejour_Id(99, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getGrille(1, 99, "appelant-token"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("listerGrilles - membre d'équipe : OK (lecture autorisée)")
    void listerGrilles_membreEquipe_ok() {
        Utilisateur membre = Utilisateur.builder()
                .id(7)
                .tokenId("membre-token")
                .role(Role.BASIC_USER)
                .build();
        SejourEquipe equipeRole = new SejourEquipe();
        equipeRole.setUtilisateur(membre);
        sejour.setEquipeRoles(List.of(equipeRole));

        when(utilisateurRepository.findByTokenId("membre-token")).thenReturn(Optional.of(membre));
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(planningGrilleRepository.findBySejour_IdOrderByMiseAJourDesc(1)).thenReturn(List.of());

        var grilles = service.listerGrilles(1, "membre-token");

        assertThat(grilles).isEmpty();
        verify(planningGrilleRepository).findBySejour_IdOrderByMiseAJourDesc(1);
    }

    @Test
    @DisplayName("listerGrilles - utilisateur hors séjour : AccessDeniedException")
    void listerGrilles_horsSejour_accessDenied() {
        Utilisateur intrus = Utilisateur.builder()
                .id(42)
                .tokenId("intrus-token")
                .role(Role.BASIC_USER)
                .build();
        sejour.setEquipeRoles(List.of());

        when(utilisateurRepository.findByTokenId("intrus-token")).thenReturn(Optional.of(intrus));
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));

        assertThatThrownBy(() -> service.listerGrilles(1, "intrus-token"))
                .isInstanceOf(AccessDeniedException.class);

        verify(planningGrilleRepository, never()).findBySejour_IdOrderByMiseAJourDesc(any(Integer.class));
    }

    @Test
    @DisplayName("creerLigne - refus si le lieu est seulement lieu d'activité")
    void creerLigne_lieuActiviteSeul_devraitEchouer() {
        PlanningGrille grille = new PlanningGrille();
        grille.setId(10);
        grille.setSejour(sejour);
        grille.setSourceLibelleLignes(PlanningLigneLibelleSource.LIEU);
        Lieu lieu = new Lieu();
        lieu.setId(50);
        lieu.setSejour(sejour);
        lieu.setUsages(EnumSet.of(UsageLieu.ACTIVITE));

        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(planningGrilleRepository.findByIdAndSejour_Id(10, 1)).thenReturn(Optional.of(grille));
        when(lieuRepository.findByIdAndSejourId(50, 1)).thenReturn(Optional.of(lieu));

        var req = new SavePlanningLigneRequest(0, null, null, null, null, null, 50, null);

        assertThatThrownBy(() -> service.creerLigne(1, 10, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("surveillance");

        verify(planningLigneRepository, never()).save(any());
    }

    @Test
    @DisplayName("creerLigne - lieu de rassemblement accepté")
    void creerLigne_lieuRassemblement_ok() {
        PlanningGrille grille = new PlanningGrille();
        grille.setId(11);
        grille.setSejour(sejour);
        grille.setSourceLibelleLignes(PlanningLigneLibelleSource.LIEU);
        Lieu lieu = new Lieu();
        lieu.setId(51);
        lieu.setSejour(sejour);
        lieu.setUsages(EnumSet.of(UsageLieu.RASSEMBLEMENT));

        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(planningGrilleRepository.findByIdAndSejour_Id(11, 1)).thenReturn(Optional.of(grille));
        when(lieuRepository.findByIdAndSejourId(51, 1)).thenReturn(Optional.of(lieu));
        when(planningLigneRepository.save(any(PlanningLigne.class)))
                .thenAnswer(
                        inv -> {
                            PlanningLigne l = inv.getArgument(0);
                            l.setId(999);
                            return l;
                        });

        var dto =
                service.creerLigne(
                        1, 11, new SavePlanningLigneRequest(2, null, null, null, null, null, 51, null));

        assertThat(dto.libelleLieuId()).isEqualTo(51);
        verify(planningLigneRepository).save(any(PlanningLigne.class));
    }

    @Test
    @DisplayName("modifierMaPresenceSurCelluleMembreEquipe - refuse si source contenu cellules ≠ MEMBRE_EQUIPE")
    void modifierMaPresence_sourcePasMembreEquipe_accessDenied() {
        Utilisateur anim = Utilisateur.builder().id(5).tokenId("anim-token").role(Role.BASIC_USER).build();
        SejourEquipe equipeRole = new SejourEquipe();
        equipeRole.setUtilisateur(anim);
        sejour.setEquipeRoles(List.of(equipeRole));

        PlanningGrille grille = new PlanningGrille();
        grille.setId(10);
        grille.setSejour(sejour);
        grille.setSourceContenuCellules(PlanningLigneLibelleSource.SAISIE_LIBRE);

        when(utilisateurRepository.findByTokenId("anim-token")).thenReturn(Optional.of(anim));
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(planningGrilleRepository.findByIdAndSejour_Id(10, 1)).thenReturn(Optional.of(grille));

        assertThatThrownBy(
                        () ->
                                service.modifierMaPresenceSurCelluleMembreEquipe(
                                        1, 10, 20, LocalDate.of(2026, 8, 1), true, "anim-token"))
                .isInstanceOf(AccessDeniedException.class);

        verify(planningCelluleRepository, never()).save(any());
        verify(planningCelluleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("modifierMaPresenceSurCelluleMembreEquipe - inscription crée une cellule et trace l'historique")
    void modifierMaPresence_inscription_creeEtHistorique() {
        Utilisateur anim = Utilisateur.builder().id(5).tokenId("anim-token").role(Role.BASIC_USER).build();
        SejourEquipe equipeRole = new SejourEquipe();
        equipeRole.setUtilisateur(anim);
        sejour.setEquipeRoles(List.of(equipeRole));

        PlanningGrille grille = new PlanningGrille();
        grille.setId(10);
        grille.setSejour(sejour);
        grille.setSourceContenuCellules(PlanningLigneLibelleSource.MEMBRE_EQUIPE);

        PlanningLigne ligne = new PlanningLigne();
        ligne.setId(20);
        ligne.setGrille(grille);

        LocalDate jour = LocalDate.of(2026, 8, 2);

        when(utilisateurRepository.findByTokenId("anim-token")).thenReturn(Optional.of(anim));
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(planningGrilleRepository.findByIdAndSejour_Id(10, 1)).thenReturn(Optional.of(grille));
        when(planningLigneRepository.findByIdAndGrille_Id(20, 10)).thenReturn(Optional.of(ligne));
        when(planningCelluleRepository.findByLigne_IdAndJour(20, jour)).thenReturn(Optional.empty());
        when(sejourEquipeRepository.existsBySejour_IdAndUtilisateur_Id(1, 5)).thenReturn(true);
        when(planningCelluleRepository.save(any(PlanningCellule.class)))
                .thenAnswer(
                        inv -> {
                            PlanningCellule c = inv.getArgument(0);
                            c.setId(400);
                            return c;
                        });

        assertThat(service.modifierMaPresenceSurCelluleMembreEquipe(1, 10, 20, jour, true, "anim-token"))
                .hasValueSatisfying(
                        dto -> {
                            assertThat(dto.id()).isEqualTo(400);
                            assertThat(dto.membreTokenIds()).containsExactly("anim-token");
                        });

        verify(historiqueModificationService)
                .enregistrerPlanningCellule(
                        eq("anim-token"),
                        eq(HistoriqueModificationAction.CREATION),
                        eq(20),
                        eq(jour),
                        eq(400),
                        eq(null),
                        any());
    }

    @Test
    @DisplayName("modifierMaPresenceSurCelluleMembreEquipe - désinscription dernier animateur supprime la cellule")
    void modifierMaPresence_desinscriptionDernier_supprimeEtHistorique() {
        Utilisateur anim = Utilisateur.builder().id(5).tokenId("anim-token").role(Role.BASIC_USER).build();
        SejourEquipe equipeRole = new SejourEquipe();
        equipeRole.setUtilisateur(anim);
        sejour.setEquipeRoles(List.of(equipeRole));

        PlanningGrille grille = new PlanningGrille();
        grille.setId(10);
        grille.setSejour(sejour);
        grille.setSourceContenuCellules(PlanningLigneLibelleSource.MEMBRE_EQUIPE);

        PlanningLigne ligne = new PlanningLigne();
        ligne.setId(20);
        ligne.setGrille(grille);

        LocalDate jour = LocalDate.of(2026, 8, 3);

        PlanningCellule cellule = new PlanningCellule();
        cellule.setId(401);
        cellule.setLigne(ligne);
        cellule.setJour(jour);
        cellule.setAnimateursAssignes(new HashSet<>(Set.of(anim)));

        when(utilisateurRepository.findByTokenId("anim-token")).thenReturn(Optional.of(anim));
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(planningGrilleRepository.findByIdAndSejour_Id(10, 1)).thenReturn(Optional.of(grille));
        when(planningLigneRepository.findByIdAndGrille_Id(20, 10)).thenReturn(Optional.of(ligne));
        when(planningCelluleRepository.findByLigne_IdAndJour(20, jour)).thenReturn(Optional.of(cellule));
        when(sejourEquipeRepository.existsBySejour_IdAndUtilisateur_Id(1, 5)).thenReturn(true);

        assertThat(service.modifierMaPresenceSurCelluleMembreEquipe(1, 10, 20, jour, false, "anim-token"))
                .isEmpty();

        verify(planningCelluleRepository).delete(cellule);
        verify(historiqueModificationService)
                .enregistrerPlanningCellule(
                        eq("anim-token"),
                        eq(HistoriqueModificationAction.SUPPRESSION),
                        eq(20),
                        eq(jour),
                        eq(401),
                        any(),
                        eq(null));
    }
}
