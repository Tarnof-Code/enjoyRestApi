package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.Enfant;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.SejourEnfant;
import com.tarnof.enjoyrestapi.entities.SejourEnfantId;
import com.tarnof.enjoyrestapi.enums.Genre;
import com.tarnof.enjoyrestapi.enums.NiveauScolaire;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.CreateEnfantRequest;
import com.tarnof.enjoyrestapi.payload.response.EnfantDto;
import com.tarnof.enjoyrestapi.payload.response.ExcelImportResponse;
import com.tarnof.enjoyrestapi.repositories.EnfantRepository;
import com.tarnof.enjoyrestapi.repositories.SejourEnfantRepository;
import com.tarnof.enjoyrestapi.repositories.SejourRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour EnfantServiceImpl")
@SuppressWarnings("null")
class EnfantServiceImplTest {

    @Mock
    private EnfantRepository enfantRepository;

    @Mock
    private SejourRepository sejourRepository;

    @Mock
    private SejourEnfantRepository sejourEnfantRepository;

    @InjectMocks
    private EnfantServiceImpl enfantService;

    private Sejour sejour;
    private Enfant enfant;
    private SejourEnfant sejourEnfant;
    private CreateEnfantRequest createEnfantRequest;
    private Date dateNaissance;

    @BeforeEach
    void setUp() {
        dateNaissance = new Date(System.currentTimeMillis() - 86400000L * 365 * 10); // 10 ans

        sejour = Sejour.builder()
                .id(1)
                .nom("Séjour Test")
                .description("Description")
                .dateDebut(new Date())
                .dateFin(new Date())
                .lieuDuSejour("Lieu")
                .enfants(new ArrayList<>())
                .build();

        enfant = Enfant.builder()
                .id(1)
                .nom("Martin")
                .prenom("Emma")
                .genre(Genre.Féminin)
                .dateNaissance(dateNaissance)
                .niveauScolaire(NiveauScolaire.CP)
                .build();

        sejourEnfant = new SejourEnfant(sejour, enfant);

        createEnfantRequest = new CreateEnfantRequest(
                "Dupont",
                "Lucas",
                Genre.Masculin,
                dateNaissance,
                NiveauScolaire.MS
        );
    }

    // ==================== creerEtAjouterEnfantAuSejour ====================

