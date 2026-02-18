package com.tarnof.enjoyrestapi.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tests unitaires pour ExcelHelper")
class ExcelHelperTest {

    // ========== Tests pour normalizeColumnName() ==========

    @Nested
    @DisplayName("normalizeColumnName")
    class NormalizeColumnNameTests {

        @Test
        @DisplayName("Devrait normaliser correctement (suppression accents, espaces, casse)")
        void shouldNormalizeCorrectly() {
            assertThat(ExcelHelper.normalizeColumnName("Nom de l'enfant")).isEqualTo("nomdel'enfant");
            assertThat(ExcelHelper.normalizeColumnName("  Prénom  ")).isEqualTo("prenom");
            assertThat(ExcelHelper.normalizeColumnName("DATE NAISSANCE")).isEqualTo("datenaissance");
            assertThat(ExcelHelper.normalizeColumnName("Éléphant")).isEqualTo("elephant");
        }

        @Test
        @DisplayName("Devrait retourner chaîne vide pour null")
        void shouldReturnEmptyForNull() {
            assertThat(ExcelHelper.normalizeColumnName(null)).isEmpty();
        }

        @Test
        @DisplayName("Devrait gérer les chaînes vides")
        void shouldHandleEmptyString() {
            assertThat(ExcelHelper.normalizeColumnName("")).isEmpty();
            assertThat(ExcelHelper.normalizeColumnName("   ")).isEmpty();
        }

        @Test
        @DisplayName("Devrait supprimer les espaces multiples")
        void shouldRemoveMultipleSpaces() {
            assertThat(ExcelHelper.normalizeColumnName("Nom   Prenom")).isEqualTo("nomprenom");
        }
    }

    // ========== Tests pour containsKeyword() ==========

    @Nested
    @DisplayName("containsKeyword")
    class ContainsKeywordTests {

        @Test
        @DisplayName("Devrait détecter un mot-clé simple")
        void shouldDetectSimpleKeyword() {
            assertThat(ExcelHelper.containsKeyword("nomdel'enfant", "nom")).isTrue();
            assertThat(ExcelHelper.containsKeyword("date de naissance", "naissance")).isTrue();
        }

        @Test
        @DisplayName("Devrait détecter quand au moins un des mots-clés est présent")
        void shouldDetectWhenOneKeywordMatches() {
            assertThat(ExcelHelper.containsKeyword("genre", "genre", "sexe")).isTrue();
            assertThat(ExcelHelper.containsKeyword("sexe", "genre", "sexe")).isTrue();
        }

        @Test
        @DisplayName("Devrait retourner false quand aucun mot-clé ne correspond")
        void shouldReturnFalseWhenNoKeywordMatches() {
            assertThat(ExcelHelper.containsKeyword("nom", "prenom", "date")).isFalse();
        }

        @Test
        @DisplayName("Devrait gérer les mots-clés avec accents")
        void shouldHandleKeywordsWithAccents() {
            // La normalisation supprime les accents : "Prénom" et "prénom" -> "prenom"
            // Scénario : en-tête "Prénom enfant" (normalisé "prenomenfant") contient mot-clé "prenom"
            assertThat(ExcelHelper.containsKeyword("prenomenfant", "prenom")).isTrue();
            // Même logique avec mot-clé accentué (normalisé identiquement)
            assertThat(ExcelHelper.containsKeyword("prenomenfant", "pr\u00e9nom")).isTrue();
        }

        @Test
        @DisplayName("Devrait retourner false pour null ou vide")
        void shouldReturnFalseForNullOrEmpty() {
            assertThat(ExcelHelper.containsKeyword(null, "nom")).isFalse();
            assertThat(ExcelHelper.containsKeyword("", "nom")).isFalse();
            assertThat(ExcelHelper.containsKeyword("   ", "nom")).isFalse();
        }
    }

    // ========== Tests pour detectColumns() ==========

    @Nested
    @DisplayName("detectColumns")
    class DetectColumnsTests {

        @Test
        @DisplayName("Devrait détecter les colonnes avec noms exacts")
        void shouldDetectExactColumnNames() throws Exception {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Test");
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Nom");
                headerRow.createCell(1).setCellValue("Prénom");
                headerRow.createCell(2).setCellValue("Genre");

                Map<String, String[]> columnMappings = new HashMap<>();
                columnMappings.put("nom", new String[]{"nom"});
                columnMappings.put("prenom", new String[]{"prenom"});
                columnMappings.put("genre", new String[]{"genre", "sexe"});

                Map<String, Integer> result = ExcelHelper.detectColumns(headerRow, columnMappings);

                assertThat(result).containsEntry("nom", 0);
                assertThat(result).containsEntry("prenom", 1);
                assertThat(result).containsEntry("genre", 2);
                assertThat(result).hasSize(3);
            }
        }

