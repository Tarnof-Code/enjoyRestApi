package com.tarnof.enjoyrestapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tarnof.enjoyrestapi.enums.Genre;
import com.tarnof.enjoyrestapi.enums.NiveauScolaire;
import com.tarnof.enjoyrestapi.exceptions.GlobalExceptionHandler;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.CreateEnfantRequest;
import com.tarnof.enjoyrestapi.payload.response.EnfantDto;
import com.tarnof.enjoyrestapi.payload.response.ExcelImportResponse;
import com.tarnof.enjoyrestapi.services.EnfantService;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour EnfantController")
@SuppressWarnings("null")
class EnfantControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private EnfantService enfantService;

    @InjectMocks
    private EnfantController enfantController;

    private EnfantDto enfantDto;
    private CreateEnfantRequest createEnfantRequest;
    private ExcelImportResponse excelImportResponse;
    private Date dateNaissance;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

        mockMvc = MockMvcBuilders.standaloneSetup(enfantController)
                .setControllerAdvice(globalExceptionHandler)
                .build();

        dateNaissance = new Date(System.currentTimeMillis() - 86400000L * 365 * 10);

        enfantDto = new EnfantDto(
                1,
                "Martin",
                "Emma",
                Genre.Féminin,
                dateNaissance,
                NiveauScolaire.CP
        );

        createEnfantRequest = new CreateEnfantRequest(
                "Dupont",
                "Lucas",
                Genre.Masculin,
                dateNaissance,
                NiveauScolaire.MS
        );

        excelImportResponse = new ExcelImportResponse(
                1,
                1,
                0,
                0,
                Collections.emptyList()
        );
    }

    // ========== Tests pour getEnfantsDuSejour() ==========

    @Test
    @DisplayName("getEnfantsDuSejour - Devrait retourner 200 OK avec la liste des enfants")
    void getEnfantsDuSejour_ShouldReturn200WithEnfantsList() throws Exception {
        List<EnfantDto> enfants = Arrays.asList(enfantDto);
        when(enfantService.getEnfantsDuSejour(1)).thenReturn(enfants);

        mockMvc.perform(get("/api/v1/sejours/1/enfants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nom").value("Martin"))
                .andExpect(jsonPath("$[0].prenom").value("Emma"))
                .andExpect(jsonPath("$[0].genre").value("Féminin"))
                .andExpect(jsonPath("$[0].niveauScolaire").value("CP"));

        verify(enfantService).getEnfantsDuSejour(1);
    }

    @Test
    @DisplayName("getEnfantsDuSejour - Devrait retourner 200 OK avec une liste vide")
    void getEnfantsDuSejour_ShouldReturn200WithEmptyList() throws Exception {
        when(enfantService.getEnfantsDuSejour(1)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/sejours/1/enfants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(enfantService).getEnfantsDuSejour(1);
    }

    @Test
    @DisplayName("getEnfantsDuSejour - Devrait retourner 404 Not Found si le séjour n'existe pas")
    void getEnfantsDuSejour_ShouldReturn404WhenSejourNotFound() throws Exception {
        when(enfantService.getEnfantsDuSejour(999))
                .thenThrow(new ResourceNotFoundException("Séjour non trouvé avec l'ID: 999"));

        mockMvc.perform(get("/api/v1/sejours/999/enfants"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Séjour non trouvé avec l'ID: 999"));

        verify(enfantService).getEnfantsDuSejour(999);
    }

    // ========== Tests pour creerEtAjouterEnfantAuSejour() ==========

    @Test
    @DisplayName("creerEtAjouterEnfantAuSejour - Devrait retourner 201 Created")
    void creerEtAjouterEnfantAuSejour_ShouldReturn201Created() throws Exception {
        doNothing().when(enfantService).creerEtAjouterEnfantAuSejour(eq(1), any(CreateEnfantRequest.class));

        mockMvc.perform(post("/api/v1/sejours/1/enfants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEnfantRequest)))
                .andExpect(status().isCreated());

        verify(enfantService).creerEtAjouterEnfantAuSejour(eq(1), any(CreateEnfantRequest.class));
    }

    @Test
    @DisplayName("creerEtAjouterEnfantAuSejour - Devrait retourner 404 Not Found si le séjour n'existe pas")
    void creerEtAjouterEnfantAuSejour_ShouldReturn404WhenSejourNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Séjour non trouvé avec l'ID: 999"))
                .when(enfantService).creerEtAjouterEnfantAuSejour(eq(999), any(CreateEnfantRequest.class));

        mockMvc.perform(post("/api/v1/sejours/999/enfants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEnfantRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Séjour non trouvé avec l'ID: 999"));

        verify(enfantService).creerEtAjouterEnfantAuSejour(eq(999), any(CreateEnfantRequest.class));
    }

    @Test
    @DisplayName("creerEtAjouterEnfantAuSejour - Devrait retourner 409 Conflict si l'enfant est déjà dans le séjour")
    void creerEtAjouterEnfantAuSejour_ShouldReturn409WhenChildAlreadyInSejour() throws Exception {
        doThrow(new ResourceAlreadyExistsException("Lucas Dupont né le 15/03/2014 existe déjà dans ce séjour"))
                .when(enfantService).creerEtAjouterEnfantAuSejour(eq(1), any(CreateEnfantRequest.class));

        mockMvc.perform(post("/api/v1/sejours/1/enfants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEnfantRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Lucas Dupont né le 15/03/2014 existe déjà dans ce séjour"));

        verify(enfantService).creerEtAjouterEnfantAuSejour(eq(1), any(CreateEnfantRequest.class));
    }

    // ========== Tests pour modifierEnfant() ==========

    @Test
    @DisplayName("modifierEnfant - Devrait retourner 200 OK avec l'enfant modifié")
    void modifierEnfant_ShouldReturn200WithUpdatedEnfant() throws Exception {
        EnfantDto updatedEnfantDto = new EnfantDto(
                1,
                "Dupont",
                "Lucas",
                Genre.Masculin,
                dateNaissance,
                NiveauScolaire.MS
        );

        when(enfantService.modifierEnfant(eq(1), eq(1), any(CreateEnfantRequest.class))).thenReturn(updatedEnfantDto);

        mockMvc.perform(put("/api/v1/sejours/1/enfants/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEnfantRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nom").value("Dupont"))
                .andExpect(jsonPath("$.prenom").value("Lucas"))
                .andExpect(jsonPath("$.genre").value("Masculin"))
                .andExpect(jsonPath("$.niveauScolaire").value("MS"));

        verify(enfantService).modifierEnfant(eq(1), eq(1), any(CreateEnfantRequest.class));
    }

    @Test
    @DisplayName("modifierEnfant - Devrait retourner 404 Not Found si le séjour ou l'enfant n'existe pas")
    void modifierEnfant_ShouldReturn404WhenNotFound() throws Exception {
        when(enfantService.modifierEnfant(eq(999), eq(1), any(CreateEnfantRequest.class)))
                .thenThrow(new ResourceNotFoundException("Séjour non trouvé avec l'ID: 999"));

        mockMvc.perform(put("/api/v1/sejours/999/enfants/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEnfantRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Séjour non trouvé avec l'ID: 999"));

        verify(enfantService).modifierEnfant(eq(999), eq(1), any(CreateEnfantRequest.class));
    }

    @Test
    @DisplayName("modifierEnfant - Devrait retourner 409 Conflict si l'enfant existant est déjà dans le séjour")
    void modifierEnfant_ShouldReturn409WhenChildAlreadyInSejour() throws Exception {
        when(enfantService.modifierEnfant(eq(1), eq(1), any(CreateEnfantRequest.class)))
                .thenThrow(new ResourceAlreadyExistsException("Un enfant avec ces informations existe déjà dans ce séjour"));

        mockMvc.perform(put("/api/v1/sejours/1/enfants/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEnfantRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Un enfant avec ces informations existe déjà dans ce séjour"));

        verify(enfantService).modifierEnfant(eq(1), eq(1), any(CreateEnfantRequest.class));
    }

    // ========== Tests pour supprimerEnfantDuSejour() ==========

    @Test
    @DisplayName("supprimerEnfantDuSejour - Devrait retourner 204 No Content")
    void supprimerEnfantDuSejour_ShouldReturn204NoContent() throws Exception {
        doNothing().when(enfantService).supprimerEnfantDuSejour(1, 1);

        mockMvc.perform(delete("/api/v1/sejours/1/enfants/1"))
                .andExpect(status().isNoContent());

        verify(enfantService).supprimerEnfantDuSejour(1, 1);
    }

    @Test
    @DisplayName("supprimerEnfantDuSejour - Devrait retourner 404 Not Found si le séjour ou l'enfant n'existe pas")
    void supprimerEnfantDuSejour_ShouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("L'enfant n'est pas inscrit à ce séjour"))
                .when(enfantService).supprimerEnfantDuSejour(1, 999);

        mockMvc.perform(delete("/api/v1/sejours/1/enfants/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("L'enfant n'est pas inscrit à ce séjour"));

        verify(enfantService).supprimerEnfantDuSejour(1, 999);
    }

    // ========== Tests pour supprimerTousLesEnfantsDuSejour() ==========

    @Test
    @DisplayName("supprimerTousLesEnfantsDuSejour - Devrait retourner 204 No Content")
    void supprimerTousLesEnfantsDuSejour_ShouldReturn204NoContent() throws Exception {
        doNothing().when(enfantService).supprimerTousLesEnfantsDuSejour(1);

        mockMvc.perform(delete("/api/v1/sejours/1/enfants/all"))
                .andExpect(status().isNoContent());

        verify(enfantService).supprimerTousLesEnfantsDuSejour(1);
    }

    @Test
    @DisplayName("supprimerTousLesEnfantsDuSejour - Devrait retourner 404 Not Found si le séjour n'existe pas")
    void supprimerTousLesEnfantsDuSejour_ShouldReturn404WhenSejourNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Séjour non trouvé avec l'ID: 999"))
                .when(enfantService).supprimerTousLesEnfantsDuSejour(999);

        mockMvc.perform(delete("/api/v1/sejours/999/enfants/all"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Séjour non trouvé avec l'ID: 999"));

        verify(enfantService).supprimerTousLesEnfantsDuSejour(999);
    }

    // ========== Tests pour importerEnfantsDepuisExcel() ==========

    @Test
    @DisplayName("importerEnfantsDepuisExcel - Devrait retourner 200 OK avec la réponse complète")
    void importerEnfantsDepuisExcel_ShouldReturn200WithFullResponse() throws Exception {
        MockMultipartFile file = createValidExcelFile();
        when(enfantService.importerEnfantsDepuisExcel(eq(1), any())).thenReturn(excelImportResponse);

        mockMvc.perform(multipart("/api/v1/sejours/1/enfants/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLignes").value(1))
                .andExpect(jsonPath("$.enfantsCrees").value(1))
                .andExpect(jsonPath("$.enfantsDejaExistants").value(0))
                .andExpect(jsonPath("$.erreurs").value(0))
                .andExpect(jsonPath("$.messagesErreur").isArray());

        verify(enfantService).importerEnfantsDepuisExcel(eq(1), any());
    }

    @Test
    @DisplayName("importerEnfantsDepuisExcel - Devrait retourner 400 Bad Request si le fichier est vide")
    void importerEnfantsDepuisExcel_ShouldReturn400WhenFileEmpty() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/v1/sejours/1/enfants/import").file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Le fichier Excel est vide"));

        verify(enfantService, never()).importerEnfantsDepuisExcel(anyInt(), any());
    }

    @Test
    @DisplayName("importerEnfantsDepuisExcel - Devrait retourner 400 Bad Request si le format est invalide")
    void importerEnfantsDepuisExcel_ShouldReturn400WhenInvalidFormat() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "contenu texte".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/sejours/1/enfants/import").file(invalidFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Le fichier doit être un fichier Excel (.xlsx ou .xls)"));

        verify(enfantService, never()).importerEnfantsDepuisExcel(anyInt(), any());
    }

    @Test
    @DisplayName("importerEnfantsDepuisExcel - Devrait retourner 200 OK avec erreurs pour colonnes manquantes")
    void importerEnfantsDepuisExcel_ShouldReturn200WithErrorsWhenColumnsMissing() throws Exception {
        MockMultipartFile file = createExcelFileWithMissingColumns();
        ExcelImportResponse responseWithErrors = new ExcelImportResponse(
                0,
                0,
                0,
                1,
                List.of("Colonne contenant 'nom' introuvable.")
        );

        when(enfantService.importerEnfantsDepuisExcel(eq(1), any())).thenReturn(responseWithErrors);

        mockMvc.perform(multipart("/api/v1/sejours/1/enfants/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLignes").value(0))
                .andExpect(jsonPath("$.erreurs").value(1))
                .andExpect(jsonPath("$.messagesErreur").isArray())
                .andExpect(jsonPath("$.messagesErreur.length()").value(1));

        verify(enfantService).importerEnfantsDepuisExcel(eq(1), any());
    }

    @Test
    @DisplayName("importerEnfantsDepuisExcel - Devrait retourner 404 Not Found si le séjour n'existe pas")
    void importerEnfantsDepuisExcel_ShouldReturn404WhenSejourNotFound() throws Exception {
        MockMultipartFile file = createValidExcelFile();
        when(enfantService.importerEnfantsDepuisExcel(eq(999), any()))
                .thenThrow(new ResourceNotFoundException("Séjour non trouvé avec l'ID: 999"));

        mockMvc.perform(multipart("/api/v1/sejours/999/enfants/import").file(file))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Séjour non trouvé avec l'ID: 999"));

        verify(enfantService).importerEnfantsDepuisExcel(eq(999), any());
    }

    // ========== Helpers ==========

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
