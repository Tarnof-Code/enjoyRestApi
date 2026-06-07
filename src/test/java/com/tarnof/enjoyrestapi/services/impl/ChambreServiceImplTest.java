package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.Chambre;
import com.tarnof.enjoyrestapi.entities.ChambreOccupant;
import com.tarnof.enjoyrestapi.entities.Enfant;
import com.tarnof.enjoyrestapi.entities.Groupe;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.SejourEnfantId;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.Genre;
import com.tarnof.enjoyrestapi.enums.GenreChambre;
import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.enums.TypeChambre;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.AffecterOccupantChambreRequest;
import com.tarnof.enjoyrestapi.payload.request.AffecterOccupantEnfantItemRequest;
import com.tarnof.enjoyrestapi.payload.request.AffecterOccupantEquipeItemRequest;
import com.tarnof.enjoyrestapi.payload.request.AffecterOccupantsEnfantsRequest;
import com.tarnof.enjoyrestapi.payload.request.AffecterOccupantsEquipeRequest;
import com.tarnof.enjoyrestapi.payload.request.AjouterReferentRequest;
import com.tarnof.enjoyrestapi.payload.request.SaveChambreRequest;
import com.tarnof.enjoyrestapi.repositories.ChambreOccupantRepository;
import com.tarnof.enjoyrestapi.repositories.ChambreRepository;
import com.tarnof.enjoyrestapi.repositories.EnfantRepository;
import com.tarnof.enjoyrestapi.repositories.GroupeRepository;
import com.tarnof.enjoyrestapi.repositories.SejourEnfantRepository;
import com.tarnof.enjoyrestapi.repositories.SejourEquipeRepository;
import com.tarnof.enjoyrestapi.repositories.SejourRepository;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.services.HistoriqueModificationService;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChambreServiceImpl")
@SuppressWarnings("null")
class ChambreServiceImplTest {

    private static final String APPELANT_TOKEN = "appelant-token";

    @Mock
    private ChambreRepository chambreRepository;
    @Mock
    private ChambreOccupantRepository chambreOccupantRepository;
    @Mock
    private SejourRepository sejourRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private SejourEquipeRepository sejourEquipeRepository;
    @Mock
    private EnfantRepository enfantRepository;
    @Mock
    private SejourEnfantRepository sejourEnfantRepository;
    @Mock
    private GroupeRepository groupeRepository;
    @Mock
    private HistoriqueModificationService historiqueModificationService;

    private ChambreServiceImpl chambreService;

    private Sejour sejour;
    private Utilisateur appelantAdmin;

    @BeforeEach
    void setUp() {
        chambreService = new ChambreServiceImpl(
                chambreRepository,
                chambreOccupantRepository,
                new SejourVerificationService(sejourRepository, utilisateurRepository, sejourEquipeRepository),
                utilisateurRepository,
                enfantRepository,
                sejourEnfantRepository,
                groupeRepository,
                historiqueModificationService);
        sejour = new Sejour();
        sejour.setId(1);
        appelantAdmin = Utilisateur.builder()
                .id(99)
                .tokenId(APPELANT_TOKEN)
                .role(Role.ADMIN)
                .build();
        lenient()
                .when(utilisateurRepository.findByTokenId(APPELANT_TOKEN))
                .thenReturn(Optional.of(appelantAdmin));
    }

    private static SaveChambreRequest requestEnfant() {
        return new SaveChambreRequest(
                TypeChambre.ENFANT,
                "101",
                "Les copains",
                4,
                GenreChambre.MIXTE,
                "Description",
                "Bâtiment A",
                "Nord",
                1,
                null);
    }

    @Test
    @DisplayName("creerChambre - refuse un doublon d'identifiant (même séjour, casse différente)")
    void creerChambre_whenDuplicateIdentifiant_shouldThrow409() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(chambreRepository.existsBySejourIdAndIdentifiantIgnoreCase(1, "101")).thenReturn(true);

        assertThatThrownBy(() -> chambreService.creerChambre(1,
                new SaveChambreRequest(
                        TypeChambre.ENFANT, "  101  ", null, 4, GenreChambre.MIXTE, null, null, null, null, null),
                APPELANT_TOKEN))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("identifiant");

