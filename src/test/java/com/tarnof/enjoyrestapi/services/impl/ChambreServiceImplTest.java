package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.Chambre;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.GenreChambre;
import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.enums.TypeChambre;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.AjouterReferentRequest;
import com.tarnof.enjoyrestapi.payload.request.SaveChambreRequest;
import com.tarnof.enjoyrestapi.repositories.ChambreRepository;
import com.tarnof.enjoyrestapi.repositories.SejourEquipeRepository;
import com.tarnof.enjoyrestapi.repositories.SejourRepository;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChambreServiceImpl")
@SuppressWarnings("null")
class ChambreServiceImplTest {

    @Mock
    private ChambreRepository chambreRepository;
    @Mock
    private SejourRepository sejourRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private SejourEquipeRepository sejourEquipeRepository;

    private ChambreServiceImpl chambreService;

    private Sejour sejour;
    private Utilisateur appelantAdmin;

    @BeforeEach
    void setUp() {
        chambreService = new ChambreServiceImpl(
                chambreRepository,
                new SejourVerificationService(sejourRepository, utilisateurRepository, sejourEquipeRepository),
                utilisateurRepository);
        sejour = new Sejour();
        sejour.setId(1);
        appelantAdmin = Utilisateur.builder()
                .id(99)
                .tokenId("appelant-token")
                .role(Role.ADMIN)
                .build();
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
                1);
    }

    @Test
    @DisplayName("creerChambre - refuse un doublon d'identifiant (même séjour, casse différente)")
    void creerChambre_whenDuplicateIdentifiant_shouldThrow409() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(chambreRepository.existsBySejourIdAndIdentifiantIgnoreCase(1, "101")).thenReturn(true);

        assertThatThrownBy(() -> chambreService.creerChambre(1,
                new SaveChambreRequest(
                        TypeChambre.ENFANT, "  101  ", null, 4, GenreChambre.MIXTE, null, null, null, null)))
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

        var dto = chambreService.creerChambre(1, requestEnfant());

        assertThat(dto.id()).isEqualTo(10);
        assertThat(dto.identifiant()).isEqualTo("101");
        assertThat(dto.nom()).isEqualTo("Les copains");
        assertThat(dto.typeChambre()).isEqualTo(TypeChambre.ENFANT);
        assertThat(dto.batiment()).isEqualTo("Bâtiment A");
        assertThat(dto.referents()).isEmpty();
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
                        TypeChambre.EQUIPE, "Foyer", null, 2, GenreChambre.MIXTE, null, null, null, null));

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
                        TypeChambre.ENFANT, "Autre", null, 3, GenreChambre.MASCULIN, null, null, null, null)))
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
                        TypeChambre.EQUIPE, "101", null, 2, GenreChambre.MIXTE, null, null, null, null));

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
                        TypeChambre.ENFANT, "101", null, 4, GenreChambre.MIXTE, null, null, null, null));

        verify(chambreRepository).save(chambre);
        assertThat(chambre.getIdentifiant()).isEqualTo("101");
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
        when(utilisateurRepository.findByTokenId("appelant-token")).thenReturn(Optional.of(appelantAdmin));
        when(chambreRepository.findBySejourIdOrderAffichage(1)).thenReturn(List.of());

        assertThat(chambreService.listerChambresDuSejour(1, "appelant-token")).isEmpty();
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

        chambreService.ajouterReferent(1, 3, new AjouterReferentRequest("ref-token"));

        assertThat(chambre.getReferents()).hasSize(1);
        verify(chambreRepository).save(chambre);
    }

    @Test
    @DisplayName("ajouterReferent - refuse sur une chambre équipe")
    void ajouterReferent_whenEquipe_shouldThrow400() {
        Chambre chambre = chambreEquipe(4);
        when(chambreRepository.findById(4)).thenReturn(Optional.of(chambre));

        assertThatThrownBy(() -> chambreService.ajouterReferent(1, 4, new AjouterReferentRequest("ref-token")))
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

        assertThatThrownBy(() -> chambreService.ajouterReferent(1, 3, new AjouterReferentRequest("ref-token")))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(chambreRepository, never()).save(any());
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
