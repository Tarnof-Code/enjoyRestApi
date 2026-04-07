package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.*;
import com.tarnof.enjoyrestapi.enums.Genre;
import com.tarnof.enjoyrestapi.enums.NiveauScolaire;
import com.tarnof.enjoyrestapi.enums.TypeGroupe;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.AjouterReferentRequest;
import com.tarnof.enjoyrestapi.payload.request.CreateGroupeRequest;
import com.tarnof.enjoyrestapi.payload.response.GroupeDto;
import com.tarnof.enjoyrestapi.repositories.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour GroupeServiceImpl")
@SuppressWarnings("null")
class GroupeServiceImplTest {

    @Mock
    private GroupeRepository groupeRepository;

    @Mock
    private SejourRepository sejourRepository;

    @Mock
    private EnfantRepository enfantRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private SejourEnfantRepository sejourEnfantRepository;

    private GroupeServiceImpl groupeService;

    private Sejour sejour;

    @BeforeEach
    void setUp() {
        groupeService = new GroupeServiceImpl(
                groupeRepository,
                new SejourVerificationService(sejourRepository),
                enfantRepository,
                utilisateurRepository,
                sejourEnfantRepository
        );

        sejour = Sejour.builder()
                .id(1)
                .nom("Colo été")
                .description("desc")
                .dateDebut(Date.valueOf(LocalDate.of(2026, 3, 20)))
                .dateFin(Date.valueOf(LocalDate.of(2026, 3, 27)))
                .lieuDuSejour("Mer")
                .enfants(new ArrayList<>())
                .build();
    }

    private Groupe groupePersiste(int id, TypeGroupe type, Sejour s) {
        return Groupe.builder()
                .id(id)
                .nom("Mon groupe")
                .description("d")
                .typeGroupe(type)
                .ageMin(type == TypeGroupe.AGE ? 7 : null)
                .ageMax(type == TypeGroupe.AGE ? 12 : null)
                .niveauScolaireMin(type == TypeGroupe.NIVEAU_SCOLAIRE ? NiveauScolaire.CP : null)
                .niveauScolaireMax(type == TypeGroupe.NIVEAU_SCOLAIRE ? NiveauScolaire.CE2 : null)
                .sejour(s)
                .enfants(new ArrayList<>())
                .referents(new ArrayList<>())
                .build();
    }

    // ---------- getGroupesDuSejour ----------

    @Test
    @DisplayName("getGroupesDuSejour - Liste des groupes du séjour")
    void getGroupesDuSejour_ShouldReturnDtos() {
        Groupe g = groupePersiste(10, TypeGroupe.THEMATIQUE, sejour);
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(groupeRepository.findBySejourId(1)).thenReturn(List.of(g));

        List<GroupeDto> result = groupeService.getGroupesDuSejour(1);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(10);
        assertThat(result.getFirst().sejourId()).isEqualTo(1);
    }

