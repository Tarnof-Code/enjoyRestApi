package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.payload.response.SejourDto;
import com.tarnof.enjoyrestapi.entities.RefreshToken;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.SejourEquipe;
import com.tarnof.enjoyrestapi.entities.SejourEquipeId;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.enums.RoleSejour;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.payload.request.CreateSejourRequest;
import com.tarnof.enjoyrestapi.payload.request.MembreEquipeRequest;
import com.tarnof.enjoyrestapi.repositories.RefreshTokenRepository;
import com.tarnof.enjoyrestapi.repositories.SejourEquipeRepository;
import com.tarnof.enjoyrestapi.repositories.SejourRepository;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.services.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour SejourServiceImpl")
class SejourServiceImplTest {

    @Mock
    private SejourRepository sejourRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private SejourEquipeRepository sejourEquipeRepository;

    @InjectMocks
    private SejourServiceImpl sejourService;

    private Sejour sejour;
    private Utilisateur directeur;
    private CreateSejourRequest createRequest;
    private Date dateDebut;
    private Date dateFin;

    @BeforeEach
    void setUp() {
        // Dates de test
        dateDebut = new Date(System.currentTimeMillis() + 86400000); // Demain
        dateFin = new Date(System.currentTimeMillis() + 172800000); // Après-demain

        // Directeur de test
        directeur = Utilisateur.builder()
                .id(1)
                .tokenId("directeur-token-123")
                .nom("Dupont")
                .prenom("Jean")
                .role(Role.DIRECTION)
                .email("jean.dupont@test.fr")
                .build();

        // Séjour de test
        sejour = Sejour.builder()
                .id(1)
                .nom("Séjour Test")
                .description("Description du séjour test")
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .lieuDuSejour("Lieu Test")
                .directeur(directeur)
                .equipeRoles(new ArrayList<>())
                .build();

        // Request de création
        createRequest = new CreateSejourRequest(
                "Nouveau Séjour",
                "Description du nouveau séjour",
                dateDebut,
                dateFin,
                "Nouveau Lieu",
                "directeur-token-123"
        );
    }

