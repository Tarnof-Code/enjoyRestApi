package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.Activite;
import com.tarnof.enjoyrestapi.entities.Groupe;
import com.tarnof.enjoyrestapi.entities.Lieu;
import com.tarnof.enjoyrestapi.entities.Moment;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.SejourEquipeId;
import com.tarnof.enjoyrestapi.entities.TypeActivite;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.EmplacementLieu;
import com.tarnof.enjoyrestapi.enums.TypeGroupe;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.CreateActiviteRequest;
import com.tarnof.enjoyrestapi.payload.request.UpdateActiviteRequest;
import com.tarnof.enjoyrestapi.payload.response.ActiviteDto;
import com.tarnof.enjoyrestapi.repositories.ActiviteRepository;
import com.tarnof.enjoyrestapi.repositories.GroupeRepository;
import com.tarnof.enjoyrestapi.repositories.LieuRepository;
import com.tarnof.enjoyrestapi.repositories.MomentRepository;
import com.tarnof.enjoyrestapi.repositories.SejourEquipeRepository;
import com.tarnof.enjoyrestapi.repositories.SejourRepository;
import com.tarnof.enjoyrestapi.repositories.TypeActiviteRepository;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour ActiviteServiceImpl")
@SuppressWarnings("null")
class ActiviteServiceImplTest {

    private static final int MOMENT_ID = 55;
    private static final int TYPE_ACTIVITE_ID = 88;

    @Mock
    private ActiviteRepository activiteRepository;
    @Mock
    private SejourRepository sejourRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private SejourEquipeRepository sejourEquipeRepository;
    @Mock
    private GroupeRepository groupeRepository;
    @Mock
    private LieuRepository lieuRepository;
    @Mock
    private MomentRepository momentRepository;
    @Mock
    private TypeActiviteRepository typeActiviteRepository;

    private ActiviteServiceImpl activiteService;
    private Sejour sejour;
    private Utilisateur membre;
    private Moment momentMatin;
    private TypeActivite typeActiviteSport;

    @BeforeEach
    void setUp() {
        activiteService = new ActiviteServiceImpl(
                activiteRepository,
                new SejourVerificationService(sejourRepository),
                utilisateurRepository,
                sejourEquipeRepository,
                groupeRepository,
                lieuRepository,
                momentRepository,
                typeActiviteRepository);
        sejour = Sejour.builder()
                .id(1)
                .nom("Colo")
                .dateDebut(Date.valueOf(LocalDate.of(2026, 7, 1)))
                .dateFin(Date.valueOf(LocalDate.of(2026, 7, 15)))
                .build();
        membre = Utilisateur.builder()
                .id(10)
                .tokenId("mem-1")
                .nom("Dupont")
                .prenom("Jean")
                .build();
        momentMatin = new Moment();
        momentMatin.setId(MOMENT_ID);
        momentMatin.setNom("Matin");
        momentMatin.setSejour(sejour);
        typeActiviteSport = new TypeActivite();
        typeActiviteSport.setId(TYPE_ACTIVITE_ID);
        typeActiviteSport.setLibelle("Sport");
        typeActiviteSport.setPredefini(false);
        typeActiviteSport.setSejour(sejour);
    }

    private void givenMomentsAuMoinsUnPourSejour1() {
        when(momentRepository.countBySejourId(1)).thenReturn(1L);
        when(momentRepository.findByIdAndSejourId(MOMENT_ID, 1)).thenReturn(Optional.of(momentMatin));
    }

    private void givenTypeActivitePourSejour1() {
        when(typeActiviteRepository.findByIdAndSejourId(TYPE_ACTIVITE_ID, 1)).thenReturn(Optional.of(typeActiviteSport));
    }

    @Test
    @DisplayName("listerActivitesDuSejour - succès")
    void lister_ShouldReturnDtos() {
        Activite a = activitePersistee(3, List.of(membre));
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(activiteRepository.findBySejourIdOrderByDateAscIdAsc(1)).thenReturn(List.of(a));

        List<ActiviteDto> result = activiteService.listerActivitesDuSejour(1);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(3);
        assertThat(result.getFirst().membres()).hasSize(1);
        assertThat(result.getFirst().membres().getFirst().tokenId()).isEqualTo("mem-1");
    }