    @Test
    @DisplayName("getGroupesDuSejour - Séjour inexistant")
    void getGroupesDuSejour_WhenSejourMissing_ShouldThrow404() {
        when(sejourRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupeService.getGroupesDuSejour(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Séjour non trouvé");
    }

    // ---------- getGroupeById ----------

    @Test
    @DisplayName("getGroupeById - Succès")
    void getGroupeById_ShouldReturnDto() {
        Groupe g = groupePersiste(5, TypeGroupe.THEMATIQUE, sejour);
        when(groupeRepository.findById(5)).thenReturn(Optional.of(g));

        GroupeDto dto = groupeService.getGroupeById(1, 5);

        assertThat(dto.id()).isEqualTo(5);
        assertThat(dto.sejourId()).isEqualTo(1);
    }

    @Test
    @DisplayName("getGroupeById - Groupe sur un autre séjour")
    void getGroupeById_WhenWrongSejour_ShouldThrow404() {
        Groupe g = groupePersiste(5, TypeGroupe.THEMATIQUE, sejour);
        when(groupeRepository.findById(5)).thenReturn(Optional.of(g));

        assertThatThrownBy(() -> groupeService.getGroupeById(999, 5))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("n'appartient pas à ce séjour");
    }

    @Test
    @DisplayName("getGroupeById - Groupe inconnu")
    void getGroupeById_WhenGroupeMissing_ShouldThrow404() {
        when(groupeRepository.findById(5)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupeService.getGroupeById(1, 5))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Groupe non trouvé");
    }

    // ---------- creerGroupe ----------

    @Test
    @DisplayName("creerGroupe - Thématique sans remplissage auto")
    void creerGroupe_thematique_ShouldSaveAndReturnEmptyEnfants() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(sejourEnfantRepository.findBySejourIdWithEnfant(1)).thenReturn(List.of());

        Groupe[] box = new Groupe[1];
        when(groupeRepository.save(any(Groupe.class))).thenAnswer(inv -> {
            Groupe g = inv.getArgument(0);
            if (g.getId() == null) {
                g.setId(100);
            }
            box[0] = g;
            return g;
        });
        when(groupeRepository.findById(100)).thenAnswer(inv -> Optional.of(box[0]));

        CreateGroupeRequest req = new CreateGroupeRequest(
                "Atelier photo", "desc", TypeGroupe.THEMATIQUE,
                null, null, null, null
        );

        GroupeDto dto = groupeService.creerGroupe(1, req);

        assertThat(dto.nom()).isEqualTo("Atelier photo");
        assertThat(dto.typeGroupe()).isEqualTo(TypeGroupe.THEMATIQUE);
        assertThat(dto.enfants()).isEmpty();
        verify(groupeRepository, times(2)).save(any(Groupe.class));
    }

    @Test
    @DisplayName("creerGroupe - Par âge : rattache les enfants du séjour dans la tranche")
    void creerGroupe_age_ShouldAttachChildrenInAgeRange() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));

        Enfant enfant = Enfant.builder()
                .id(7)
                .nom("Lopez")
                .prenom("Ana")
                .genre(Genre.Féminin)
                .dateNaissance(Date.valueOf(LocalDate.of(2018, 3, 20)))
                .niveauScolaire(NiveauScolaire.CP)
                .build();
        SejourEnfant se = new SejourEnfant(sejour, enfant);
        when(sejourEnfantRepository.findBySejourIdWithEnfant(1)).thenReturn(List.of(se));

        Groupe[] box = new Groupe[1];
        when(groupeRepository.save(any(Groupe.class))).thenAnswer(inv -> {
            Groupe g = inv.getArgument(0);
            if (g.getId() == null) {
                g.setId(42);
            }
            box[0] = g;
            return g;
        });
        when(groupeRepository.findById(42)).thenAnswer(inv -> Optional.of(box[0]));

        CreateGroupeRequest req = new CreateGroupeRequest(
                "7-10 ans", null, TypeGroupe.AGE, 7, 10, null, null
        );

        GroupeDto dto = groupeService.creerGroupe(1, req);

        assertThat(dto.enfants()).hasSize(1);
        assertThat(dto.enfants().getFirst().id()).isEqualTo(7);
    }

    @Test
    @DisplayName("creerGroupe - Par niveau scolaire : filtre les enfants")
    void creerGroupe_niveauScolaire_ShouldAttachMatchingLevel() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));

        Enfant enfant = Enfant.builder()
                .id(3)
                .nom("Durand")
                .prenom("Leo")
                .genre(Genre.Masculin)
                .dateNaissance(Date.valueOf(LocalDate.of(2015, 1, 1)))
                .niveauScolaire(NiveauScolaire.CP)
                .build();
        when(sejourEnfantRepository.findBySejourIdWithEnfant(1)).thenReturn(List.of(new SejourEnfant(sejour, enfant)));

        Groupe[] box = new Groupe[1];
        when(groupeRepository.save(any(Groupe.class))).thenAnswer(inv -> {
            Groupe g = inv.getArgument(0);
            if (g.getId() == null) {
                g.setId(33);
            }
            box[0] = g;
            return g;
        });
        when(groupeRepository.findById(33)).thenAnswer(inv -> Optional.of(box[0]));

        CreateGroupeRequest req = new CreateGroupeRequest(
                "CP à CE2", null, TypeGroupe.NIVEAU_SCOLAIRE,
                null, null, NiveauScolaire.CP, NiveauScolaire.CE2
        );

        GroupeDto dto = groupeService.creerGroupe(1, req);

        assertThat(dto.enfants()).hasSize(1);
        assertThat(dto.enfants().getFirst().niveauScolaire()).isEqualTo(NiveauScolaire.CP);
    }

    @Test
    @DisplayName("creerGroupe - Type AGE sans ageMin / ageMax")
    void creerGroupe_age_incomplete_ShouldThrowIllegalArgument() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        CreateGroupeRequest req = new CreateGroupeRequest(
                "mal formé", null, TypeGroupe.AGE, null, 10, null, null
        );

        assertThatThrownBy(() -> groupeService.creerGroupe(1, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ageMin et ageMax sont obligatoires");
    }

    @Test
    @DisplayName("creerGroupe - ageMin > ageMax")
    void creerGroupe_age_invalidOrder_ShouldThrowIllegalArgument() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        CreateGroupeRequest req = new CreateGroupeRequest(
                "incohérent", null, TypeGroupe.AGE, 12, 7, null, null
        );

        assertThatThrownBy(() -> groupeService.creerGroupe(1, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ageMin doit être inférieur ou égal à ageMax");
    }

    @Test
    @DisplayName("creerGroupe - Niveau scolaire : min après max")
    void creerGroupe_niveau_invalidOrder_ShouldThrowIllegalArgument() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        CreateGroupeRequest req = new CreateGroupeRequest(
                "bad", null, TypeGroupe.NIVEAU_SCOLAIRE,
                null, null, NiveauScolaire.CE2, NiveauScolaire.CP
        );

        assertThatThrownBy(() -> groupeService.creerGroupe(1, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("niveauScolaireMin doit être antérieur ou égal");
    }

    // ---------- modifierGroupe ----------

    @Test
    @DisplayName("modifierGroupe - Met à jour et retourne le DTO")
    void modifierGroupe_ShouldPersistChanges() {
        Groupe g = groupePersiste(8, TypeGroupe.THEMATIQUE, sejour);
        when(groupeRepository.findById(8)).thenReturn(Optional.of(g));
        when(groupeRepository.save(any(Groupe.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateGroupeRequest req = new CreateGroupeRequest(
                "Nouveau nom", "nouvelle desc", TypeGroupe.THEMATIQUE,
                null, null, null, null
        );

        GroupeDto dto = groupeService.modifierGroupe(1, 8, req);

        assertThat(dto.nom()).isEqualTo("Nouveau nom");
        assertThat(dto.description()).isEqualTo("nouvelle desc");
        verify(groupeRepository).save(g);
    }

    // ---------- supprimerGroupe ----------

    @Test
    @DisplayName("supprimerGroupe - Supprime l'entité")
    void supprimerGroupe_ShouldCallDelete() {
        Groupe g = groupePersiste(8, TypeGroupe.THEMATIQUE, sejour);
        when(groupeRepository.findById(8)).thenReturn(Optional.of(g));

        groupeService.supprimerGroupe(1, 8);

        verify(groupeRepository).delete(g);
    }

    // ---------- ajouterEnfantAuGroupe ----------

    @Test
    @DisplayName("ajouterEnfantAuGroupe - Succès")
    void ajouterEnfantAuGroupe_ShouldAddChild() {
        Groupe g = groupePersiste(8, TypeGroupe.THEMATIQUE, sejour);
        Enfant enfant = Enfant.builder()
                .id(20)
                .nom("X")
                .prenom("Y")
                .genre(Genre.Masculin)
                .dateNaissance(new java.util.Date())
                .niveauScolaire(NiveauScolaire.MS)
                .build();
        when(groupeRepository.findById(8)).thenReturn(Optional.of(g));
        when(enfantRepository.findById(20)).thenReturn(Optional.of(enfant));
        when(sejourEnfantRepository.existsById(new SejourEnfantId(1, 20))).thenReturn(true);
        when(groupeRepository.save(g)).thenReturn(g);

        groupeService.ajouterEnfantAuGroupe(1, 8, 20);

        assertThat(g.getEnfants()).contains(enfant);
        verify(groupeRepository).save(g);
    }

    @Test
    @DisplayName("ajouterEnfantAuGroupe - Enfant déjà dans le groupe")
    void ajouterEnfantAuGroupe_whenDuplicate_ShouldThrow409() {
        Groupe g = groupePersiste(8, TypeGroupe.THEMATIQUE, sejour);
        Enfant enfant = Enfant.builder()
                .id(20)
                .nom("X")
                .prenom("Y")
                .genre(Genre.Masculin)
                .dateNaissance(new java.util.Date())
                .niveauScolaire(NiveauScolaire.MS)
                .build();
        g.getEnfants().add(enfant);
        when(groupeRepository.findById(8)).thenReturn(Optional.of(g));
        when(enfantRepository.findById(20)).thenReturn(Optional.of(enfant));
        when(sejourEnfantRepository.existsById(new SejourEnfantId(1, 20))).thenReturn(true);

        assertThatThrownBy(() -> groupeService.ajouterEnfantAuGroupe(1, 8, 20))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("déjà partie du groupe");
    }

    @Test
    @DisplayName("ajouterEnfantAuGroupe - Enfant non inscrit au séjour")
    void ajouterEnfantAuGroupe_whenNotInSejour_ShouldThrowIllegalArgument() {
        Groupe g = groupePersiste(8, TypeGroupe.THEMATIQUE, sejour);
        Enfant enfant = Enfant.builder()
                .id(20)
                .nom("X")
                .prenom("Y")
                .genre(Genre.Masculin)
                .dateNaissance(new java.util.Date())
                .niveauScolaire(NiveauScolaire.MS)
                .build();
        when(groupeRepository.findById(8)).thenReturn(Optional.of(g));
        when(enfantRepository.findById(20)).thenReturn(Optional.of(enfant));
        when(sejourEnfantRepository.existsById(new SejourEnfantId(1, 20))).thenReturn(false);

        assertThatThrownBy(() -> groupeService.ajouterEnfantAuGroupe(1, 8, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inscrit au séjour");
    }

    // ---------- retirerEnfantDuGroupe ----------

    @Test
    @DisplayName("retirerEnfantDuGroupe - Succès")
    void retirerEnfantDuGroupe_ShouldRemove() {
        Groupe g = groupePersiste(8, TypeGroupe.THEMATIQUE, sejour);
        Enfant enfant = Enfant.builder()
                .id(20)
                .nom("X")
                .prenom("Y")
                .genre(Genre.Masculin)
                .dateNaissance(new java.util.Date())
                .niveauScolaire(NiveauScolaire.MS)
                .build();
        g.getEnfants().add(enfant);
        when(groupeRepository.findById(8)).thenReturn(Optional.of(g));
        when(groupeRepository.save(g)).thenReturn(g);

        groupeService.retirerEnfantDuGroupe(1, 8, 20);

        assertThat(g.getEnfants()).isEmpty();
    }

    @Test
    @DisplayName("retirerEnfantDuGroupe - Enfant absent du groupe")
    void retirerEnfantDuGroupe_whenMissing_ShouldThrow404() {
        Groupe g = groupePersiste(8, TypeGroupe.THEMATIQUE, sejour);
        when(groupeRepository.findById(8)).thenReturn(Optional.of(g));

        assertThatThrownBy(() -> groupeService.retirerEnfantDuGroupe(1, 8, 20))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ne fait pas partie du groupe");
    }

    // ---------- référents ----------

    @Test
    @DisplayName("ajouterReferent - Succès")
    void ajouterReferent_ShouldAdd() {
        Groupe g = groupePersiste(8, TypeGroupe.THEMATIQUE, sejour);
        Utilisateur ref = Utilisateur.builder()
                .id(5)
                .tokenId("tok-ref")
                .nom("Martin")
                .prenom("Claire")
                .build();
        when(groupeRepository.findById(8)).thenReturn(Optional.of(g));
        when(utilisateurRepository.findByTokenId("tok-ref")).thenReturn(Optional.of(ref));
        when(groupeRepository.save(g)).thenReturn(g);

        groupeService.ajouterReferent(1, 8, new AjouterReferentRequest("tok-ref"));

        assertThat(g.getReferents()).contains(ref);
    }

    @Test
    @DisplayName("ajouterReferent - Référent déjà présent")
    void ajouterReferent_duplicate_ShouldThrow409() {
        Groupe g = groupePersiste(8, TypeGroupe.THEMATIQUE, sejour);
        Utilisateur ref = Utilisateur.builder()
                .id(5)
                .tokenId("tok-ref")
                .nom("Martin")
                .prenom("Claire")
                .build();
        g.getReferents().add(ref);
        when(groupeRepository.findById(8)).thenReturn(Optional.of(g));
        when(utilisateurRepository.findByTokenId("tok-ref")).thenReturn(Optional.of(ref));

        assertThatThrownBy(() -> groupeService.ajouterReferent(1, 8, new AjouterReferentRequest("tok-ref")))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("référent fait déjà partie");
    }

    @Test
    @DisplayName("retirerReferent - Succès")
    void retirerReferent_ShouldRemove() {
        Groupe g = groupePersiste(8, TypeGroupe.THEMATIQUE, sejour);
        Utilisateur ref = Utilisateur.builder()
                .id(5)
                .tokenId("tok-ref")
                .nom("Martin")
                .prenom("Claire")
                .build();
        g.getReferents().add(ref);
        when(groupeRepository.findById(8)).thenReturn(Optional.of(g));
        when(utilisateurRepository.findByTokenId("tok-ref")).thenReturn(Optional.of(ref));
        when(groupeRepository.save(g)).thenReturn(g);

        groupeService.retirerReferent(1, 8, "tok-ref");

        assertThat(g.getReferents()).isEmpty();
    }

    @Test
    @DisplayName("retirerReferent - Absent du groupe")
    void retirerReferent_whenNotInGroupe_ShouldThrow404() {
        Groupe g = groupePersiste(8, TypeGroupe.THEMATIQUE, sejour);
        Utilisateur ref = Utilisateur.builder()
                .id(5)
                .tokenId("tok-ref")
                .nom("Martin")
                .prenom("Claire")
                .build();
        when(groupeRepository.findById(8)).thenReturn(Optional.of(g));
        when(utilisateurRepository.findByTokenId("tok-ref")).thenReturn(Optional.of(ref));

        assertThatThrownBy(() -> groupeService.retirerReferent(1, 8, "tok-ref"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ne fait pas partie du groupe");
    }
}