    @Test
    @DisplayName("getAllSejours - Devrait retourner une liste de tous les séjours")
    void getAllSejours_ShouldReturnListOfSejours() {
        // Given
        List<Sejour> sejours = Arrays.asList(sejour, Sejour.builder()
                .id(2)
                .nom("Séjour 2")
                .description("Description 2")
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .lieuDuSejour("Lieu 2")
                .directeur(directeur)
                .build());
        when(sejourRepository.findAll()).thenReturn(sejours);

        // When
        List<SejourDto> result = sejourService.getAllSejours();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).nom()).isEqualTo("Séjour Test");
        verify(sejourRepository).findAll();
    }

    @Test
    @DisplayName("getAllSejours - Devrait retourner une liste vide si aucun séjour")
    void getAllSejours_WhenNoSejours_ShouldReturnEmptyList() {
        // Given
        when(sejourRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<SejourDto> result = sejourService.getAllSejours();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(sejourRepository).findAll();
    }

    @Test
    @DisplayName("getSejourById - Devrait retourner un séjour existant avec directeur")
    void getSejourById_WhenSejourExistsWithDirecteur_ShouldReturnSejourResponse() {
        // Given
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));

        // When
        SejourDto result = sejourService.getSejourById(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1);
        assertThat(result.nom()).isEqualTo("Séjour Test");
        assertThat(result.description()).isEqualTo("Description du séjour test");
        assertThat(result.directeur()).isNotNull();
        assertThat(result.directeur().tokenId()).isEqualTo("directeur-token-123");
        verify(sejourRepository).findById(1);
    }

    @Test
    @DisplayName("getSejourById - Devrait retourner un séjour existant sans directeur")
    void getSejourById_WhenSejourExistsWithoutDirecteur_ShouldReturnSejourResponse() {
        // Given - séjour SANS directeur
        Sejour sejourSansDirecteur = Sejour.builder()
                .id(2)
                .nom("Séjour Sans Directeur")
                .description("Description du séjour sans directeur")
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .lieuDuSejour("Lieu Test")
                .directeur(null)
                .equipeRoles(new ArrayList<>())
                .build();

        when(sejourRepository.findById(2)).thenReturn(Optional.of(sejourSansDirecteur));

        // When
        SejourDto result = sejourService.getSejourById(2);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2);
        assertThat(result.nom()).isEqualTo("Séjour Sans Directeur");
        assertThat(result.description()).isEqualTo("Description du séjour sans directeur");
        assertThat(result.directeur()).isNull();
        verify(sejourRepository).findById(2);
    }

    @Test
    @DisplayName("getSejourById - Devrait lancer une exception si le séjour n'existe pas")
    void getSejourById_WhenSejourNotFound_ShouldThrowException() {
        // Given
        int id = 999;
        when(sejourRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sejourService.getSejourById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Séjour non trouvé avec l'ID: " + id);
        verify(sejourRepository).findById(id);
    }

    @Test
    @DisplayName("creerSejour - Devrait créer un nouveau séjour avec directeur")
    @SuppressWarnings("null")
    void creerSejour_WithValidRequestAndDirecteur_ShouldCreateSejour() {
        // Given
        when(utilisateurRepository.findByTokenId("directeur-token-123"))
                .thenReturn(Optional.of(directeur));
        when(sejourRepository.save(any(Sejour.class))).thenReturn(sejour);

        // When
        SejourDto result = sejourService.creerSejour(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.nom()).isEqualTo("Séjour Test");
        verify(utilisateurRepository).findByTokenId("directeur-token-123");
        verify(sejourRepository).save(any(Sejour.class));
    }

    @Test
    @DisplayName("creerSejour - Devrait créer un nouveau séjour sans directeur")
    @SuppressWarnings("null")
    void creerSejour_WithValidRequestWithoutDirecteur_ShouldCreateSejour() {
        // Given - requête sans directeur
        CreateSejourRequest requestSansDirecteur = new CreateSejourRequest(
                "Séjour Sans Directeur",
                "Description du séjour sans directeur",
                dateDebut,
                dateFin,
                "Lieu Test",
                null  // Pas de directeur
        );

        Sejour sejourSansDirecteur = Sejour.builder()
                .id(2)
                .nom("Séjour Sans Directeur")
                .description("Description du séjour sans directeur")
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .lieuDuSejour("Lieu Test")
                .directeur(null)
                .equipeRoles(new ArrayList<>())
                .build();

        when(sejourRepository.save(any(Sejour.class))).thenReturn(sejourSansDirecteur);

        // When
        SejourDto result = sejourService.creerSejour(requestSansDirecteur);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.nom()).isEqualTo("Séjour Sans Directeur");
        assertThat(result.directeur()).isNull();
        verify(utilisateurRepository, never()).findByTokenId(anyString());
        verify(sejourRepository).save(any(Sejour.class));
    }

    @Test
    @DisplayName("creerSejour - Devrait lancer une exception si le directeur n'existe pas")
    @SuppressWarnings("null")
    void creerSejour_WhenDirecteurNotFound_ShouldThrowException() {
        // Given
        when(utilisateurRepository.findByTokenId("directeur-token-123"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sejourService.creerSejour(createRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Directeur non trouvé avec l'ID: directeur-token-123");
        verify(utilisateurRepository).findByTokenId("directeur-token-123");
        verify(sejourRepository, never()).save(any(Sejour.class));
    }

    @Test
    @DisplayName("modifierSejour - Devrait modifier un séjour existant avec succès")
    @SuppressWarnings("null")
    void modifierSejour_WithValidRequest_ShouldUpdateSejour() {
        // Given
        CreateSejourRequest updateRequest = new CreateSejourRequest(
                "Séjour Modifié",
                "Description modifiée",
                dateDebut,
                dateFin,
                "Lieu Modifié",
                "directeur-token-123"
        );

        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(utilisateurRepository.findByTokenId("directeur-token-123"))
                .thenReturn(Optional.of(directeur));
        when(sejourRepository.save(any(Sejour.class))).thenAnswer(invocation -> (Sejour) invocation.getArgument(0));

        // When
        SejourDto result = sejourService.modifierSejour(1, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.nom()).isEqualTo("Séjour Modifié");
        verify(sejourRepository).findById(1);
        verify(utilisateurRepository).findByTokenId("directeur-token-123");
        verify(sejourRepository).save(any(Sejour.class));
    }

    @Test
    @DisplayName("modifierSejour - Devrait lancer une exception si le séjour n'existe pas")
    @SuppressWarnings("null")
    void modifierSejour_WhenSejourNotFound_ShouldThrowException() {
        // Given
        when(sejourRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sejourService.modifierSejour(999, createRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Séjour non trouvé avec l'ID: 999");
        verify(sejourRepository).findById(999);
        verify(sejourRepository, never()).save(any(Sejour.class));
    }

    @Test
    @DisplayName("modifierSejour - Devrait lancer une exception si le directeur n'existe pas")
    @SuppressWarnings("null")
    void modifierSejour_WhenDirecteurNotFound_ShouldThrowException() {
        // Given
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(utilisateurRepository.findByTokenId("directeur-token-123"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sejourService.modifierSejour(1, createRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Directeur non trouvé avec l'ID: directeur-token-123");
        verify(sejourRepository).findById(1);
        verify(utilisateurRepository).findByTokenId("directeur-token-123");
        verify(sejourRepository, never()).save(any(Sejour.class));
    }

    @Test
    @DisplayName("ajouterMembreEquipe - Devrait ajouter un membre existant avec succès")
    @SuppressWarnings("null")
    void ajouterMembreEquipe_WithExistingMember_ShouldAddMember() {
        // Given
        Utilisateur membre = Utilisateur.builder()
                .id(2)
                .tokenId("membre-token-456")
                .nom("Martin")
                .prenom("Pierre")
                .role(Role.BASIC_USER)
                .sejoursEquipe(new ArrayList<>())
                .build();

        MembreEquipeRequest membreRequest = new MembreEquipeRequest(
                "membre-token-456",
                RoleSejour.ANIM
        );

        RefreshToken refreshToken = RefreshToken.builder()
                .expiryDate(Instant.now().plusSeconds(86400))
                .build();
        membre.setRefreshToken(refreshToken);

        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(utilisateurRepository.findByTokenId("membre-token-456"))
                .thenReturn(Optional.of(membre));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sejourRepository.save(any(Sejour.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        sejourService.ajouterMembreEquipe(1, null, membreRequest);

        // Then
        verify(sejourRepository).findById(1);
        verify(utilisateurRepository).findByTokenId("membre-token-456");
        verify(sejourRepository).save(any(Sejour.class));
    }

    @Test
    @DisplayName("ajouterMembreEquipe - Devrait lancer une exception si aucune requête n'est fournie")
    void ajouterMembreEquipe_WhenNoRequest_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> sejourService.ajouterMembreEquipe(1, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Une requête d'ajout ou d'inscription est requise");
        verify(sejourRepository, never()).findById(anyInt());
    }

    @Test
    @DisplayName("ajouterMembreEquipe - Devrait lancer une exception si le séjour n'existe pas")
    void ajouterMembreEquipe_WhenSejourNotFound_ShouldThrowException() {
        // Given
        MembreEquipeRequest membreRequest = new MembreEquipeRequest(
                "membre-token-456",
                RoleSejour.ANIM
        );

        when(sejourRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sejourService.ajouterMembreEquipe(999, null, membreRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Séjour non trouvé avec l'ID: 999");
        verify(sejourRepository).findById(999);
    }

    @Test
    @DisplayName("ajouterMembreEquipe - Devrait lancer une exception si le membre existe déjà dans l'équipe")
    @SuppressWarnings("null")
    void ajouterMembreEquipe_WhenMemberAlreadyExists_ShouldThrowException() {
        // Given
        Utilisateur membre = Utilisateur.builder()
                .id(2)
                .tokenId("membre-token-456")
                .nom("Martin")
                .prenom("Pierre")
                .role(Role.BASIC_USER)
                .build();

        SejourEquipe sejourEquipe = SejourEquipe.builder()
                .sejour(sejour)
                .utilisateur(membre)
                .roleSejour(RoleSejour.ANIM)
                .build();

        sejour.setEquipeRoles(Arrays.asList(sejourEquipe));

        MembreEquipeRequest membreRequest = new MembreEquipeRequest(
                "membre-token-456",
                RoleSejour.ANIM
        );

        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(utilisateurRepository.findByTokenId("membre-token-456"))
                .thenReturn(Optional.of(membre));

        // When & Then
        assertThatThrownBy(() -> sejourService.ajouterMembreEquipe(1, null, membreRequest))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("Cet utilisateur fait déjà partie de l'équipe");
        verify(sejourRepository).findById(1);
        verify(utilisateurRepository).findByTokenId("membre-token-456");
        verify(sejourRepository, never()).save(any(Sejour.class));
    }

    @Test
    @DisplayName("modifierRoleMembreEquipe - Devrait modifier le rôle d'un membre avec succès")
    @SuppressWarnings("null")
    void modifierRoleMembreEquipe_WithValidData_ShouldUpdateRole() {
        // Given
        Utilisateur membre = Utilisateur.builder()
                .id(2)
                .tokenId("membre-token-456")
                .nom("Martin")
                .prenom("Pierre")
                .role(Role.BASIC_USER)
                .build();

        SejourEquipe sejourEquipe = SejourEquipe.builder()
                .sejour(sejour)
                .utilisateur(membre)
                .roleSejour(RoleSejour.ANIM)
                .build();

        sejour.setEquipeRoles(Arrays.asList(sejourEquipe));

        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(utilisateurRepository.findByTokenId("membre-token-456"))
                .thenReturn(Optional.of(membre));
        when(sejourRepository.save(any(Sejour.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        sejourService.modifierRoleMembreEquipe(1, "membre-token-456", RoleSejour.AS);

        // Then
        assertThat(sejourEquipe.getRoleSejour()).isEqualTo(RoleSejour.AS);
        verify(sejourRepository).findById(1);
        verify(utilisateurRepository).findByTokenId("membre-token-456");
        verify(sejourRepository).save(any(Sejour.class));
    }

    @Test
    @DisplayName("modifierRoleMembreEquipe - Devrait lancer une exception si le séjour n'existe pas")
    void modifierRoleMembreEquipe_WhenSejourNotFound_ShouldThrowException() {
        // Given
        when(sejourRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sejourService.modifierRoleMembreEquipe(999, "membre-token-456", RoleSejour.AS))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Séjour non trouvé avec l'ID: 999");
        verify(sejourRepository).findById(999);
    }

    @Test
    @DisplayName("modifierRoleMembreEquipe - Devrait lancer une exception si le membre n'est pas dans l'équipe")
    @SuppressWarnings("null")
    void modifierRoleMembreEquipe_WhenMemberNotInTeam_ShouldThrowException() {
        // Given
        Utilisateur membre = Utilisateur.builder()
                .id(2)
                .tokenId("membre-token-456")
                .build();

        sejour.setEquipeRoles(Collections.emptyList());

        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        when(utilisateurRepository.findByTokenId("membre-token-456"))
                .thenReturn(Optional.of(membre));

        // When & Then
        assertThatThrownBy(() -> sejourService.modifierRoleMembreEquipe(1, "membre-token-456", RoleSejour.AS))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Le membre ne fait pas partie de l'équipe de ce séjour");
        verify(sejourRepository).findById(1);
        verify(utilisateurRepository).findByTokenId("membre-token-456");
        verify(sejourRepository, never()).save(any(Sejour.class));
    }

    @Test
    @DisplayName("supprimerMembreEquipe - Devrait supprimer un membre de l'équipe avec succès")
    @SuppressWarnings("null")
    void supprimerMembreEquipe_WithValidData_ShouldRemoveMember() {
        // Given
        Utilisateur membre = Utilisateur.builder()
                .id(2)
                .tokenId("membre-token-456")
                .nom("Martin")
                .prenom("Pierre")
                .role(Role.BASIC_USER)
                .sejoursEquipe(new ArrayList<>())
                .build();

        SejourEquipeId sejourEquipeId = new SejourEquipeId(1, 2);

        when(utilisateurRepository.findByTokenId("membre-token-456"))
                .thenReturn(Optional.of(membre));
        when(sejourEquipeRepository.existsById(sejourEquipeId)).thenReturn(true);
        doNothing().when(sejourEquipeRepository).deleteById(sejourEquipeId);
        doNothing().when(sejourEquipeRepository).flush();
        when(utilisateurRepository.findByTokenId("membre-token-456"))
                .thenReturn(Optional.of(membre));

        // When
        sejourService.supprimerMembreEquipe(1, "membre-token-456");

        // Then
        verify(utilisateurRepository, times(2)).findByTokenId("membre-token-456");
        verify(sejourEquipeRepository).existsById(eq(sejourEquipeId));
        verify(sejourEquipeRepository).deleteById(eq(sejourEquipeId));
        verify(sejourEquipeRepository).flush();
    }

    @Test
    @DisplayName("supprimerMembreEquipe - Devrait lancer une exception si le membre n'existe pas")
    @SuppressWarnings("null")
    void supprimerMembreEquipe_WhenMemberNotFound_ShouldThrowException() {
        // Given
        when(utilisateurRepository.findByTokenId("membre-token-456"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sejourService.supprimerMembreEquipe(999, "membre-token-456"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Membre non trouvé avec l'ID: membre-token-456");
        verify(utilisateurRepository).findByTokenId("membre-token-456");
        verify(sejourEquipeRepository, never()).existsById(any());
    }

    @Test
    @DisplayName("supprimerMembreEquipe - Devrait lancer une exception si le membre n'est pas dans l'équipe")
    @SuppressWarnings("null")
    void supprimerMembreEquipe_WhenMemberNotInTeam_ShouldThrowException() {
        // Given
        Utilisateur membre = Utilisateur.builder()
                .id(2)
                .tokenId("membre-token-456")
                .nom("Martin")
                .prenom("Pierre")
                .role(Role.BASIC_USER)
                .build();

        SejourEquipeId sejourEquipeId = new SejourEquipeId(1, 2);

        when(utilisateurRepository.findByTokenId("membre-token-456"))
                .thenReturn(Optional.of(membre));
        when(sejourEquipeRepository.existsById(sejourEquipeId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> sejourService.supprimerMembreEquipe(1, "membre-token-456"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Le membre ne fait pas partie de l'équipe de ce séjour");
        verify(utilisateurRepository).findByTokenId("membre-token-456");
        verify(sejourEquipeRepository).existsById(eq(sejourEquipeId));
        verify(sejourEquipeRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("supprimerSejour - Devrait supprimer un séjour existant avec succès")
    void supprimerSejour_WhenSejourExists_ShouldDeleteSejour() {
        // Given
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));
        doNothing().when(sejourRepository).deleteById(1);

        // When
        sejourService.supprimerSejour(1);

        // Then
        verify(sejourRepository).findById(1);
        verify(sejourRepository).deleteById(1);
    }

    @Test
    @DisplayName("supprimerSejour - Devrait lancer une exception si le séjour n'existe pas")
    void supprimerSejour_WhenSejourNotFound_ShouldThrowException() {
        // Given
        when(sejourRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sejourService.supprimerSejour(999))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Séjour non trouvé avec l'ID: 999");
        verify(sejourRepository).findById(999);
        verify(sejourRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("getSejoursByDirecteur - Devrait retourner les séjours d'un directeur")
    void getSejoursByDirecteur_WithValidDirecteur_ShouldReturnSejours() {
        // Given
        List<Sejour> sejours = Arrays.asList(sejour);
        when(utilisateurRepository.findByTokenId("directeur-token-123"))
                .thenReturn(Optional.of(directeur));
        when(sejourRepository.findByDirecteur(directeur)).thenReturn(sejours);

        // When
        List<SejourDto> result = sejourService.getSejoursByDirecteur("directeur-token-123");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).nom()).isEqualTo("Séjour Test");
        verify(utilisateurRepository).findByTokenId("directeur-token-123");
        verify(sejourRepository).findByDirecteur(directeur);
    }

    @Test
    @DisplayName("getSejoursByDirecteur - Devrait lancer une exception si le directeur n'existe pas")
    void getSejoursByDirecteur_WhenDirecteurNotFound_ShouldThrowException() {
        // Given
        when(utilisateurRepository.findByTokenId("directeur-inexistant"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sejourService.getSejoursByDirecteur("directeur-inexistant"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Directeur non trouvé avec le token ID: directeur-inexistant");
        verify(utilisateurRepository).findByTokenId("directeur-inexistant");
        verify(sejourRepository, never()).findByDirecteur(any(Utilisateur.class));
    }
}

