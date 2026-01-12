package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.RefreshToken;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.exceptions.EmailDejaUtiliseException;
import com.tarnof.enjoyrestapi.exceptions.UtilisateurException;
import com.tarnof.enjoyrestapi.payload.request.UpdateUserRequest;
import com.tarnof.enjoyrestapi.payload.response.ProfilDto;
import com.tarnof.enjoyrestapi.repositories.RefreshTokenRepository;
import com.tarnof.enjoyrestapi.repositories.SejourRepository;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour UtilisateurServiceImpl")
public class UtilisateurServiceImplTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private SejourRepository sejourRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    
    @InjectMocks
    private UtilisateurServiceImpl utilisateurService;

    private Utilisateur utilisateur;
    private Date dateNaissance;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        dateNaissance = new Date(System.currentTimeMillis() - 86400000L * 365 * 25); // Il y a 25 ans
        
        refreshToken = RefreshToken.builder()
                .id(1L)
                .token("refresh-token-123")
                .expiryDate(Instant.now().plusSeconds(86400))
                .build();

        utilisateur = Utilisateur.builder()
                .id(1)
                .tokenId("user-token-123")
                .nom("Dupont")
                .prenom("Jean")
                .role(Role.BASIC_USER)
                .email("jean.dupont@test.fr")
                .telephone("0123456789")
                .genre("Homme")
                .dateNaissance(dateNaissance)
                .motDePasse("MotDePasse123!")
                .refreshToken(refreshToken)
                .sejoursEquipe(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("creerUtilisateur - Devrait créer un utilisateur avec succès")
    @SuppressWarnings("null")
    void creerUtilisateur_WithValidData_ShouldCreateUser() {
        // Given
        when(utilisateurRepository.existsByEmail(utilisateur.getEmail())).thenReturn(false);
        when(utilisateurRepository.existsByTelephone(utilisateur.getTelephone())).thenReturn(false);
        when(bCryptPasswordEncoder.encode(utilisateur.getMotDePasse())).thenReturn("encoded-password");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> {
            Utilisateur u = invocation.getArgument(0);
            u.setMotDePasse("encoded-password");
            return u;
        });

        // When
        Utilisateur result = utilisateurService.creerUtilisateur(utilisateur);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMotDePasse()).isEqualTo("encoded-password");
        verify(utilisateurRepository).existsByEmail(utilisateur.getEmail());
        verify(utilisateurRepository).existsByTelephone(utilisateur.getTelephone());
        verify(bCryptPasswordEncoder).encode("MotDePasse123!");
        verify(utilisateurRepository).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("creerUtilisateur - Devrait lancer une exception si l'email est déjà utilisé")
    @SuppressWarnings("null")
    void creerUtilisateur_WhenEmailExists_ShouldThrowException() {
        // Given
        when(utilisateurRepository.existsByEmail(utilisateur.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> utilisateurService.creerUtilisateur(utilisateur))
                .isInstanceOf(EmailDejaUtiliseException.class)
                .hasMessageContaining("L'email ou le numéro de téléphone est déjà utilisé");
        verify(utilisateurRepository).existsByEmail(utilisateur.getEmail());
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("creerUtilisateur - Devrait lancer une exception si le téléphone est déjà utilisé")
    @SuppressWarnings("null")
    void creerUtilisateur_WhenTelephoneExists_ShouldThrowException() {
        // Given
        when(utilisateurRepository.existsByEmail(utilisateur.getEmail())).thenReturn(false);
        when(utilisateurRepository.existsByTelephone(utilisateur.getTelephone())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> utilisateurService.creerUtilisateur(utilisateur))
                .isInstanceOf(EmailDejaUtiliseException.class)
                .hasMessageContaining("L'email ou le numéro de téléphone est déjà utilisé");
        verify(utilisateurRepository).existsByEmail(utilisateur.getEmail());
        verify(utilisateurRepository).existsByTelephone(utilisateur.getTelephone());
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("creerUtilisateur - Devrait retourner null si une exception survient lors de la sauvegarde")
    @SuppressWarnings("null")
    void creerUtilisateur_WhenExceptionOccurs_ShouldReturnNull() {
        // Given
        when(utilisateurRepository.existsByEmail(utilisateur.getEmail())).thenReturn(false);
        when(utilisateurRepository.existsByTelephone(utilisateur.getTelephone())).thenReturn(false);
        when(bCryptPasswordEncoder.encode(utilisateur.getMotDePasse())).thenReturn("encoded-password");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenThrow(new RuntimeException("Erreur de sauvegarde"));

        // When
        Utilisateur result = utilisateurService.creerUtilisateur(utilisateur);

        // Then
        assertThat(result).isNull();
        verify(utilisateurRepository).existsByEmail(utilisateur.getEmail());
        verify(utilisateurRepository).existsByTelephone(utilisateur.getTelephone());
        verify(bCryptPasswordEncoder).encode("MotDePasse123!");
        verify(utilisateurRepository).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("getAllUtilisateursDTO - Devrait retourner une liste de tous les utilisateurs")
    void getAllUtilisateursDTO_ShouldReturnListOfUsers() {
        // Given
        Utilisateur utilisateur2 = Utilisateur.builder()
                .id(2)
                .tokenId("user-token-456")
                .nom("Martin")
                .prenom("Pierre")
                .role(Role.ADMIN)
                .email("pierre.martin@test.fr")
                .telephone("0987654321")
                .genre("Homme")
                .dateNaissance(dateNaissance)
                .build();

        List<Utilisateur> utilisateurs = Arrays.asList(utilisateur, utilisateur2);
        when(utilisateurRepository.findAll()).thenReturn(utilisateurs);

        // When
        List<ProfilDto> result = utilisateurService.getAllUtilisateursDTO();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).nom()).isEqualTo("Dupont");
        assertThat(result.get(1).nom()).isEqualTo("Martin");
        verify(utilisateurRepository).findAll();
    }

    @Test
    @DisplayName("getAllUtilisateursDTO - Devrait retourner une liste vide si aucun utilisateur")
    void getAllUtilisateursDTO_WhenNoUsers_ShouldReturnEmptyList() {
        // Given
        when(utilisateurRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<ProfilDto> result = utilisateurService.getAllUtilisateursDTO();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(utilisateurRepository).findAll();
    }

    @Test
    @DisplayName("getAllUtilisateursDTO - Devrait retourner null si une exception survient")
    void getAllUtilisateursDTO_WhenExceptionOccurs_ShouldReturnNull() {
        // Given
        when(utilisateurRepository.findAll()).thenThrow(new RuntimeException("Erreur de récupération"));

        // When
        List<ProfilDto> result = utilisateurService.getAllUtilisateursDTO();

        // Then
        assertThat(result).isNull();
        verify(utilisateurRepository).findAll();
    }

    @Test
    @DisplayName("getUtilisateursByRole - Devrait retourner une liste d'utilisateurs par rôle")
    void getUtilisateursByRole_ShouldReturnListOfUsersByRole() {
        // Given
        Utilisateur admin = Utilisateur.builder()
                .id(2)
                .tokenId("admin-token-456")
                .nom("Admin")
                .prenom("Super")
                .role(Role.ADMIN)
                .email("admin@test.fr")
                .telephone("0987654321")
                .genre("Homme")
                .dateNaissance(dateNaissance)
                .build();

        List<Utilisateur> admins = Arrays.asList(admin);
        when(utilisateurRepository.findByRole(Role.ADMIN)).thenReturn(admins);

        // When
        List<ProfilDto> result = utilisateurService.getUtilisateursByRole(Role.ADMIN);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).role()).isEqualTo(Role.ADMIN);
        assertThat(result.get(0).nom()).isEqualTo("Admin");
        verify(utilisateurRepository).findByRole(Role.ADMIN);
    }

    @Test
    @DisplayName("getUtilisateursByRole - Devrait retourner une liste vide si aucun utilisateur avec ce rôle")
    void getUtilisateursByRole_WhenNoUsersWithRole_ShouldReturnEmptyList() {
        // Given
        when(utilisateurRepository.findByRole(Role.DIRECTION)).thenReturn(Collections.emptyList());

        // When
        List<ProfilDto> result = utilisateurService.getUtilisateursByRole(Role.DIRECTION);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(utilisateurRepository).findByRole(Role.DIRECTION);
    }

    @Test
    @DisplayName("getUtilisateursByRole - Devrait retourner null si une exception survient")
    void getUtilisateursByRole_WhenExceptionOccurs_ShouldReturnNull() {
        // Given
        when(utilisateurRepository.findByRole(Role.ADMIN)).thenThrow(new RuntimeException("Erreur de récupération"));

        // When
        List<ProfilDto> result = utilisateurService.getUtilisateursByRole(Role.ADMIN);

        // Then
        assertThat(result).isNull();
        verify(utilisateurRepository).findByRole(Role.ADMIN);
    }

    @Test
    @DisplayName("profilUtilisateur - Devrait retourner un utilisateur existant")
    void profilUtilisateur_WhenUserExists_ShouldReturnUser() {
        // Given
        String tokenId = "user-token-123";
        when(utilisateurRepository.findByTokenId(tokenId)).thenReturn(Optional.of(utilisateur));

        // When
        Optional<Utilisateur> result = utilisateurService.profilUtilisateur(tokenId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTokenId()).isEqualTo(tokenId);
        assertThat(result.get().getNom()).isEqualTo("Dupont");
        verify(utilisateurRepository).findByTokenId(tokenId);
    }

    @Test
    @DisplayName("profilUtilisateur - Devrait retourner Optional.empty si l'utilisateur n'existe pas")
    void profilUtilisateur_WhenUserNotFound_ShouldReturnEmpty() {
        // Given
        String tokenId = "non-existent-token";
        when(utilisateurRepository.findByTokenId(tokenId)).thenReturn(Optional.empty());

        // When
        Optional<Utilisateur> result = utilisateurService.profilUtilisateur(tokenId);

        // Then
        assertThat(result).isEmpty();
        verify(utilisateurRepository).findByTokenId(tokenId);
    }

    @Test
    @DisplayName("getUtilisateurByEmail - Devrait retourner un utilisateur existant")
    void getUtilisateurByEmail_WhenUserExists_ShouldReturnUser() {
        // Given
        String email = "jean.dupont@test.fr";
        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.of(utilisateur));

        // When
        Optional<Utilisateur> result = utilisateurService.getUtilisateurByEmail(email);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
        verify(utilisateurRepository).findByEmail(email);
    }

    @Test
    @DisplayName("getUtilisateurByEmail - Devrait retourner Optional.empty si l'utilisateur n'existe pas")
    void getUtilisateurByEmail_WhenUserNotFound_ShouldReturnEmpty() {
        // Given
        String email = "non-existent@test.fr";
        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<Utilisateur> result = utilisateurService.getUtilisateurByEmail(email);

        // Then
        assertThat(result).isEmpty();
        verify(utilisateurRepository).findByEmail(email);
    }

    @Test
    @DisplayName("mapUtilisateurToProfilDTO - Devrait mapper correctement un utilisateur vers ProfilDto")
    void mapUtilisateurToProfilDTO_ShouldMapCorrectly() {
        // When
        ProfilDto result = utilisateurService.mapUtilisateurToProfilDTO(utilisateur);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.tokenId()).isEqualTo("user-token-123");
        assertThat(result.role()).isEqualTo(Role.BASIC_USER);
        assertThat(result.roleSejour()).isNull();
        assertThat(result.nom()).isEqualTo("Dupont");
        assertThat(result.prenom()).isEqualTo("Jean");
        assertThat(result.genre()).isEqualTo("Homme");
        assertThat(result.email()).isEqualTo("jean.dupont@test.fr");
        assertThat(result.telephone()).isEqualTo("0123456789");
        assertThat(result.dateNaissance()).isEqualTo(dateNaissance);
        assertThat(result.dateExpirationCompte()).isEqualTo(refreshToken.getExpiryDate());
    }

    @Test
    @DisplayName("mapUtilisateurToProfilDTO - Devrait mapper correctement un utilisateur sans refreshToken")
    void mapUtilisateurToProfilDTO_WhenUserHasNoRefreshToken_ShouldMapCorrectly() {
        // Given
        Utilisateur utilisateurSansToken = Utilisateur.builder()
                .id(2)
                .tokenId("user-token-456")
                .nom("Martin")
                .prenom("Pierre")
                .role(Role.ADMIN)
                .email("pierre.martin@test.fr")
                .telephone("0987654321")
                .genre("Homme")
                .dateNaissance(dateNaissance)
                .refreshToken(null)
                .build();

        // When
        ProfilDto result = utilisateurService.mapUtilisateurToProfilDTO(utilisateurSansToken);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.tokenId()).isEqualTo("user-token-456");
        assertThat(result.role()).isEqualTo(Role.ADMIN);
        assertThat(result.roleSejour()).isNull();
        assertThat(result.nom()).isEqualTo("Martin");
        assertThat(result.prenom()).isEqualTo("Pierre");
        assertThat(result.genre()).isEqualTo("Homme");
        assertThat(result.email()).isEqualTo("pierre.martin@test.fr");
        assertThat(result.telephone()).isEqualTo("0987654321");
        assertThat(result.dateNaissance()).isEqualTo(dateNaissance);
        assertThat(result.dateExpirationCompte()).isNull();
    }

    @Test
    @DisplayName("modifUserByUser - Devrait modifier un utilisateur avec succès")
    @SuppressWarnings("null")
    void modifUserByUser_WithValidData_ShouldUpdateUser() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest(
                "user-token-123",
                "Pierre",
                "Martin",
                "Femme",
                "jean.dupont@test.fr",
                "0123456789",
                dateNaissance,
                null,
                null
        );

        // L'email est le même, donc existsByEmail n'est pas appelé
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Utilisateur result = utilisateurService.modifUserByUser(utilisateur, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPrenom()).isEqualTo("Pierre");
        assertThat(result.getNom()).isEqualTo("Martin");
        assertThat(result.getGenre()).isEqualTo("Femme");
        verify(utilisateurRepository, never()).existsByEmail(anyString());
        verify(utilisateurRepository).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("modifUserByUser - Devrait lancer une exception si l'email est déjà utilisé par un autre utilisateur")
    @SuppressWarnings("null")
    void modifUserByUser_WhenEmailAlreadyUsed_ShouldThrowException() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest(
                "user-token-123",
                "Jean",
                "Dupont",
                "Homme",
                "autre.email@test.fr",
                "0123456789",
                dateNaissance,
                null,
                null
        );

        when(utilisateurRepository.existsByEmail(request.email())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> utilisateurService.modifUserByUser(utilisateur, request))
                .isInstanceOf(EmailDejaUtiliseException.class)
                .hasMessageContaining("L'email est déjà utilisé par un autre compte");
        verify(utilisateurRepository).existsByEmail(request.email());
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("modifUserByAdmin - Devrait modifier un utilisateur avec succès")
    @SuppressWarnings("null")
    void modifUserByAdmin_WithValidData_ShouldUpdateUser() {
        // Given
        Instant nouvelleDateExpiration = Instant.now().plusSeconds(172800);
        UpdateUserRequest request = new UpdateUserRequest(
                "user-token-123",
                "Pierre",
                "Martin",
                "Femme",
                "jean.dupont@test.fr",
                "0123456789",
                dateNaissance,
                Role.ADMIN,
                nouvelleDateExpiration
        );

        // L'email est le même, donc existsByEmail n'est pas appelé
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Utilisateur result = utilisateurService.modifUserByAdmin(utilisateur, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPrenom()).isEqualTo("Pierre");
        assertThat(result.getNom()).isEqualTo("Martin");
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
        verify(utilisateurRepository, never()).existsByEmail(anyString());
        verify(utilisateurRepository).save(any(Utilisateur.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("modifUserByAdmin - Devrait retirer le directeur des séjours si le rôle change de DIRECTION vers autre chose")
    @SuppressWarnings("null")
    void modifUserByAdmin_WhenRoleChangesFromDirection_ShouldRemoveDirecteurFromSejours() {
        // Given
        Utilisateur directeur = Utilisateur.builder()
                .id(2)
                .tokenId("directeur-token-123")
                .nom("Directeur")
                .prenom("Test")
                .role(Role.DIRECTION)
                .email("directeur@test.fr")
                .telephone("0123456789")
                .genre("Homme")
                .dateNaissance(dateNaissance)
                .build();

        Sejour sejour1 = Sejour.builder()
                .id(1)
                .nom("Séjour 1")
                .directeur(directeur)
                .build();

        Sejour sejour2 = Sejour.builder()
                .id(2)
                .nom("Séjour 2")
                .directeur(directeur)
                .build();

        List<Sejour> sejoursDiriges = Arrays.asList(sejour1, sejour2);
        directeur.setRefreshToken(refreshToken);

        UpdateUserRequest request = new UpdateUserRequest(
                "directeur-token-123",
                "Directeur",
                "Test",
                "Homme",
                "directeur@test.fr",
                "0123456789",
                dateNaissance,
                Role.ADMIN,
                Instant.now().plusSeconds(172800)
        );

        // L'email est le même, donc existsByEmail n'est pas appelé
        when(sejourRepository.findByDirecteur(directeur)).thenReturn(sejoursDiriges);
        when(sejourRepository.saveAll(anyList())).thenReturn(sejoursDiriges);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Utilisateur result = utilisateurService.modifUserByAdmin(directeur, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
        assertThat(sejour1.getDirecteur()).isNull();
        assertThat(sejour2.getDirecteur()).isNull();
        verify(sejourRepository).findByDirecteur(directeur);
        verify(sejourRepository).saveAll(anyList());
        verify(utilisateurRepository).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("modifUserByAdmin - Devrait modifier le rôle d'un directeur sans séjours si le rôle change de DIRECTION vers autre chose")
    @SuppressWarnings("null")
    void modifUserByAdmin_WhenRoleChangesFromDirectionWithNoSejours_ShouldUpdateRoleWithoutSejours() {
        // Given
        Utilisateur directeur = Utilisateur.builder()
                .id(2)
                .tokenId("directeur-token-456")
                .nom("Directeur")
                .prenom("SansSejours")
                .role(Role.DIRECTION)
                .email("directeur.sans.sejours@test.fr")
                .telephone("0123456789")
                .genre("Homme")
                .dateNaissance(dateNaissance)
                .refreshToken(refreshToken)
                .build();

        UpdateUserRequest request = new UpdateUserRequest(
                "directeur-token-456",
                "Directeur",
                "SansSejours",
                "Homme",
                "directeur.sans.sejours@test.fr",
                "0123456789",
                dateNaissance,
                Role.ADMIN,
                Instant.now().plusSeconds(172800)
        );

        // L'email est le même, donc existsByEmail n'est pas appelé
        when(sejourRepository.findByDirecteur(directeur)).thenReturn(Collections.emptyList());
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Utilisateur result = utilisateurService.modifUserByAdmin(directeur, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
        verify(sejourRepository).findByDirecteur(directeur);
        verify(sejourRepository, never()).saveAll(anyList());
        verify(utilisateurRepository).save(any(Utilisateur.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("modifUserByAdmin - Devrait mettre à jour la date d'expiration du refreshToken")
    @SuppressWarnings("null")
    void modifUserByAdmin_ShouldUpdateRefreshTokenExpiryDate() {
        // Given
        Instant nouvelleDateExpiration = Instant.now().plusSeconds(172800);
        UpdateUserRequest request = new UpdateUserRequest(
                "user-token-123",
                "Jean",
                "Dupont",
                "Homme",
                "jean.dupont@test.fr",
                "0123456789",
                dateNaissance,
                Role.BASIC_USER,
                nouvelleDateExpiration
        );

        // L'email est le même, donc existsByEmail n'est pas appelé
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        utilisateurService.modifUserByAdmin(utilisateur, request);

        // Then
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        assertThat(refreshToken.getExpiryDate()).isEqualTo(nouvelleDateExpiration);
    }

    @Test
    @DisplayName("modifUserByAdmin - Devrait modifier un utilisateur sans refreshToken")
    @SuppressWarnings("null")
    void modifUserByAdmin_WhenUserHasNoRefreshToken_ShouldUpdateUserWithoutToken() {
        // Given
        Utilisateur utilisateurSansToken = Utilisateur.builder()
                .id(2)
                .tokenId("user-token-456")
                .nom("Dupont")
                .prenom("Jean")
                .role(Role.BASIC_USER)
                .email("jean.dupont@test.fr")
                .telephone("0123456789")
                .genre("Homme")
                .dateNaissance(dateNaissance)
                .refreshToken(null)
                .build();

        Instant nouvelleDateExpiration = Instant.now().plusSeconds(172800);
        UpdateUserRequest request = new UpdateUserRequest(
                "user-token-456",
                "Pierre",
                "Martin",
                "Femme",
                "jean.dupont@test.fr",
                "0123456789",
                dateNaissance,
                Role.ADMIN,
                nouvelleDateExpiration
        );

        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Utilisateur result = utilisateurService.modifUserByAdmin(utilisateurSansToken, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPrenom()).isEqualTo("Pierre");
        assertThat(result.getNom()).isEqualTo("Martin");
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
        verify(utilisateurRepository).save(any(Utilisateur.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("modifUserByAdmin - Devrait mettre à jour le refreshToken même si dateExpirationCompte est null")
    @SuppressWarnings("null")
    void modifUserByAdmin_WhenDateExpirationIsNull_ShouldUpdateRefreshTokenWithNull() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest(
                "user-token-123",
                "Jean",
                "Dupont",
                "Homme",
                "jean.dupont@test.fr",
                "0123456789",
                dateNaissance,
                Role.BASIC_USER,
                null
        );

        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        utilisateurService.modifUserByAdmin(utilisateur, request);

        // Then
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        assertThat(refreshToken.getExpiryDate()).isNull();
    }

    @Test
    @DisplayName("modifUserByAdmin - Devrait modifier le rôle sans affecter les séjours si le rôle ne change pas de DIRECTION")
    @SuppressWarnings("null")
    void modifUserByAdmin_WhenRoleChangesButNotFromDirection_ShouldNotAffectSejours() {
        // Given
        Utilisateur utilisateurAdmin = Utilisateur.builder()
                .id(3)
                .tokenId("admin-token-789")
                .nom("Admin")
                .prenom("Test")
                .role(Role.ADMIN)
                .email("admin@test.fr")
                .telephone("0123456789")
                .genre("Homme")
                .dateNaissance(dateNaissance)
                .refreshToken(refreshToken)
                .build();

        UpdateUserRequest request = new UpdateUserRequest(
                "admin-token-789",
                "Admin",
                "Test",
                "Homme",
                "admin@test.fr",
                "0123456789",
                dateNaissance,
                Role.DIRECTION,
                Instant.now().plusSeconds(172800)
        );

        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Utilisateur result = utilisateurService.modifUserByAdmin(utilisateurAdmin, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(Role.DIRECTION);
        verify(sejourRepository, never()).findByDirecteur(any(Utilisateur.class));
        verify(sejourRepository, never()).saveAll(anyList());
        verify(utilisateurRepository).save(any(Utilisateur.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("supprimerUtilisateur - Devrait supprimer un utilisateur existant")
    void supprimerUtilisateur_WhenUserExists_ShouldDeleteUser() {
        // Given
        String tokenId = "user-token-123";
        when(utilisateurRepository.findByTokenId(tokenId)).thenReturn(Optional.of(utilisateur));
        doNothing().when(utilisateurRepository).deleteByTokenId(tokenId);

        // When
        utilisateurService.supprimerUtilisateur(tokenId);

        // Then
        verify(utilisateurRepository).findByTokenId(tokenId);
        verify(utilisateurRepository).deleteByTokenId(tokenId);
    }

    @Test
    @DisplayName("supprimerUtilisateur - Devrait lancer une exception si l'utilisateur n'existe pas")
    void supprimerUtilisateur_WhenUserNotFound_ShouldThrowException() {
        // Given
        String tokenId = "non-existent-token";
        when(utilisateurRepository.findByTokenId(tokenId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> utilisateurService.supprimerUtilisateur(tokenId))
                .isInstanceOf(UtilisateurException.class)
                .hasMessageContaining("L'utilisateur n'existe pas");
        verify(utilisateurRepository).findByTokenId(tokenId);
        verify(utilisateurRepository, never()).deleteByTokenId(anyString());
    }

    @Test
    @DisplayName("changerMotDePasseParAdmin - Devrait changer le mot de passe avec succès")
    @SuppressWarnings("null")
    void changerMotDePasseParAdmin_WithValidData_ShouldChangePassword() {
        // Given
        String tokenId = "user-token-123";
        String nouveauMotDePasse = "NouveauMotDePasse123!";
        String encodedPassword = "encoded-new-password";

        when(utilisateurRepository.findByTokenId(tokenId)).thenReturn(Optional.of(utilisateur));
        when(bCryptPasswordEncoder.encode(nouveauMotDePasse)).thenReturn(encodedPassword);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Utilisateur result = utilisateurService.changerMotDePasseParAdmin(tokenId, nouveauMotDePasse);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMotDePasse()).isEqualTo(encodedPassword);
        verify(utilisateurRepository).findByTokenId(tokenId);
        verify(bCryptPasswordEncoder).encode(nouveauMotDePasse);
        verify(utilisateurRepository).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("changerMotDePasseParAdmin - Devrait lancer une exception si l'utilisateur n'existe pas")
    @SuppressWarnings("null")
    void changerMotDePasseParAdmin_WhenUserNotFound_ShouldThrowException() {
        // Given
        String tokenId = "non-existent-token";
        String nouveauMotDePasse = "NouveauMotDePasse123!";
        when(utilisateurRepository.findByTokenId(tokenId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> utilisateurService.changerMotDePasseParAdmin(tokenId, nouveauMotDePasse))
                .isInstanceOf(UtilisateurException.class)
                .hasMessageContaining("Utilisateur non trouvé");
        verify(utilisateurRepository).findByTokenId(tokenId);
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("changerMotDePasseParUtilisateur - Devrait changer le mot de passe avec succès")
    @SuppressWarnings("null")
    void changerMotDePasseParUtilisateur_WithValidData_ShouldChangePassword() {
        // Given
        String tokenId = "user-token-123";
        String ancienMotDePasse = "MotDePasse123!";
        String nouveauMotDePasse = "NouveauMotDePasse123!";
        String encodedPassword = "encoded-new-password";

        utilisateur.setMotDePasse("encoded-old-password");
        when(utilisateurRepository.findByTokenId(tokenId)).thenReturn(Optional.of(utilisateur));
        when(bCryptPasswordEncoder.matches(ancienMotDePasse, utilisateur.getMotDePasse())).thenReturn(true);
        when(bCryptPasswordEncoder.encode(nouveauMotDePasse)).thenReturn(encodedPassword);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Utilisateur result = utilisateurService.changerMotDePasseParUtilisateur(tokenId, ancienMotDePasse, nouveauMotDePasse);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMotDePasse()).isEqualTo(encodedPassword);
        verify(utilisateurRepository).findByTokenId(tokenId);
        verify(bCryptPasswordEncoder).matches(ancienMotDePasse, "encoded-old-password");
        verify(bCryptPasswordEncoder).encode(nouveauMotDePasse);
        verify(utilisateurRepository).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("changerMotDePasseParUtilisateur - Devrait lancer une exception si l'ancien mot de passe est incorrect")
    @SuppressWarnings("null")
    void changerMotDePasseParUtilisateur_WhenOldPasswordIncorrect_ShouldThrowException() {
        // Given
        String tokenId = "user-token-123";
        String ancienMotDePasse = "MauvaisMotDePasse123!";
        String nouveauMotDePasse = "NouveauMotDePasse123!";

        utilisateur.setMotDePasse("encoded-old-password");
        when(utilisateurRepository.findByTokenId(tokenId)).thenReturn(Optional.of(utilisateur));
        when(bCryptPasswordEncoder.matches(ancienMotDePasse, utilisateur.getMotDePasse())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> utilisateurService.changerMotDePasseParUtilisateur(tokenId, ancienMotDePasse, nouveauMotDePasse))
                .isInstanceOf(UtilisateurException.class)
                .hasMessageContaining("L'ancien mot de passe est incorrect");
        verify(utilisateurRepository).findByTokenId(tokenId);
        verify(bCryptPasswordEncoder).matches(ancienMotDePasse, utilisateur.getMotDePasse());
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("changerMotDePasseParUtilisateur - Devrait lancer une exception si l'utilisateur n'existe pas")
    @SuppressWarnings("null")
    void changerMotDePasseParUtilisateur_WhenUserNotFound_ShouldThrowException() {
        // Given
        String tokenId = "non-existent-token";
        String ancienMotDePasse = "MotDePasse123!";
        String nouveauMotDePasse = "NouveauMotDePasse123!";
        when(utilisateurRepository.findByTokenId(tokenId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> utilisateurService.changerMotDePasseParUtilisateur(tokenId, ancienMotDePasse, nouveauMotDePasse))
                .isInstanceOf(UtilisateurException.class)
                .hasMessageContaining("Utilisateur non trouvé");
        verify(utilisateurRepository).findByTokenId(tokenId);
        verify(bCryptPasswordEncoder, never()).matches(anyString(), anyString());
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }
}