        @Test
        @DisplayName("Devrait détecter les colonnes contenant des mots-clés")
        void shouldDetectColumnsWithKeywords() throws Exception {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Test");
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Nom de l'enfant");
                headerRow.createCell(1).setCellValue("Date de naissance");

                Map<String, String[]> columnMappings = new HashMap<>();
                columnMappings.put("nom", new String[]{"nom"});
                columnMappings.put("dateNaissance", new String[]{"date", "naissance"});

                Map<String, Integer> result = ExcelHelper.detectColumns(headerRow, columnMappings);

                assertThat(result).containsEntry("nom", 0);
                assertThat(result).containsEntry("dateNaissance", 1);
            }
        }

        @Test
        @DisplayName("Devrait retourner une map vide pour colonnes manquantes")
        void shouldReturnEmptyMapForMissingColumns() throws Exception {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Test");
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Colonne Invalide");
                headerRow.createCell(1).setCellValue("Autre");

                Map<String, String[]> columnMappings = new HashMap<>();
                columnMappings.put("nom", new String[]{"nom"});
                columnMappings.put("prenom", new String[]{"prenom"});

                Map<String, Integer> result = ExcelHelper.detectColumns(headerRow, columnMappings);

                assertThat(result).isEmpty();
            }
        }

        @Test
        @DisplayName("Devrait gérer les colonnes avec accents et espaces")
        void shouldHandleColumnsWithAccentsAndSpaces() throws Exception {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Test");
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("  Prénom enfant  ");
                headerRow.createCell(1).setCellValue("Niveau scolaire");

                Map<String, String[]> columnMappings = new HashMap<>();
                columnMappings.put("prenom", new String[]{"prenom"});
                columnMappings.put("niveauScolaire", new String[]{"niveau", "classe"});

                Map<String, Integer> result = ExcelHelper.detectColumns(headerRow, columnMappings);

                assertThat(result).containsEntry("prenom", 0);
                assertThat(result).containsEntry("niveauScolaire", 1);
            }
        }
    }

    // ========== Tests pour isRowEmpty() ==========

    @Nested
    @DisplayName("isRowEmpty")
    class IsRowEmptyTests {

        @Test
        @DisplayName("Devrait détecter une ligne vide")
        void shouldDetectEmptyRow() throws Exception {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Test");
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Nom");
                headerRow.createCell(1).setCellValue("Prénom");

                Row dataRow = sheet.createRow(1);
                dataRow.createCell(0).setCellValue("");
                dataRow.createCell(1).setCellValue("");

                Map<String, Integer> columnMap = new HashMap<>();
                columnMap.put("nom", 0);
                columnMap.put("prenom", 1);

                assertThat(ExcelHelper.isRowEmpty(dataRow, columnMap)).isTrue();
            }
        }

        @Test
        @DisplayName("Devrait détecter une ligne avec données")
        void shouldDetectRowWithData() throws Exception {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Test");
                Row dataRow = sheet.createRow(0);
                dataRow.createCell(0).setCellValue("Martin");
                dataRow.createCell(1).setCellValue("Emma");

                Map<String, Integer> columnMap = new HashMap<>();
                columnMap.put("nom", 0);
                columnMap.put("prenom", 1);

                assertThat(ExcelHelper.isRowEmpty(dataRow, columnMap)).isFalse();
            }
        }

        @Test
        @DisplayName("Devrait retourner true pour row null")
        void shouldReturnTrueForNullRow() {
            Map<String, Integer> columnMap = new HashMap<>();
            columnMap.put("nom", 0);

            assertThat(ExcelHelper.isRowEmpty(null, columnMap)).isTrue();
        }

        @Test
        @DisplayName("Devrait considérer comme vide une ligne avec cellules null")
        void shouldConsiderEmptyWhenCellsAreNull() throws Exception {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Test");
                Row dataRow = sheet.createRow(0);
                // Pas de cellules créées - getCell retournera null

                Map<String, Integer> columnMap = new HashMap<>();
                columnMap.put("nom", 0);
                columnMap.put("prenom", 1);

                assertThat(ExcelHelper.isRowEmpty(dataRow, columnMap)).isTrue();
            }
        }
    }

    // ========== Tests pour getCellValueAsString() ==========

    @Nested
    @DisplayName("getCellValueAsString")
    class GetCellValueAsStringTests {

        @Test
        @DisplayName("Devrait extraire STRING")
        void shouldExtractString() throws Exception {
            try (Workbook workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet("Test").createRow(0);
                row.createCell(0).setCellValue("Martin");

                assertThat(ExcelHelper.getCellValueAsString(row, 0)).isEqualTo("Martin");
            }
        }

        @Test
        @DisplayName("Devrait extraire NUMERIC entier")
        void shouldExtractNumericInteger() throws Exception {
            try (Workbook workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet("Test").createRow(0);
                row.createCell(0).setCellValue(42);

                assertThat(ExcelHelper.getCellValueAsString(row, 0)).isEqualTo("42");
            }
        }

        @Test
        @DisplayName("Devrait extraire NUMERIC décimal")
        void shouldExtractNumericDecimal() throws Exception {
            try (Workbook workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet("Test").createRow(0);
                row.createCell(0).setCellValue(3.14);

                assertThat(ExcelHelper.getCellValueAsString(row, 0)).isEqualTo("3.14");
            }
        }

        @Test
        @DisplayName("Devrait extraire NUMERIC date formaté")
        void shouldExtractNumericDateFormatted() throws Exception {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Test");
                Row row = sheet.createRow(0);
                Cell cell = row.createCell(0);
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(2014, 2, 15); // 15 mars 2014
                cell.setCellValue(cal.getTime());

                var style = workbook.createCellStyle();
                style.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));
                cell.setCellStyle(style);

                String result = ExcelHelper.getCellValueAsString(row, 0);
                assertThat(result).isNotEmpty();
                assertThat(result).isEqualTo("15/03/2014");
            }
        }

        @Test
        @DisplayName("Devrait extraire BOOLEAN")
        void shouldExtractBoolean() throws Exception {
            try (Workbook workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet("Test").createRow(0);
                row.createCell(0).setCellValue(true);
                assertThat(ExcelHelper.getCellValueAsString(row, 0)).isEqualTo("true");
            }
        }

        @Test
        @DisplayName("Devrait extraire FORMULA")
        void shouldExtractFormula() throws Exception {
            try (Workbook workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet("Test").createRow(0);
                row.createCell(0).setCellFormula("A1+B1");

                assertThat(ExcelHelper.getCellValueAsString(row, 0)).isEqualTo("A1+B1");
            }
        }

        @Test
        @DisplayName("Devrait retourner null pour cellule null")
        void shouldReturnNullForNullCell() throws Exception {
            try (Workbook workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet("Test").createRow(0);
                // Pas de cellule à l'index 5
                assertThat(ExcelHelper.getCellValueAsString(row, 5)).isNull();
            }
        }
    }

    // ========== Tests pour parseDateFromString() ==========

    @Nested
    @DisplayName("parseDateFromString")
    class ParseDateFromStringTests {

        @Test
        @DisplayName("Devrait parser le format dd/MM/yyyy")
        void shouldParseFormatDdMmYyyy() throws ParseException {
            Date date = ExcelHelper.parseDateFromString("15/03/2014");
            assertThat(date).isNotNull();
            assertThat(ExcelHelper.formatDate(date)).isEqualTo("15/03/2014");
        }

        @Test
        @DisplayName("Devrait parser le format yyyy-MM-dd")
        void shouldParseFormatYyyyMmDd() throws ParseException {
            Date date = ExcelHelper.parseDateFromString("2014-03-15");
            assertThat(date).isNotNull();
            assertThat(ExcelHelper.formatDate(date)).isEqualTo("15/03/2014");
        }

        @Test
        @DisplayName("Devrait parser le format Excel numérique")
        void shouldParseExcelNumericFormat() throws ParseException {
            // 41700 correspond environ au 15/03/2014 en format Excel
            Date date = ExcelHelper.parseDateFromString("41700");
            assertThat(date).isNotNull();
        }

        @Test
        @DisplayName("Devrait lancer ParseException pour format invalide")
        void shouldThrowParseExceptionForInvalidFormat() {
            assertThatThrownBy(() -> ExcelHelper.parseDateFromString("invalid-date"))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Format de date invalide");
        }

        @Test
        @DisplayName("Devrait lancer ParseException pour valeur null")
        void shouldThrowParseExceptionForNull() {
            assertThatThrownBy(() -> ExcelHelper.parseDateFromString(null))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("ne peut pas être vide");
        }

        @Test
        @DisplayName("Devrait lancer ParseException pour valeur vide")
        void shouldThrowParseExceptionForEmpty() {
            assertThatThrownBy(() -> ExcelHelper.parseDateFromString(""))
                    .isInstanceOf(ParseException.class);
            assertThatThrownBy(() -> ExcelHelper.parseDateFromString("   "))
                    .isInstanceOf(ParseException.class);
        }
    }

    // ========== Tests pour formatDate() ==========

    @Nested
    @DisplayName("formatDate")
    class FormatDateTests {

        @Test
        @DisplayName("Devrait formater correctement en dd/MM/yyyy")
        void shouldFormatCorrectly() throws ParseException {
            Date date = ExcelHelper.parseDateFromString("15/03/2014");
            assertThat(ExcelHelper.formatDate(date)).isEqualTo("15/03/2014");
        }

        @Test
        @DisplayName("Devrait retourner chaîne vide pour date null")
        void shouldReturnEmptyForNullDate() {
            assertThat(ExcelHelper.formatDate(null)).isEmpty();
        }
    }
}