        verify(chambreRepository, never()).save(any());
    }

    @Test
    @DisplayName("creerChambre - persiste avec identifiant trimé et nom optionnel")
    void creerChambre_success_trimsFields() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(chambreRepository.existsBySejourIdAndIdentifiantIgnoreCase(1, "101")).thenReturn(false);
        when(chambreRepository.save(any(Chambre.class))).thenAnswer(inv -> {
            Chambre c = inv.getArgument(0);
            c.setId(10);
            return c;
        });

        var dto = chambreService.creerChambre(1, requestEnfant(), APPELANT_TOKEN);

        assertThat(dto.id()).isEqualTo(10);
        assertThat(dto.identifiant()).isEqualTo("101");
        assertThat(dto.nom()).isEqualTo("Les copains");
        assertThat(dto.typeChambre()).isEqualTo(TypeChambre.ENFANT);
        assertThat(dto.batiment()).isEqualTo("Bâtiment A");
        assertThat(dto.referents()).isEmpty();
        assertThat(dto.occupants()).isEmpty();
    }

    @Test
    @DisplayName("creerChambre - chambre équipe sans nom ni référents")
    void creerChambre_equipe_success() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(chambreRepository.existsBySejourIdAndIdentifiantIgnoreCase(1, "Foyer")).thenReturn(false);
        when(chambreRepository.save(any(Chambre.class))).thenAnswer(inv -> {
            Chambre c = inv.getArgument(0);
            c.setId(11);
            return c;
        });

        var dto = chambreService.creerChambre(1,
                new SaveChambreRequest(
                        TypeChambre.EQUIPE, "Foyer", null, 2, GenreChambre.MIXTE, null, null, null, null, null),
                APPELANT_TOKEN);

        assertThat(dto.typeChambre()).isEqualTo(TypeChambre.EQUIPE);
        assertThat(dto.identifiant()).isEqualTo("Foyer");
        assertThat(dto.nom()).isNull();
        assertThat(dto.referents()).isEmpty();
    }

    @Test
    @DisplayName("modifierChambre - refuse si le nouvel identifiant existe déjà sur une autre chambre")
    void modifierChambre_whenIdentifiantTakenByOther_shouldThrow409() {
        Chambre chambre = new Chambre();
        chambre.setId(5);
        chambre.setTypeChambre(TypeChambre.ENFANT);
        chambre.setIdentifiant("Ancien");
        chambre.setCapaciteMax(2);
        chambre.setGenreAutorise(GenreChambre.FEMININ);
        chambre.setSejour(sejour);
        when(chambreRepository.findById(5)).thenReturn(Optional.of(chambre));
        when(chambreRepository.existsBySejourIdAndIdentifiantIgnoreCaseAndIdNot(1, "Autre", 5)).thenReturn(true);

        assertThatThrownBy(() -> chambreService.modifierChambre(1, 5,
                new SaveChambreRequest(
                        TypeChambre.ENFANT, "Autre", null, 3, GenreChambre.MASCULIN, null, null, null, null, null),
                APPELANT_TOKEN))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(chambreRepository, never()).save(any());
    }

    @Test
    @DisplayName("modifierChambre - passage en équipe supprime les référents")
    void modifierChambre_switchToEquipe_clearsReferents() {
        Chambre chambre = new Chambre();
        chambre.setId(5);
        chambre.setTypeChambre(TypeChambre.ENFANT);
        chambre.setIdentifiant("101");
        chambre.setCapaciteMax(4);
        chambre.setGenreAutorise(GenreChambre.MIXTE);
        chambre.setSejour(sejour);
        chambre.setReferents(new ArrayList<>(List.of(Utilisateur.builder().id(2).tokenId("ref").build())));
        when(chambreRepository.findById(5)).thenReturn(Optional.of(chambre));
        when(chambreRepository.existsBySejourIdAndIdentifiantIgnoreCaseAndIdNot(1, "101", 5)).thenReturn(false);
        when(chambreRepository.save(chambre)).thenReturn(chambre);

        var dto = chambreService.modifierChambre(1, 5,
                new SaveChambreRequest(
                        TypeChambre.EQUIPE, "101", null, 2, GenreChambre.MIXTE, null, null, null, null, null),
                APPELANT_TOKEN);

        assertThat(chambre.getReferents()).isEmpty();
        assertThat(dto.typeChambre()).isEqualTo(TypeChambre.EQUIPE);
        assertThat(dto.referents()).isEmpty();
    }

    @Test
    @DisplayName("modifierChambre - autorise de garder le même identifiant (casse différente)")
    void modifierChambre_sameIdentifiantDifferentCase_shouldSucceed() {
        Chambre chambre = new Chambre();
        chambre.setId(5);
        chambre.setTypeChambre(TypeChambre.ENFANT);
        chambre.setIdentifiant("101");
        chambre.setCapaciteMax(4);
        chambre.setGenreAutorise(GenreChambre.MIXTE);
        chambre.setSejour(sejour);
        when(chambreRepository.findById(5)).thenReturn(Optional.of(chambre));
        when(chambreRepository.existsBySejourIdAndIdentifiantIgnoreCaseAndIdNot(1, "101", 5)).thenReturn(false);
        when(chambreRepository.save(any(Chambre.class))).thenAnswer(inv -> inv.getArgument(0));

        chambreService.modifierChambre(1, 5,
                new SaveChambreRequest(
                        TypeChambre.ENFANT, "101", null, 4, GenreChambre.MIXTE, null, null, null, null, null),
                APPELANT_TOKEN);

        verify(chambreRepository).save(chambre);
        assertThat(chambre.getIdentifiant()).isEqualTo("101");
    }

    @Test
    @DisplayName("modifierChambre - refuse si le nouveau genre exclut un occupant enfant")
    void modifierChambre_whenGenreIncompatibleWithOccupants_shouldThrow400() {
        Chambre chambre = chambreEnfant(5);
        chambre.setGenreAutorise(GenreChambre.MIXTE);
        ChambreOccupant occupant = new ChambreOccupant();
        occupant.setId(50);
        occupant.setChambre(chambre);
        occupant.setEnfant(enfant(42, Genre.Féminin));
        chambre.setOccupants(new ArrayList<>(List.of(occupant)));
        when(chambreRepository.findById(5)).thenReturn(Optional.of(chambre));
        when(chambreRepository.existsBySejourIdAndIdentifiantIgnoreCaseAndIdNot(1, "101", 5)).thenReturn(false);

        assertThatThrownBy(() -> chambreService.modifierChambre(1, 5,
                        new SaveChambreRequest(
                                TypeChambre.ENFANT,
                                "101",
                                null,
                                4,
                                GenreChambre.MASCULIN,
                                null,
                                null,
                                null,
                                null,
                                null),
                APPELANT_TOKEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("genre autorisé");

        verify(chambreRepository, never()).save(any());
    }

    @Test
    @DisplayName("modifierChambre - refuse si le nouveau genre exclut un occupant équipe")
    void modifierChambre_whenGenreIncompatibleWithOccupantEquipe_shouldThrow400() {
        Chambre chambre = chambreEquipe(5);
        chambre.setGenreAutorise(GenreChambre.MIXTE);
        ChambreOccupant occupant = new ChambreOccupant();
        occupant.setId(50);
        occupant.setChambre(chambre);
        occupant.setUtilisateur(membreEquipe(8, "membre-token", Genre.Féminin));
        chambre.setOccupants(new ArrayList<>(List.of(occupant)));
        when(chambreRepository.findById(5)).thenReturn(Optional.of(chambre));
        when(chambreRepository.existsBySejourIdAndIdentifiantIgnoreCaseAndIdNot(1, "Foyer", 5)).thenReturn(false);

        assertThatThrownBy(() -> chambreService.modifierChambre(1, 5,
                        new SaveChambreRequest(
                                TypeChambre.EQUIPE,
                                "Foyer",
                                null,
                                2,
                                GenreChambre.MASCULIN,
                                null,
                                null,
                                null,
                                null,
                                null),
                APPELANT_TOKEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("genre autorisé");

        verify(chambreRepository, never()).save(any());
    }

    @Test
    @DisplayName("creerChambre - 403 si l'utilisateur n'appartient pas au séjour")
    void creerChambre_whenHorsSejour_shouldThrow403() {
        Utilisateur animateur = Utilisateur.builder()
                .id(50)
                .tokenId("animateur-token")
                .role(Role.BASIC_USER)
                .build();
        sejour.setEquipeRoles(new ArrayList<>());
        when(utilisateurRepository.findByTokenId("animateur-token")).thenReturn(Optional.of(animateur));
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));

        assertThatThrownBy(() -> chambreService.creerChambre(1, requestEnfant(), "animateur-token"))
                .isInstanceOf(AccessDeniedException.class);

        verify(chambreRepository, never()).save(any());
    }

    @Test
    @DisplayName("listerChambresDuSejour - 404 si séjour absent")
    void lister_whenSejourMissing_shouldThrow404() {
        Utilisateur appelantBasique = Utilisateur.builder()
                .id(50)
                .tokenId("user-token")
                .role(Role.BASIC_USER)
                .build();
        when(utilisateurRepository.findByTokenId("user-token")).thenReturn(Optional.of(appelantBasique));
        when(sejourRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chambreService.listerChambresDuSejour(99, "user-token"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("listerChambresDuSejour - liste vide OK")
    void lister_whenEmpty_shouldReturnEmpty() {
        when(utilisateurRepository.findByTokenId(APPELANT_TOKEN)).thenReturn(Optional.of(appelantAdmin));
        when(chambreRepository.findBySejourIdOrderAffichageWithOccupants(1)).thenReturn(List.of());

        assertThat(chambreService.listerChambresDuSejour(1, APPELANT_TOKEN)).isEmpty();
    }

    @Test
    @DisplayName("ajouterReferent - ajoute un référent à une chambre enfant")
    void ajouterReferent_success() {
        Chambre chambre = chambreEnfant(3);
        chambre.setReferents(new ArrayList<>());
        Utilisateur referent = Utilisateur.builder().id(7).tokenId("ref-token").build();
        when(chambreRepository.findById(3)).thenReturn(Optional.of(chambre));
        when(utilisateurRepository.findByTokenId("ref-token")).thenReturn(Optional.of(referent));
        when(chambreRepository.save(chambre)).thenReturn(chambre);

        chambreService.ajouterReferent(1, 3, new AjouterReferentRequest("ref-token"), APPELANT_TOKEN);

        assertThat(chambre.getReferents()).hasSize(1);
        verify(chambreRepository).save(chambre);
    }

    @Test
    @DisplayName("ajouterReferent - refuse sur une chambre équipe")
    void ajouterReferent_whenEquipe_shouldThrow400() {
        Chambre chambre = chambreEquipe(4);
        when(chambreRepository.findById(4)).thenReturn(Optional.of(chambre));

        assertThatThrownBy(() -> chambreService.ajouterReferent(1, 4, new AjouterReferentRequest("ref-token"), APPELANT_TOKEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("équipe");

        verify(chambreRepository, never()).save(any());
    }

    @Test
    @DisplayName("ajouterReferent - refuse un doublon")
    void ajouterReferent_whenAlreadyPresent_shouldThrow409() {
        Chambre chambre = chambreEnfant(3);
        Utilisateur referent = Utilisateur.builder().id(7).tokenId("ref-token").build();
        chambre.setReferents(new ArrayList<>(List.of(referent)));
        when(chambreRepository.findById(3)).thenReturn(Optional.of(chambre));
        when(utilisateurRepository.findByTokenId("ref-token")).thenReturn(Optional.of(referent));

        assertThatThrownBy(() -> chambreService.ajouterReferent(1, 3, new AjouterReferentRequest("ref-token"), APPELANT_TOKEN))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(chambreRepository, never()).save(any());
    }

    @Test
    @DisplayName("affecterEnfant - affecte un enfant inscrit à une chambre enfant")
    void affecterEnfant_success() {
        Chambre chambre = chambreEnfant(3);
        chambre.setOccupants(new ArrayList<>());
        Enfant enfant = enfant(42, Genre.Féminin);
        when(chambreRepository.findById(3)).thenReturn(Optional.of(chambre));
        when(sejourEnfantRepository.existsById(new SejourEnfantId(1, 42))).thenReturn(true);
        when(enfantRepository.findById(42)).thenReturn(Optional.of(enfant));
        when(chambreOccupantRepository.findByEnfantIdAndSejourId(42, 1)).thenReturn(Optional.empty());
        when(chambreRepository.save(chambre)).thenAnswer(inv -> {
            chambre.getOccupants().stream().filter(o -> o.getId() == null).forEach(o -> o.setId(200));
            return chambre;
        });
        when(chambreRepository.findByIdAndSejourIdWithOccupants(3, 1)).thenReturn(Optional.of(chambre));

        var dto = chambreService.affecterEnfant(1, 3, 42, new AffecterOccupantChambreRequest(1), APPELANT_TOKEN);

        assertThat(chambre.getOccupants()).hasSize(1);
        assertThat(chambre.getOccupants().getFirst().getEnfant().getId()).isEqualTo(42);
        assertThat(chambre.getOccupants().getFirst().getNumeroLit()).isEqualTo(1);
        assertThat(dto.occupants()).hasSize(1);
        assertThat(dto.occupants().getFirst().enfantId()).isEqualTo(42);
    }

    @Test
    @DisplayName("affecterEnfant - refuse sur une chambre équipe")
    void affecterEnfant_whenEquipeChambre_shouldThrow400() {
        Chambre chambre = chambreEquipe(4);
        when(chambreRepository.findById(4)).thenReturn(Optional.of(chambre));

        assertThatThrownBy(() -> chambreService.affecterEnfant(1, 4, 42, null, APPELANT_TOKEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("enfants");

        verify(chambreRepository, never()).save(any());
    }

    @Test
    @DisplayName("affecterEnfant - refuse si genre incompatible")
    void affecterEnfant_whenGenreIncompatible_shouldThrow400() {
        Chambre chambre = chambreEnfant(3);
        chambre.setGenreAutorise(GenreChambre.MASCULIN);
        Enfant enfant = enfant(42, Genre.Féminin);
        when(chambreRepository.findById(3)).thenReturn(Optional.of(chambre));
        when(sejourEnfantRepository.existsById(new SejourEnfantId(1, 42))).thenReturn(true);
        when(enfantRepository.findById(42)).thenReturn(Optional.of(enfant));

        assertThatThrownBy(() -> chambreService.affecterEnfant(1, 3, 42, null, APPELANT_TOKEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("genre");

        verify(chambreRepository, never()).save(any());
    }

    @Test
    @DisplayName("affecterEnfant - refuse si capacité atteinte")
    void affecterEnfant_whenCapaciteAtteinte_shouldThrow400() {
        Chambre chambre = chambreEnfant(3);
        chambre.setCapaciteMax(1);
        ChambreOccupant occupantExistant = new ChambreOccupant();
        occupantExistant.setId(100);
        occupantExistant.setChambre(chambre);
        occupantExistant.setEnfant(enfant(99, Genre.Féminin));
        chambre.setOccupants(new ArrayList<>(List.of(occupantExistant)));
        when(chambreRepository.findById(3)).thenReturn(Optional.of(chambre));
        when(chambreOccupantRepository.findByEnfantIdAndSejourId(42, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chambreService.affecterEnfant(1, 3, 42, null, APPELANT_TOKEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("capacité");

        verify(chambreRepository, never()).save(any());
    }

    @Test
    @DisplayName("affecterEnfant - refuse si non inscrit au séjour")
    void affecterEnfant_whenNotInscrit_shouldThrow400() {
        Chambre chambre = chambreEnfant(3);
        when(chambreRepository.findById(3)).thenReturn(Optional.of(chambre));
        when(sejourEnfantRepository.existsById(new SejourEnfantId(1, 42))).thenReturn(false);

        assertThatThrownBy(() -> chambreService.affecterEnfant(1, 3, 42, null, APPELANT_TOKEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inscrit");

        verify(enfantRepository, never()).findById(any());
    }

    @Test
    @DisplayName("retirerEnfant - retire l'occupant de la chambre")
    void retirerEnfant_success() {
        Chambre chambre = chambreEnfant(3);
        Enfant enfant = enfant(42, Genre.Féminin);
        ChambreOccupant occupant = new ChambreOccupant();
        occupant.setId(100);
        occupant.setChambre(chambre);
        occupant.setEnfant(enfant);
        chambre.setOccupants(new ArrayList<>(List.of(occupant)));
        when(chambreRepository.findById(3)).thenReturn(Optional.of(chambre));
        when(chambreRepository.save(chambre)).thenReturn(chambre);

        chambreService.retirerEnfant(1, 3, 42, APPELANT_TOKEN);

        assertThat(chambre.getOccupants()).isEmpty();
        verify(chambreOccupantRepository).delete(occupant);
    }

    @Test
    @DisplayName("affecterMembreEquipe - affecte un membre à une chambre équipe")
    void affecterMembreEquipe_success() {
        Chambre chambre = chambreEquipe(5);
        chambre.setOccupants(new ArrayList<>());
        Utilisateur membre = membreEquipe(8, "membre-token", Genre.Masculin);
        when(chambreRepository.findById(5)).thenReturn(Optional.of(chambre));
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(utilisateurRepository.findByTokenId("membre-token")).thenReturn(Optional.of(membre));
        when(sejourEquipeRepository.existsBySejour_IdAndUtilisateur_Id(1, 8)).thenReturn(true);
        when(chambreOccupantRepository.findByUtilisateurIdAndSejourId(8, 1)).thenReturn(Optional.empty());
        when(chambreRepository.save(chambre)).thenAnswer(inv -> {
            chambre.getOccupants().stream().filter(o -> o.getId() == null).forEach(o -> o.setId(201));
            return chambre;
        });
        when(chambreRepository.findByIdAndSejourIdWithOccupants(5, 1)).thenReturn(Optional.of(chambre));

        var dto = chambreService.affecterMembreEquipe(1, 5, "membre-token", null, APPELANT_TOKEN);

        assertThat(chambre.getOccupants()).hasSize(1);
        assertThat(chambre.getOccupants().getFirst().getUtilisateur().getTokenId()).isEqualTo("membre-token");
        assertThat(dto.occupants()).hasSize(1);
        assertThat(dto.occupants().getFirst().membreTokenId()).isEqualTo("membre-token");
    }

    @Test
    @DisplayName("affecterMembreEquipe - refuse si genre incompatible")
    void affecterMembreEquipe_whenGenreIncompatible_shouldThrow400() {
        Chambre chambre = chambreEquipe(5);
        chambre.setGenreAutorise(GenreChambre.MASCULIN);
        Utilisateur membre = membreEquipe(8, "membre-token", Genre.Féminin);
        when(chambreRepository.findById(5)).thenReturn(Optional.of(chambre));
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(utilisateurRepository.findByTokenId("membre-token")).thenReturn(Optional.of(membre));
        when(sejourEquipeRepository.existsBySejour_IdAndUtilisateur_Id(1, 8)).thenReturn(true);

        assertThatThrownBy(() -> chambreService.affecterMembreEquipe(1, 5, "membre-token", null, APPELANT_TOKEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("genre");

        verify(chambreRepository, never()).save(any());
    }

    @Test
    @DisplayName("affecterMembreEquipe - refuse sur une chambre enfant")
    void affecterMembreEquipe_whenEnfantChambre_shouldThrow400() {
        Chambre chambre = chambreEnfant(3);
        when(chambreRepository.findById(3)).thenReturn(Optional.of(chambre));

        assertThatThrownBy(() -> chambreService.affecterMembreEquipe(1, 3, "membre-token", null, APPELANT_TOKEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("équipe");

        verify(chambreRepository, never()).save(any());
    }

    @Test
    @DisplayName("modifierChambre - changement de type efface les occupants")
    void modifierChambre_switchType_clearsOccupants() {
        Chambre chambre = chambreEnfant(5);
        ChambreOccupant occupant = new ChambreOccupant();
        occupant.setId(50);
        occupant.setChambre(chambre);
        occupant.setEnfant(enfant(42, Genre.Féminin));
        chambre.setOccupants(new ArrayList<>(List.of(occupant)));
        when(chambreRepository.findById(5)).thenReturn(Optional.of(chambre));
        when(chambreRepository.existsBySejourIdAndIdentifiantIgnoreCaseAndIdNot(1, "101", 5)).thenReturn(false);
        when(chambreRepository.save(chambre)).thenReturn(chambre);

        chambreService.modifierChambre(1, 5,
                new SaveChambreRequest(
                        TypeChambre.EQUIPE, "101", null, 2, GenreChambre.MIXTE, null, null, null, null, null),
                APPELANT_TOKEN);

        assertThat(chambre.getOccupants()).isEmpty();
        verify(chambreRepository).save(chambre);
    }

    @Test
    @DisplayName("affecterEnfant - refuse un enfant hors du groupe lié à la chambre")
    void affecterEnfant_whenEnfantHorsGroupeChambre_shouldThrow400() {
        Chambre chambre = chambreEnfant(3);
        chambre.setOccupants(new ArrayList<>());
        Groupe groupe = new Groupe();
        groupe.setId(7);
        groupe.setNom("Les Aigles");
        groupe.setSejour(sejour);
        chambre.setGroupe(groupe);
        Enfant enfant = enfant(42, Genre.Féminin);
        when(chambreRepository.findById(3)).thenReturn(Optional.of(chambre));
        when(sejourEnfantRepository.existsById(new SejourEnfantId(1, 42))).thenReturn(true);
        when(enfantRepository.findById(42)).thenReturn(Optional.of(enfant));
        when(chambreOccupantRepository.findByEnfantIdAndSejourId(42, 1)).thenReturn(Optional.empty());
        when(groupeRepository.existsEnfantInGroupe(7, 42)).thenReturn(false);

        assertThatThrownBy(() -> chambreService.affecterEnfant(1, 3, 42, null, APPELANT_TOKEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("groupe");

        verify(chambreRepository, never()).save(any());
    }

    @Test
    @DisplayName("creerChambre - associe un groupe sur une chambre enfant")
    void creerChambre_withGroupe_success() {
        Groupe groupe = new Groupe();
        groupe.setId(7);
        groupe.setNom("Les Aigles");
        groupe.setSejour(sejour);
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(chambreRepository.existsBySejourIdAndIdentifiantIgnoreCase(1, "101")).thenReturn(false);
        when(groupeRepository.findByIdAndSejourId(7, 1)).thenReturn(Optional.of(groupe));
        when(chambreRepository.save(any(Chambre.class))).thenAnswer(inv -> {
            Chambre c = inv.getArgument(0);
            c.setId(10);
            return c;
        });

        var dto = chambreService.creerChambre(
                1,
                new SaveChambreRequest(
                        TypeChambre.ENFANT,
                        "101",
                        null,
                        4,
                        GenreChambre.MIXTE,
                        null,
                        null,
                        null,
                        null,
                        7),
                APPELANT_TOKEN);

        assertThat(dto.groupe()).isNotNull();
        assertThat(dto.groupe().id()).isEqualTo(7);
        assertThat(dto.groupe().libelle()).isEqualTo("Les Aigles");
    }

    @Test
    @DisplayName("affecterEnfants - affecte plusieurs enfants en une fois")
    void affecterEnfants_success() {
        Chambre chambre = chambreEnfant(3);
        chambre.setOccupants(new ArrayList<>());
        Enfant enfant1 = enfant(42, Genre.Féminin);
        Enfant enfant2 = enfant(43, Genre.Féminin);
        when(chambreRepository.findById(3)).thenReturn(Optional.of(chambre));
        when(sejourEnfantRepository.existsById(new SejourEnfantId(1, 42))).thenReturn(true);
        when(sejourEnfantRepository.existsById(new SejourEnfantId(1, 43))).thenReturn(true);
        when(enfantRepository.findById(42)).thenReturn(Optional.of(enfant1));
        when(enfantRepository.findById(43)).thenReturn(Optional.of(enfant2));
        when(chambreOccupantRepository.findByEnfantIdAndSejourId(42, 1)).thenReturn(Optional.empty());
        when(chambreOccupantRepository.findByEnfantIdAndSejourId(43, 1)).thenReturn(Optional.empty());
        when(chambreRepository.save(chambre)).thenAnswer(inv -> {
            chambre.getOccupants().stream().filter(o -> o.getId() == null).forEach(o -> o.setId(200));
            return chambre;
        });
        when(chambreRepository.findByIdAndSejourIdWithOccupants(3, 1)).thenReturn(Optional.of(chambre));

        var dto = chambreService.affecterEnfants(
                1,
                3,
                new AffecterOccupantsEnfantsRequest(
                        List.of(
                                new AffecterOccupantEnfantItemRequest(42, 1),
                                new AffecterOccupantEnfantItemRequest(43, 2))),
                APPELANT_TOKEN);

        assertThat(chambre.getOccupants()).hasSize(2);
        assertThat(dto.occupants()).hasSize(2);
    }

    @Test
    @DisplayName("affecterEnfants - refuse si capacité dépassée (transaction annulée)")
    void affecterEnfants_whenCapaciteDepassee_shouldThrow400() {
        Chambre chambre = chambreEnfant(3);
        chambre.setCapaciteMax(1);
        chambre.setOccupants(new ArrayList<>());
        when(chambreRepository.findById(3)).thenReturn(Optional.of(chambre));
        when(chambreOccupantRepository.findByEnfantIdAndSejourId(42, 1)).thenReturn(Optional.empty());
        when(chambreOccupantRepository.findByEnfantIdAndSejourId(43, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chambreService.affecterEnfants(
                        1,
                        3,
                        new AffecterOccupantsEnfantsRequest(
                                List.of(
                                        new AffecterOccupantEnfantItemRequest(42, null),
                                        new AffecterOccupantEnfantItemRequest(43, null))),
                APPELANT_TOKEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("capacité");

        verify(chambreRepository, never()).save(any());
    }

    @Test
    @DisplayName("affecterMembresEquipe - affecte plusieurs membres en une fois")
    void affecterMembresEquipe_success() {
        Chambre chambre = chambreEquipe(5);
        chambre.setOccupants(new ArrayList<>());
        Utilisateur membre1 = membreEquipe(8, "membre-1", Genre.Masculin);
        Utilisateur membre2 = membreEquipe(9, "membre-2", Genre.Féminin);
        when(chambreRepository.findById(5)).thenReturn(Optional.of(chambre));
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(utilisateurRepository.findByTokenId("membre-1")).thenReturn(Optional.of(membre1));
        when(utilisateurRepository.findByTokenId("membre-2")).thenReturn(Optional.of(membre2));
        when(sejourEquipeRepository.existsBySejour_IdAndUtilisateur_Id(1, 8)).thenReturn(true);
        when(sejourEquipeRepository.existsBySejour_IdAndUtilisateur_Id(1, 9)).thenReturn(true);
        when(chambreOccupantRepository.findByUtilisateurIdAndSejourId(8, 1)).thenReturn(Optional.empty());
        when(chambreOccupantRepository.findByUtilisateurIdAndSejourId(9, 1)).thenReturn(Optional.empty());
        when(chambreRepository.save(chambre)).thenAnswer(inv -> {
            chambre.getOccupants().stream().filter(o -> o.getId() == null).forEach(o -> o.setId(201));
            return chambre;
        });
        when(chambreRepository.findByIdAndSejourIdWithOccupants(5, 1)).thenReturn(Optional.of(chambre));

        var dto = chambreService.affecterMembresEquipe(
                1,
                5,
                new AffecterOccupantsEquipeRequest(
                        List.of(
                                new AffecterOccupantEquipeItemRequest("membre-1", 1),
                                new AffecterOccupantEquipeItemRequest("membre-2", 2))),
                APPELANT_TOKEN);

        assertThat(chambre.getOccupants()).hasSize(2);
        assertThat(dto.occupants()).hasSize(2);
    }

    @Test
    @DisplayName("affecterMembreEquipe - réaffecte un membre depuis une autre chambre")
    void affecterMembreEquipe_reaffectationFromOtherChambre_success() {
        Chambre ancienneChambre = chambreEquipe(3);
        Chambre nouvelleChambre = chambreEquipe(9);
        nouvelleChambre.setOccupants(new ArrayList<>());
        Utilisateur membre = membreEquipe(66, "membre-token", Genre.Masculin);
        ChambreOccupant occupantExistant = new ChambreOccupant();
        occupantExistant.setId(100);
        occupantExistant.setChambre(ancienneChambre);
        occupantExistant.setUtilisateur(membre);
        occupantExistant.setNumeroLit(1);
        ancienneChambre.setOccupants(new ArrayList<>(List.of(occupantExistant)));
        when(chambreRepository.findById(9)).thenReturn(Optional.of(nouvelleChambre));
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(utilisateurRepository.findByTokenId("membre-token")).thenReturn(Optional.of(membre));
        when(sejourEquipeRepository.existsBySejour_IdAndUtilisateur_Id(1, 66)).thenReturn(true);
        when(chambreOccupantRepository.findByUtilisateurIdAndSejourId(66, 1))
                .thenReturn(Optional.of(occupantExistant));
        when(chambreRepository.save(nouvelleChambre)).thenAnswer(inv -> {
            nouvelleChambre.getOccupants().stream().filter(o -> o.getId() == null).forEach(o -> o.setId(201));
            return nouvelleChambre;
        });
        when(chambreRepository.findByIdAndSejourIdWithOccupants(9, 1)).thenReturn(Optional.of(nouvelleChambre));

        var dto = chambreService.affecterMembreEquipe(1, 9, "membre-token", null, APPELANT_TOKEN);

        assertThat(ancienneChambre.getOccupants()).isEmpty();
        assertThat(nouvelleChambre.getOccupants()).hasSize(1);
        assertThat(nouvelleChambre.getOccupants().getFirst().getUtilisateur().getId()).isEqualTo(66);
        assertThat(dto.occupants()).hasSize(1);
        assertThat(dto.occupants().getFirst().membreTokenId()).isEqualTo("membre-token");
        var inOrder = inOrder(chambreOccupantRepository);
        inOrder.verify(chambreOccupantRepository).delete(occupantExistant);
        inOrder.verify(chambreOccupantRepository).flush();
    }

    @Test
    @DisplayName("affecterEnfant - réaffecte un enfant depuis une autre chambre")
    void affecterEnfant_reaffectationFromOtherChambre_success() {
        Chambre ancienneChambre = chambreEnfant(3);
        Chambre nouvelleChambre = chambreEnfant(9);
        nouvelleChambre.setOccupants(new ArrayList<>());
        Enfant enfant = enfant(42, Genre.Féminin);
        ChambreOccupant occupantExistant = new ChambreOccupant();
        occupantExistant.setId(100);
        occupantExistant.setChambre(ancienneChambre);
        occupantExistant.setEnfant(enfant);
        occupantExistant.setNumeroLit(1);
        ancienneChambre.setOccupants(new ArrayList<>(List.of(occupantExistant)));
        when(chambreRepository.findById(9)).thenReturn(Optional.of(nouvelleChambre));
        when(sejourEnfantRepository.existsById(new SejourEnfantId(1, 42))).thenReturn(true);
        when(enfantRepository.findById(42)).thenReturn(Optional.of(enfant));
        when(chambreOccupantRepository.findByEnfantIdAndSejourId(42, 1)).thenReturn(Optional.of(occupantExistant));
        when(chambreRepository.save(nouvelleChambre)).thenAnswer(inv -> {
            nouvelleChambre.getOccupants().stream().filter(o -> o.getId() == null).forEach(o -> o.setId(201));
            return nouvelleChambre;
        });
        when(chambreRepository.findByIdAndSejourIdWithOccupants(9, 1)).thenReturn(Optional.of(nouvelleChambre));

        var dto = chambreService.affecterEnfant(1, 9, 42, null, APPELANT_TOKEN);

        assertThat(ancienneChambre.getOccupants()).isEmpty();
        assertThat(nouvelleChambre.getOccupants()).hasSize(1);
        assertThat(nouvelleChambre.getOccupants().getFirst().getEnfant().getId()).isEqualTo(42);
        assertThat(dto.occupants()).hasSize(1);
        assertThat(dto.occupants().getFirst().enfantId()).isEqualTo(42);
        var inOrder = inOrder(chambreOccupantRepository);
        inOrder.verify(chambreOccupantRepository).delete(occupantExistant);
        inOrder.verify(chambreOccupantRepository).flush();
    }

    private static Enfant enfant(int id, Genre genre) {
        Enfant enfant = new Enfant();
        enfant.setId(id);
        enfant.setNom("Dupont");
        enfant.setPrenom("Alice");
        enfant.setGenre(genre);
        return enfant;
    }

    private static Utilisateur membreEquipe(int id, String tokenId, Genre genre) {
        return Utilisateur.builder()
                .id(id)
                .tokenId(tokenId)
                .nom("Martin")
                .prenom("Bob")
                .genre(genre)
                .build();
    }

    private Chambre chambreEnfant(int id) {
        Chambre chambre = new Chambre();
        chambre.setId(id);
        chambre.setTypeChambre(TypeChambre.ENFANT);
        chambre.setIdentifiant("101");
        chambre.setCapaciteMax(4);
        chambre.setGenreAutorise(GenreChambre.MIXTE);
        chambre.setSejour(sejour);
        return chambre;
    }

    private Chambre chambreEquipe(int id) {
        Chambre chambre = new Chambre();
        chambre.setId(id);
        chambre.setTypeChambre(TypeChambre.EQUIPE);
        chambre.setIdentifiant("Foyer");
        chambre.setCapaciteMax(2);
        chambre.setGenreAutorise(GenreChambre.MIXTE);
        chambre.setSejour(sejour);
        chambre.setReferents(new ArrayList<>());
        return chambre;
    }
}