    @Test
    @DisplayName("creerEtAjouterEnfantAuSejour - Devrait créer un nouvel enfant et l'ajouter au séjour")
    void creerEtAjouterEnfantAuSejour_WithNewEnfant_ShouldCreateAndAdd() {
        // Given - enfant n'existe pas
        Enfant enfantCree = Enfant.builder()
                .id(1)
                .nom(createEnfantRequest.nom())
                .prenom(createEnfantRequest.prenom())
                .genre(createEnfantRequest.genre())
                .dateNaissance(createEnfantRequest.dateNaissance())
                .niveauScolaire(createEnfantRequest.niveauScolaire())
                .build();

        when(sejourRepository.findById(1)).thenReturn(Optional.of(Objects.requireNonNull(sejour)));
        when(enfantRepository.findByNomAndPrenomAndGenreAndDateNaissance(
                createEnfantRequest.nom(),
                createEnfantRequest.prenom(),
                createEnfantRequest.genre(),
                createEnfantRequest.dateNaissance())).thenReturn(Optional.empty());
        when(enfantRepository.save(any(Enfant.class))).thenReturn(enfantCree);
        when(sejourRepository.save(any(Sejour.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        enfantService.creerEtAjouterEnfantAuSejour(1, createEnfantRequest);

        // Then
        verify(sejourRepository).findById(1);
        verify(enfantRepository).findByNomAndPrenomAndGenreAndDateNaissance(
                createEnfantRequest.nom(),
                createEnfantRequest.prenom(),
                createEnfantRequest.genre(),
                createEnfantRequest.dateNaissance());
        verify(enfantRepository).save(any(Enfant.class));
        verify(sejourRepository).save(any(Sejour.class));
        assertThat(sejour.getEnfants()).hasSize(1);
    }

    @Test
    @DisplayName("creerEtAjouterEnfantAuSejour - Devrait réutiliser l'enfant existant s'il n'est pas dans le séjour")
    void creerEtAjouterEnfantAuSejour_WithExistingEnfantNotInSejour_ShouldReuseAndAdd() {
        // Given - enfant existe déjà mais pas dans ce séjour
        when(sejourRepository.findById(1)).thenReturn(Optional.of(Objects.requireNonNull(sejour)));
        when(enfantRepository.findByNomAndPrenomAndGenreAndDateNaissance(
                createEnfantRequest.nom(),
                createEnfantRequest.prenom(),
                createEnfantRequest.genre(),
                createEnfantRequest.dateNaissance())).thenReturn(Optional.of(Objects.requireNonNull(enfant)));
        when(sejourRepository.save(any(Sejour.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        enfantService.creerEtAjouterEnfantAuSejour(1, createEnfantRequest);

        // Then
        verify(sejourRepository).findById(1);
        verify(enfantRepository).findByNomAndPrenomAndGenreAndDateNaissance(
                createEnfantRequest.nom(),
                createEnfantRequest.prenom(),
                createEnfantRequest.genre(),
                createEnfantRequest.dateNaissance());
        verify(enfantRepository, never()).save(any(Enfant.class));
        verify(sejourRepository).save(any(Sejour.class));
        assertThat(sejour.getEnfants()).hasSize(1);
    }

    @Test
    @DisplayName("creerEtAjouterEnfantAuSejour - Devrait lancer une exception si l'enfant existe déjà dans le séjour")
    void creerEtAjouterEnfantAuSejour_WhenChildAlreadyInSejour_ShouldThrow409() {
        // Given - enfant existe et est déjà dans le séjour
        sejour.getEnfants().add(sejourEnfant);

        CreateEnfantRequest sameEnfantRequest = new CreateEnfantRequest(
                enfant.getNom(),
                enfant.getPrenom(),
                enfant.getGenre(),
                enfant.getDateNaissance(),
                enfant.getNiveauScolaire()
        );

        when(sejourRepository.findById(1)).thenReturn(Optional.of(Objects.requireNonNull(sejour)));
        when(enfantRepository.findByNomAndPrenomAndGenreAndDateNaissance(
                enfant.getNom(),
                enfant.getPrenom(),
                enfant.getGenre(),
                enfant.getDateNaissance())).thenReturn(Optional.of(Objects.requireNonNull(enfant)));

        // When & Then
        assertThatThrownBy(() -> enfantService.creerEtAjouterEnfantAuSejour(1, sameEnfantRequest))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("existe déjà dans ce séjour");

        verify(sejourRepository).findById(1);
        verify(enfantRepository).findByNomAndPrenomAndGenreAndDateNaissance(
                enfant.getNom(), enfant.getPrenom(), enfant.getGenre(), enfant.getDateNaissance());
        verify(enfantRepository, never()).save(any(Enfant.class));
        verify(sejourRepository, never()).save(any(Sejour.class));
    }

    @Test
    @DisplayName("creerEtAjouterEnfantAuSejour - Devrait lancer une exception si le séjour n'existe pas")
    void creerEtAjouterEnfantAuSejour_WhenSejourNotFound_ShouldThrow404() {
        when(sejourRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enfantService.creerEtAjouterEnfantAuSejour(999, createEnfantRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Séjour non trouvé avec l'ID: 999");

        verify(sejourRepository).findById(999);
        verify(enfantRepository, never()).findByNomAndPrenomAndGenreAndDateNaissance(any(), any(), any(), any());
    }

    // ==================== modifierEnfant ====================

    @Test
    @DisplayName("modifierEnfant - Devrait modifier un enfant avec succès")
    void modifierEnfant_WithValidData_ShouldUpdateEnfant() {
        // Given - pas d'autre enfant avec les nouvelles données
        SejourEnfantId sejourEnfantId = new SejourEnfantId(1, 1);
        when(sejourEnfantRepository.findById(sejourEnfantId)).thenReturn(Optional.of(Objects.requireNonNull(sejourEnfant)));
        when(enfantRepository.findByNomAndPrenomAndGenreAndDateNaissance(
                createEnfantRequest.nom(),
                createEnfantRequest.prenom(),
                createEnfantRequest.genre(),
                createEnfantRequest.dateNaissance())).thenReturn(Optional.empty());

        Enfant enfantModifie = Enfant.builder()
                .id(1)
                .nom(createEnfantRequest.nom())
                .prenom(createEnfantRequest.prenom())
                .genre(createEnfantRequest.genre())
                .dateNaissance(createEnfantRequest.dateNaissance())
                .niveauScolaire(createEnfantRequest.niveauScolaire())
                .build();

        when(enfantRepository.save(any(Enfant.class))).thenReturn(enfantModifie);

        // When
        EnfantDto result = enfantService.modifierEnfant(1, 1, createEnfantRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.nom()).isEqualTo("Dupont");
        assertThat(result.prenom()).isEqualTo("Lucas");
        assertThat(result.genre()).isEqualTo(Genre.Masculin);
        verify(sejourEnfantRepository).findById(sejourEnfantId);
        verify(enfantRepository).save(any(Enfant.class));
    }

    @Test
    @DisplayName("modifierEnfant - Devrait remplacer par l'enfant existant s'il n'est pas dans le séjour")
    void modifierEnfant_WhenExistingChildNotInSejour_ShouldReplaceRelation() {
        // Given - un autre enfant existe avec les mêmes données, pas dans le séjour
        sejour.getEnfants().add(sejourEnfant);
        Enfant autreEnfant = Enfant.builder()
                .id(2)
                .nom("Dupont")
                .prenom("Lucas")
                .genre(Genre.Masculin)
                .dateNaissance(dateNaissance)
                .niveauScolaire(NiveauScolaire.MS)
                .build();

        SejourEnfantId sejourEnfantId = new SejourEnfantId(1, 1);
        when(sejourEnfantRepository.findById(sejourEnfantId)).thenReturn(Optional.of(sejourEnfant));
        when(enfantRepository.findByNomAndPrenomAndGenreAndDateNaissance(
                createEnfantRequest.nom(),
                createEnfantRequest.prenom(),
                createEnfantRequest.genre(),
                createEnfantRequest.dateNaissance())).thenReturn(Optional.of(autreEnfant));
        // Après suppression de la relation, enfantActuel (id=1) n'est plus dans aucun séjour
        when(sejourEnfantRepository.countByEnfantId(1)).thenReturn(0L);
        when(sejourEnfantRepository.save(any(SejourEnfant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sejourRepository.save(any(Sejour.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        EnfantDto result = enfantService.modifierEnfant(1, 1, createEnfantRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2);
        assertThat(result.nom()).isEqualTo("Dupont");
        verify(sejourEnfantRepository).delete(sejourEnfant);
        verify(sejourEnfantRepository).flush();
        verify(sejourEnfantRepository).save(any(SejourEnfant.class));
        verify(enfantRepository).delete(Objects.requireNonNull(enfant));
    }

    @Test
    @DisplayName("modifierEnfant - Devrait lancer une exception si l'enfant existant est déjà dans le séjour")
    void modifierEnfant_WhenExistingChildAlreadyInSejour_ShouldThrow409() {
        // Given - un autre enfant existe avec les mêmes données ET est dans le séjour
        Enfant autreEnfant = Enfant.builder()
                .id(2)
                .nom("Dupont")
                .prenom("Lucas")
                .genre(Genre.Masculin)
                .dateNaissance(dateNaissance)
                .niveauScolaire(NiveauScolaire.MS)
                .build();

        SejourEnfant autreSejourEnfant = new SejourEnfant(sejour, autreEnfant);
        sejour.getEnfants().add(sejourEnfant);
        sejour.getEnfants().add(autreSejourEnfant);

        SejourEnfantId sejourEnfantId = new SejourEnfantId(1, 1);
        when(sejourEnfantRepository.findById(sejourEnfantId)).thenReturn(Optional.of(Objects.requireNonNull(sejourEnfant)));
        when(enfantRepository.findByNomAndPrenomAndGenreAndDateNaissance(
                createEnfantRequest.nom(),
                createEnfantRequest.prenom(),
                createEnfantRequest.genre(),
                createEnfantRequest.dateNaissance())).thenReturn(Optional.of(autreEnfant));

        // When & Then
        assertThatThrownBy(() -> enfantService.modifierEnfant(1, 1, createEnfantRequest))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("existe déjà dans ce séjour");

        verify(sejourEnfantRepository).findById(sejourEnfantId);
        verify(sejourEnfantRepository, never()).delete(any());
        verify(enfantRepository, never()).save(any(Enfant.class));
    }

    @Test
    @DisplayName("modifierEnfant - Devrait lancer une exception si l'enfant n'est pas inscrit au séjour")
    void modifierEnfant_WhenChildNotInSejour_ShouldThrow404() {
        SejourEnfantId sejourEnfantId = new SejourEnfantId(1, 99);
        when(sejourEnfantRepository.findById(Objects.requireNonNull(sejourEnfantId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enfantService.modifierEnfant(1, 99, createEnfantRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("L'enfant n'est pas inscrit à ce séjour");

        verify(sejourEnfantRepository).findById(sejourEnfantId);
        verify(enfantRepository, never()).save(any(Enfant.class));
    }

    // ==================== supprimerEnfantDuSejour ====================

    @Test
    @DisplayName("supprimerEnfantDuSejour - Devrait supprimer l'enfant du séjour")
    void supprimerEnfantDuSejour_WhenChildInMultipleSejours_ShouldOnlyRemoveRelation() {
        // Given - enfant dans plusieurs séjours
        SejourEnfantId sejourEnfantId = new SejourEnfantId(1, 1);
        when(sejourEnfantRepository.findById(sejourEnfantId)).thenReturn(Optional.of(Objects.requireNonNull(sejourEnfant)));
        when(sejourEnfantRepository.countByEnfantId(1)).thenReturn(2L);

        enfantService.supprimerEnfantDuSejour(1, 1);

        verify(sejourEnfantRepository).deleteById(sejourEnfantId);
        verify(sejourEnfantRepository).flush();
        verify(enfantRepository, never()).delete(any(Enfant.class));
    }

    @Test
    @DisplayName("supprimerEnfantDuSejour - Devrait supprimer l'enfant de la BDD s'il n'est plus dans aucun séjour")
    void supprimerEnfantDuSejour_WhenLastSejour_ShouldDeleteEnfant() {
        SejourEnfantId sejourEnfantId = new SejourEnfantId(1, 1);
        when(sejourEnfantRepository.findById(sejourEnfantId)).thenReturn(Optional.of(Objects.requireNonNull(sejourEnfant)));
        when(sejourEnfantRepository.countByEnfantId(1)).thenReturn(1L);

        enfantService.supprimerEnfantDuSejour(1, 1);

        verify(sejourEnfantRepository).deleteById(sejourEnfantId);
        verify(enfantRepository).delete(Objects.requireNonNull(enfant));
        verify(enfantRepository).flush();
    }

    @Test
    @DisplayName("supprimerEnfantDuSejour - Devrait lancer une exception si l'enfant n'est pas inscrit")
    void supprimerEnfantDuSejour_WhenChildNotInSejour_ShouldThrow404() {
        SejourEnfantId sejourEnfantId = new SejourEnfantId(1, 99);
        when(sejourEnfantRepository.findById(sejourEnfantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enfantService.supprimerEnfantDuSejour(1, 99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("L'enfant n'est pas inscrit à ce séjour");

        verify(sejourEnfantRepository).findById(sejourEnfantId);
        verify(sejourEnfantRepository, never()).deleteById(any());
    }

    // ==================== supprimerTousLesEnfantsDuSejour ====================

    @Test
    @DisplayName("supprimerTousLesEnfantsDuSejour - Devrait supprimer tous les enfants du séjour")
    void supprimerTousLesEnfantsDuSejour_WithChildren_ShouldRemoveAllAndDeleteOrphans() {
        sejour.getEnfants().add(sejourEnfant);
        when(sejourRepository.findById(1)).thenReturn(Optional.of(Objects.requireNonNull(sejour)));
        when(sejourEnfantRepository.countByEnfantId(1)).thenReturn(1L);
        when(sejourRepository.save(any(Sejour.class))).thenAnswer(invocation -> invocation.getArgument(0));

        enfantService.supprimerTousLesEnfantsDuSejour(1);

        verify(sejourRepository).findById(1);
        verify(sejourRepository).save(any(Sejour.class));
        verify(enfantRepository).delete(Objects.requireNonNull(enfant));
        assertThat(sejour.getEnfants()).isEmpty();
    }

    @Test
    @DisplayName("supprimerTousLesEnfantsDuSejour - Devrait ne rien faire si le séjour n'a pas d'enfants")
    void supprimerTousLesEnfantsDuSejour_WhenNoChildren_ShouldReturnEarly() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(Objects.requireNonNull(sejour)));

        enfantService.supprimerTousLesEnfantsDuSejour(1);

        verify(sejourRepository).findById(1);
        verify(sejourRepository, never()).save(any(Sejour.class));
        verify(enfantRepository, never()).delete(any(Enfant.class));
    }

    @Test
    @DisplayName("supprimerTousLesEnfantsDuSejour - Devrait lancer une exception si le séjour n'existe pas")
    void supprimerTousLesEnfantsDuSejour_WhenSejourNotFound_ShouldThrow404() {
        when(sejourRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enfantService.supprimerTousLesEnfantsDuSejour(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Séjour non trouvé avec l'ID: 999");

        verify(sejourRepository).findById(999);
        verify(enfantRepository, never()).delete(any(Enfant.class));
    }

    @Test
    @DisplayName("supprimerTousLesEnfantsDuSejour - Devrait ne pas supprimer les enfants dans d'autres séjours")
    void supprimerTousLesEnfantsDuSejour_WhenChildInOtherSejours_ShouldNotDeleteEnfant() {
        sejour.getEnfants().add(sejourEnfant);
        when(sejourRepository.findById(1)).thenReturn(Optional.of(Objects.requireNonNull(sejour)));
        when(sejourEnfantRepository.countByEnfantId(1)).thenReturn(2L); // enfant dans 2 séjours
        when(sejourRepository.save(any(Sejour.class))).thenAnswer(invocation -> invocation.getArgument(0));

        enfantService.supprimerTousLesEnfantsDuSejour(1);

        verify(sejourRepository).save(any(Sejour.class));
        verify(enfantRepository, never()).delete(any(Enfant.class));
    }

    // ==================== getEnfantsDuSejour ====================

    @Test
    @DisplayName("getEnfantsDuSejour - Devrait retourner la liste des enfants du séjour")
    void getEnfantsDuSejour_WithChildren_ShouldReturnList() {
        sejour.getEnfants().add(sejourEnfant);
        when(sejourRepository.findById(1)).thenReturn(Optional.of(Objects.requireNonNull(sejour)));

        List<EnfantDto> result = enfantService.getEnfantsDuSejour(1);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).nom()).isEqualTo("Martin");
        assertThat(result.get(0).prenom()).isEqualTo("Emma");
        verify(sejourRepository).findById(1);
    }

    @Test
    @DisplayName("getEnfantsDuSejour - Devrait retourner une liste vide si le séjour n'a pas d'enfants")
    void getEnfantsDuSejour_WhenNoChildren_ShouldReturnEmptyList() {
        when(sejourRepository.findById(1)).thenReturn(Optional.of(sejour));

        List<EnfantDto> result = enfantService.getEnfantsDuSejour(1);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(sejourRepository).findById(1);
    }

    @Test
    @DisplayName("getEnfantsDuSejour - Devrait lancer une exception si le séjour n'existe pas")
    void getEnfantsDuSejour_WhenSejourNotFound_ShouldThrow404() {
        when(sejourRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enfantService.getEnfantsDuSejour(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Séjour non trouvé avec l'ID: 999");

        verify(sejourRepository).findById(999);
    }

    // ==================== importerEnfantsDepuisExcel ====================

    @Test
    @DisplayName("importerEnfantsDepuisExcel - Devrait importer des enfants avec succès")
    void importerEnfantsDepuisExcel_WithValidFile_ShouldImportChildren() throws IOException {
        MockMultipartFile file = createValidExcelFile();
        when(sejourRepository.findById(1)).thenReturn(Optional.of(Objects.requireNonNull(sejour)));
        when(enfantRepository.findByNomAndPrenomAndGenreAndDateNaissance(any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        when(enfantRepository.save(any(Enfant.class))).thenAnswer(invocation -> {
            Enfant e = invocation.getArgument(0);
            e.setId(1);
            return e;
        });
        when(sejourRepository.save(any(Sejour.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExcelImportResponse result = enfantService.importerEnfantsDepuisExcel(1, file);

        assertThat(result).isNotNull();
        assertThat(result.totalLignes()).isEqualTo(1);
        assertThat(result.enfantsCrees()).isEqualTo(1);
        assertThat(result.enfantsDejaExistants()).isEqualTo(0);
        assertThat(result.erreurs()).isEqualTo(0);
        assertThat(result.messagesErreur()).isEmpty();
    }

    @Test
    @DisplayName("importerEnfantsDepuisExcel - Devrait retourner des erreurs pour un fichier avec colonnes manquantes")
    void importerEnfantsDepuisExcel_WhenColumnsMissing_ShouldReturnErrors() throws IOException {
        MockMultipartFile file = createExcelFileWithMissingColumns();

        ExcelImportResponse result = enfantService.importerEnfantsDepuisExcel(1, file);

        assertThat(result).isNotNull();
        assertThat(result.totalLignes()).isEqualTo(0);
        assertThat(result.enfantsCrees()).isEqualTo(0);
        assertThat(result.erreurs()).isGreaterThan(0);
        assertThat(result.messagesErreur()).isNotEmpty();
    }

    @Test
    @DisplayName("importerEnfantsDepuisExcel - Devrait compter les enfants déjà existants")
    void importerEnfantsDepuisExcel_WhenChildAlreadyInSejour_ShouldCountAsDejaExistant() throws IOException {
        MockMultipartFile file = createValidExcelFile();
        sejour.getEnfants().add(sejourEnfant);
        when(sejourRepository.findById(1)).thenReturn(Optional.of(Objects.requireNonNull(sejour)));
        when(enfantRepository.findByNomAndPrenomAndGenreAndDateNaissance(
                eq("Martin"), eq("Emma"), eq(Genre.Féminin), any())).thenReturn(Optional.of(Objects.requireNonNull(enfant)));

        ExcelImportResponse result = enfantService.importerEnfantsDepuisExcel(1, file);

        assertThat(result).isNotNull();
        assertThat(result.totalLignes()).isEqualTo(1);
        assertThat(result.enfantsCrees()).isEqualTo(0);
        assertThat(result.enfantsDejaExistants()).isEqualTo(1);
        assertThat(result.erreurs()).isEqualTo(1);
    }

    @Test
    @DisplayName("importerEnfantsDepuisExcel - Devrait retourner des erreurs quand le séjour n'existe pas")
    void importerEnfantsDepuisExcel_WhenSejourNotFound_ShouldReturnErrors() throws IOException {
        MockMultipartFile file = createValidExcelFile();
        when(sejourRepository.findById(999)).thenReturn(Optional.empty());

        ExcelImportResponse result = enfantService.importerEnfantsDepuisExcel(999, file);

        assertThat(result).isNotNull();
        assertThat(result.totalLignes()).isEqualTo(1);
        assertThat(result.erreurs()).isEqualTo(1);
        assertThat(result.messagesErreur()).isNotEmpty();
        assertThat(result.messagesErreur().get(0)).contains("Séjour non trouvé");
        verify(enfantRepository, never()).save(any(Enfant.class));
    }

    @Test
    @DisplayName("importerEnfantsDepuisExcel - Devrait lancer une exception pour un fichier vide")
    void importerEnfantsDepuisExcel_WhenEmptyFile_ShouldThrowException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]
        );

        assertThatThrownBy(() -> enfantService.importerEnfantsDepuisExcel(1, file))
                .hasMessageContaining("empty");
    }

    // ==================== Helpers ====================

    private MockMultipartFile createValidExcelFile() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Enfants");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Nom");
            headerRow.createCell(1).setCellValue("Prénom");
            headerRow.createCell(2).setCellValue("Genre");
            headerRow.createCell(3).setCellValue("Date de naissance");
            headerRow.createCell(4).setCellValue("Niveau scolaire");

            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("Martin");
            dataRow.createCell(1).setCellValue("Emma");
            dataRow.createCell(2).setCellValue("Féminin");
            dataRow.createCell(3).setCellValue(new SimpleDateFormat("dd/MM/yyyy").format(dateNaissance));
            dataRow.createCell(4).setCellValue("CP");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] bytes = outputStream.toByteArray();

            return new MockMultipartFile(
                    "file",
                    "enfants.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    bytes
            );
        }
    }

    private MockMultipartFile createExcelFileWithMissingColumns() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Enfants");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Colonne Invalide");
            headerRow.createCell(1).setCellValue("Autre Colonne");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] bytes = outputStream.toByteArray();

            return new MockMultipartFile(
                    "file",
                    "enfants-invalid.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    bytes
            );
        }
    }
}