    @Test
    @DisplayName("listerActivitesDuSejour - séjour absent")
    void lister_whenSejourMissing_shouldThrow() {
        when(sejourRepository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> activiteService.listerActivitesDuSejour(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Séjour non trouvé");
    }

    @Test
    @DisplayName("creerActivite - succès avec plusieurs groupes")
    void creer_shouldPersist() {
        Groupe g5 = Groupe.builder().id(5).nom("G5").typeGroupe(TypeGroupe.THEMATIQUE).sejour(sejour).build();
        Groupe g6 = Groupe.builder().id(6).nom("G6").typeGroupe(TypeGroupe.THEMATIQUE).sejour(sejour).build();
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        givenMomentsAuMoinsUnPourSejour1();
        givenTypeActivitePourSejour1();
        when(utilisateurRepository.findByTokenId("mem-1")).thenReturn(Optional.of(membre));
        when(sejourEquipeRepository.existsById(new SejourEquipeId(1, 10))).thenReturn(true);
        when(groupeRepository.findById(5)).thenReturn(Optional.of(g5));
        when(groupeRepository.findById(6)).thenReturn(Optional.of(g6));
        when(activiteRepository.save(any(Activite.class))).thenAnswer(inv -> {
            Activite saved = inv.getArgument(0);
            saved.setId(100);
            return saved;
        });

        CreateActiviteRequest req = new CreateActiviteRequest(
                LocalDate.of(2026, 7, 5),
                "Kayak",
                "Sortie matin",
                null,
                MOMENT_ID,
                TYPE_ACTIVITE_ID,
                List.of("mem-1"),
                List.of(6, 5));

        ActiviteDto dto = activiteService.creerActivite(1, req);

        assertThat(dto.id()).isEqualTo(100);
        assertThat(dto.typeActivite().id()).isEqualTo(TYPE_ACTIVITE_ID);
        assertThat(dto.groupeIds()).containsExactly(5, 6);
        assertThat(dto.nom()).isEqualTo("Kayak");
        assertThat(dto.lieu()).isNull();
        assertThat(dto.avertissementLieu()).isNull();
    }

    @Test
    @DisplayName("creerActivite - lieu du séjour")
    void creer_withLieuDuSejour_shouldSetLieu() {
        Lieu lieu = new Lieu();
        lieu.setId(42);
        lieu.setNom("Salle polyvalente");
        lieu.setEmplacement(EmplacementLieu.INTERIEUR);
        lieu.setSejour(sejour);
        lieu.setPartageableEntreAnimateurs(false);
        Groupe g5 = Groupe.builder().id(5).nom("G5").typeGroupe(TypeGroupe.THEMATIQUE).sejour(sejour).build();
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        givenMomentsAuMoinsUnPourSejour1();
        givenTypeActivitePourSejour1();
        when(lieuRepository.findByIdAndSejourId(42, 1)).thenReturn(Optional.of(lieu));
        when(activiteRepository.countBySejour_IdAndLieu_IdAndDateAndMoment_Id(1, 42, LocalDate.of(2026, 7, 5), MOMENT_ID))
                .thenReturn(0L);
        when(utilisateurRepository.findByTokenId("mem-1")).thenReturn(Optional.of(membre));
        when(sejourEquipeRepository.existsById(new SejourEquipeId(1, 10))).thenReturn(true);
        when(groupeRepository.findById(5)).thenReturn(Optional.of(g5));
        when(activiteRepository.save(any(Activite.class))).thenAnswer(inv -> {
            Activite saved = inv.getArgument(0);
            saved.setId(100);
            return saved;
        });

        CreateActiviteRequest req = new CreateActiviteRequest(
                LocalDate.of(2026, 7, 5),
                "Kayak",
                null,
                42,
                MOMENT_ID,
                TYPE_ACTIVITE_ID,
                List.of("mem-1"),
                List.of(5));

        ActiviteDto dto = activiteService.creerActivite(1, req);

        assertThat(dto.lieu()).isNotNull();
        assertThat(dto.lieu().id()).isEqualTo(42);
        assertThat(dto.lieu().nom()).isEqualTo("Salle polyvalente");
        assertThat(dto.avertissementLieu()).isNull();
    }

    @Test
    @DisplayName("creerActivite - lieu déjà pris et non partageable")
    void creer_whenLieuDejaPrisNonPartageable_shouldThrow() {
        Lieu lieu = new Lieu();
        lieu.setId(42);
        lieu.setNom("Salle");
        lieu.setEmplacement(EmplacementLieu.INTERIEUR);
        lieu.setSejour(sejour);
        lieu.setPartageableEntreAnimateurs(false);
        Groupe g5 = Groupe.builder().id(5).nom("G5").typeGroupe(TypeGroupe.THEMATIQUE).sejour(sejour).build();
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        givenMomentsAuMoinsUnPourSejour1();
        givenTypeActivitePourSejour1();
        when(lieuRepository.findByIdAndSejourId(42, 1)).thenReturn(Optional.of(lieu));
        when(activiteRepository.countBySejour_IdAndLieu_IdAndDateAndMoment_Id(1, 42, LocalDate.of(2026, 7, 5), MOMENT_ID))
                .thenReturn(1L);
        when(utilisateurRepository.findByTokenId("mem-1")).thenReturn(Optional.of(membre));
        when(sejourEquipeRepository.existsById(new SejourEquipeId(1, 10))).thenReturn(true);
        when(groupeRepository.findById(5)).thenReturn(Optional.of(g5));

        CreateActiviteRequest req = new CreateActiviteRequest(
                LocalDate.of(2026, 7, 5),
                "Kayak",
                null,
                42,
                MOMENT_ID,
                TYPE_ACTIVITE_ID,
                List.of("mem-1"),
                List.of(5));

        assertThatThrownBy(() -> activiteService.creerActivite(1, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("déjà utilisé")
                .hasMessageContaining("partagé");
        verify(activiteRepository, never()).save(any());
    }

    @Test
    @DisplayName("creerActivite - lieu partageable avec avertissement")
    void creer_whenLieuPartageableDejaOccupe_shouldReturnAvertissement() {
        Lieu lieu = new Lieu();
        lieu.setId(42);
        lieu.setNom("Terrain");
        lieu.setEmplacement(EmplacementLieu.EXTERIEUR);
        lieu.setSejour(sejour);
        lieu.setPartageableEntreAnimateurs(true);
        lieu.setNombreMaxActivitesSimultanees(3);
        Groupe g5 = Groupe.builder().id(5).nom("G5").typeGroupe(TypeGroupe.THEMATIQUE).sejour(sejour).build();
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        givenMomentsAuMoinsUnPourSejour1();
        givenTypeActivitePourSejour1();
        when(lieuRepository.findByIdAndSejourId(42, 1)).thenReturn(Optional.of(lieu));
        when(activiteRepository.countBySejour_IdAndLieu_IdAndDateAndMoment_Id(1, 42, LocalDate.of(2026, 7, 5), MOMENT_ID))
                .thenReturn(1L);
        when(utilisateurRepository.findByTokenId("mem-1")).thenReturn(Optional.of(membre));
        when(sejourEquipeRepository.existsById(new SejourEquipeId(1, 10))).thenReturn(true);
        when(groupeRepository.findById(5)).thenReturn(Optional.of(g5));
        when(activiteRepository.save(any(Activite.class))).thenAnswer(inv -> {
            Activite saved = inv.getArgument(0);
            saved.setId(100);
            return saved;
        });

        CreateActiviteRequest req = new CreateActiviteRequest(
                LocalDate.of(2026, 7, 5),
                "Foot",
                null,
                42,
                MOMENT_ID,
                TYPE_ACTIVITE_ID,
                List.of("mem-1"),
                List.of(5));

        ActiviteDto dto = activiteService.creerActivite(1, req);

        assertThat(dto.avertissementLieu()).isNotNull();
        assertThat(dto.avertissementLieu()).contains("déjà affecté");
        assertThat(dto.avertissementLieu()).contains("acceptée");
    }

    @Test
    @DisplayName("creerActivite - lieu partageable limite atteinte")
    void creer_whenLieuPartageableLimiteAtteinte_shouldThrow() {
        Lieu lieu = new Lieu();
        lieu.setId(42);
        lieu.setNom("Terrain");
        lieu.setEmplacement(EmplacementLieu.EXTERIEUR);
        lieu.setSejour(sejour);
        lieu.setPartageableEntreAnimateurs(true);
        lieu.setNombreMaxActivitesSimultanees(2);
        Groupe g5 = Groupe.builder().id(5).nom("G5").typeGroupe(TypeGroupe.THEMATIQUE).sejour(sejour).build();
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        givenMomentsAuMoinsUnPourSejour1();
        givenTypeActivitePourSejour1();
        when(lieuRepository.findByIdAndSejourId(42, 1)).thenReturn(Optional.of(lieu));
        when(activiteRepository.countBySejour_IdAndLieu_IdAndDateAndMoment_Id(1, 42, LocalDate.of(2026, 7, 5), MOMENT_ID))
                .thenReturn(2L);
        when(utilisateurRepository.findByTokenId("mem-1")).thenReturn(Optional.of(membre));
        when(sejourEquipeRepository.existsById(new SejourEquipeId(1, 10))).thenReturn(true);
        when(groupeRepository.findById(5)).thenReturn(Optional.of(g5));

        CreateActiviteRequest req = new CreateActiviteRequest(
                LocalDate.of(2026, 7, 5),
                "Foot",
                null,
                42,
                MOMENT_ID,
                TYPE_ACTIVITE_ID,
                List.of("mem-1"),
                List.of(5));

        assertThatThrownBy(() -> activiteService.creerActivite(1, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("limite")
                .hasMessageContaining("partage");
        verify(activiteRepository, never()).save(any());
    }

    @Test
    @DisplayName("creerActivite - lieu absent ou autre séjour")
    void creer_whenLieuNotInSejour_shouldThrow() {
        Groupe g5 = Groupe.builder().id(5).nom("G5").typeGroupe(TypeGroupe.THEMATIQUE).sejour(sejour).build();
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        givenMomentsAuMoinsUnPourSejour1();
        when(lieuRepository.findByIdAndSejourId(99, 1)).thenReturn(Optional.empty());
        when(utilisateurRepository.findByTokenId("mem-1")).thenReturn(Optional.of(membre));
        when(sejourEquipeRepository.existsById(new SejourEquipeId(1, 10))).thenReturn(true);
        when(groupeRepository.findById(5)).thenReturn(Optional.of(g5));

        CreateActiviteRequest req = new CreateActiviteRequest(
                LocalDate.of(2026, 7, 5),
                "Kayak",
                null,
                99,
                MOMENT_ID,
                TYPE_ACTIVITE_ID,
                List.of("mem-1"),
                List.of(5));

        assertThatThrownBy(() -> activiteService.creerActivite(1, req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Lieu non trouvé pour ce séjour");
        verify(activiteRepository, never()).save(any());
    }

    @Test
    @DisplayName("creerActivite - directeur accepté sans être dans la table équipe")
    void creer_whenDirecteurNotInEquipe_shouldSucceed() {
        Utilisateur directeur = Utilisateur.builder()
                .id(10)
                .tokenId("dir-1")
                .nom("Martin")
                .prenom("Sophie")
                .build();
        Sejour sejourAvecDirecteur = Sejour.builder()
                .id(1)
                .nom("Colo")
                .directeur(directeur)
                .dateDebut(Date.valueOf(LocalDate.of(2026, 7, 1)))
                .dateFin(Date.valueOf(LocalDate.of(2026, 7, 15)))
                .build();
        Groupe g = Groupe.builder().id(5).nom("G").typeGroupe(TypeGroupe.THEMATIQUE).sejour(sejourAvecDirecteur).build();
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejourAvecDirecteur));
        givenMomentsAuMoinsUnPourSejour1();
        givenTypeActivitePourSejour1();
        when(utilisateurRepository.findByTokenId("dir-1")).thenReturn(Optional.of(directeur));
        when(groupeRepository.findById(5)).thenReturn(Optional.of(g));
        when(activiteRepository.save(any(Activite.class))).thenAnswer(inv -> {
            Activite saved = inv.getArgument(0);
            saved.setId(100);
            return saved;
        });

        CreateActiviteRequest req = new CreateActiviteRequest(
                LocalDate.of(2026, 7, 5),
                "Réunion",
                null,
                null,
                MOMENT_ID,
                TYPE_ACTIVITE_ID,
                List.of("dir-1"),
                List.of(5));

        ActiviteDto dto = activiteService.creerActivite(1, req);

        assertThat(dto.id()).isEqualTo(100);
        verify(sejourEquipeRepository, never()).existsById(any());
    }

    @Test
    @DisplayName("creerActivite - membre hors équipe")
    void creer_whenNotInEquipe_shouldThrow() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        givenMomentsAuMoinsUnPourSejour1();
        when(utilisateurRepository.findByTokenId("mem-1")).thenReturn(Optional.of(membre));
        when(sejourEquipeRepository.existsById(new SejourEquipeId(1, 10))).thenReturn(false);

        CreateActiviteRequest req = new CreateActiviteRequest(
                LocalDate.of(2026, 7, 5),
                "Kayak",
                null,
                null,
                MOMENT_ID,
                TYPE_ACTIVITE_ID,
                List.of("mem-1"),
                List.of(5));

        assertThatThrownBy(() -> activiteService.creerActivite(1, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ne fait pas partie de l'équipe");
        verify(activiteRepository, never()).save(any());
    }

    @Test
    @DisplayName("creerActivite - groupe d'un autre séjour")
    void creer_whenGroupeWrongSejour_shouldThrow() {
        Sejour autre = Sejour.builder().id(2).nom("X").build();
        Groupe g = Groupe.builder().id(7).nom("G").typeGroupe(TypeGroupe.THEMATIQUE).sejour(autre).build();
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        givenMomentsAuMoinsUnPourSejour1();
        when(utilisateurRepository.findByTokenId("mem-1")).thenReturn(Optional.of(membre));
        when(sejourEquipeRepository.existsById(new SejourEquipeId(1, 10))).thenReturn(true);
        when(groupeRepository.findById(7)).thenReturn(Optional.of(g));

        CreateActiviteRequest req = new CreateActiviteRequest(
                LocalDate.of(2026, 7, 5),
                "Kayak",
                null,
                null,
                MOMENT_ID,
                TYPE_ACTIVITE_ID,
                List.of("mem-1"),
                List.of(7));

        assertThatThrownBy(() -> activiteService.creerActivite(1, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("n'appartient pas à ce séjour");
    }

    @Test
    @DisplayName("creerActivite - date avant la période du séjour")
    void creer_whenDateBeforeSejour_shouldThrow() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        givenMomentsAuMoinsUnPourSejour1();
        CreateActiviteRequest req = new CreateActiviteRequest(
                LocalDate.of(2026, 6, 30),
                "Kayak",
                null,
                null,
                MOMENT_ID,
                TYPE_ACTIVITE_ID,
                List.of("mem-1"),
                List.of(5));

        assertThatThrownBy(() -> activiteService.creerActivite(1, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("doit être comprise entre");
        verify(activiteRepository, never()).save(any());
    }

    @Test
    @DisplayName("creerActivite - date après la période du séjour")
    void creer_whenDateAfterSejour_shouldThrow() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        givenMomentsAuMoinsUnPourSejour1();
        CreateActiviteRequest req = new CreateActiviteRequest(
                LocalDate.of(2026, 7, 16),
                "Kayak",
                null,
                null,
                MOMENT_ID,
                TYPE_ACTIVITE_ID,
                List.of("mem-1"),
                List.of(5));

        assertThatThrownBy(() -> activiteService.creerActivite(1, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("doit être comprise entre");
        verify(activiteRepository, never()).save(any());
    }

    @Test
    @DisplayName("creerActivite - séjour sans dates de début / fin")
    void creer_whenSejourHasNoBounds_shouldThrow() {
        Sejour sansDates = Sejour.builder()
                .id(1)
                .nom("Colo")
                .dateDebut(null)
                .dateFin(null)
                .build();
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sansDates));
        givenMomentsAuMoinsUnPourSejour1();
        CreateActiviteRequest req = new CreateActiviteRequest(
                LocalDate.of(2026, 7, 5),
                "Kayak",
                null,
                null,
                MOMENT_ID,
                TYPE_ACTIVITE_ID,
                List.of("mem-1"),
                List.of(5));

        assertThatThrownBy(() -> activiteService.creerActivite(1, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("date de début et une date de fin");
        verify(activiteRepository, never()).save(any());
    }

    @Test
    @DisplayName("modifierActivite - date hors période du séjour")
    void modifier_whenDateOutsideSejour_shouldThrow() {
        Activite a = activitePersistee(4, List.of(membre));
        when(activiteRepository.findByIdAndSejourId(4, 1)).thenReturn(Optional.of(a));
        givenMomentsAuMoinsUnPourSejour1();

        UpdateActiviteRequest req = new UpdateActiviteRequest(
                LocalDate.of(2026, 7, 20),
                "Kayak",
                null,
                null,
                MOMENT_ID,
                TYPE_ACTIVITE_ID,
                List.of("mem-1"),
                List.of(5));

        assertThatThrownBy(() -> activiteService.modifierActivite(1, 4, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("doit être comprise entre");
        verify(activiteRepository, never()).save(any());
    }

    @Test
    @DisplayName("getActivite - succès")
    void get_shouldReturnDto() {
        Activite a = activitePersistee(4, List.of(membre));
        when(activiteRepository.findByIdAndSejourId(4, 1)).thenReturn(Optional.of(a));
        ActiviteDto dto = activiteService.getActivite(1, 4);
        assertThat(dto.id()).isEqualTo(4);
        assertThat(dto.avertissementLieu()).isNull();
    }

    @Test
    @DisplayName("supprimerActivite - absent")
    void supprimer_whenMissing_shouldThrow() {
        when(activiteRepository.findByIdAndSejourId(4, 1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> activiteService.supprimerActivite(1, 4))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(activiteRepository, never()).delete(any());
    }

    @Test
    @DisplayName("creerActivite - aucun moment pour le séjour")
    void creer_whenAucunMoment_shouldAskDirection() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(momentRepository.countBySejourId(1)).thenReturn(0L);

        CreateActiviteRequest req = new CreateActiviteRequest(
                LocalDate.of(2026, 7, 5),
                "Kayak",
                null,
                null,
                MOMENT_ID,
                TYPE_ACTIVITE_ID,
                List.of("mem-1"),
                List.of(5));

        assertThatThrownBy(() -> activiteService.creerActivite(1, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("direction");
        verify(momentRepository, never()).findByIdAndSejourId(anyInt(), anyInt());
        verify(activiteRepository, never()).save(any());
    }

    @Test
    @DisplayName("creerActivite - moment non renseigné alors qu'il en existe")
    void creer_whenMomentIdNull_shouldThrow() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(momentRepository.countBySejourId(1)).thenReturn(1L);

        CreateActiviteRequest req = new CreateActiviteRequest(
                LocalDate.of(2026, 7, 5),
                "Kayak",
                null,
                null,
                null,
                TYPE_ACTIVITE_ID,
                List.of("mem-1"),
                List.of(5));

        assertThatThrownBy(() -> activiteService.creerActivite(1, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatoire");
        verify(activiteRepository, never()).save(any());
    }

    private Activite activitePersistee(int id, List<Utilisateur> membres) {
        Activite a = new Activite();
        a.setId(id);
        a.setDate(LocalDate.of(2026, 7, 5));
        a.setNom("Act");
        a.setDescription("d");
        a.setSejour(sejour);
        a.setMoment(momentMatin);
        a.setTypeActivite(typeActiviteSport);
        a.setMembres(new ArrayList<>(membres));
        a.setGroupes(new ArrayList<>());
        return a;
    }
}
