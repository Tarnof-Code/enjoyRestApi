package com.tarnof.enjoyrestapi.utils;

import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

/**
 * Classe utilitaire pour le traitement des fichiers Excel
 */
public class ExcelHelper {

    private static final DataFormatter DATA_FORMATTER = new DataFormatter(Locale.US);

    private ExcelHelper() {
        // Classe utilitaire, constructeur privé pour empêcher l'instanciation
    }

    /**
     * Détecte les colonnes en analysant les en-têtes de la première ligne.
     * Chaque colonne a des groupes de mots-clés : tous les groupes doivent matcher (ET),
     * au moins un mot par groupe (OU).
     * @param headerRow La première ligne contenant les en-têtes
     * @param columnMappings Map associant le nom de colonne à des groupes de mots-clés
     *                       (ex: "emailParent1" -> [["email","mail"], ["parent"], ["1"]])
     * @return Une map associant les noms de colonnes normalisés aux indices de colonnes
     */
    public static Map<String, Integer> detectColumns(Row headerRow, Map<String, String[][]> columnMappings) {
        Map<String, Integer> columnMap = new HashMap<>();
        
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String headerValue = normalizeColumnName(getCellValueAsString(headerRow, i));
                
                for (Map.Entry<String, String[][]> entry : columnMappings.entrySet()) {
                    String columnName = entry.getKey();
                    String[][] groups = entry.getValue();
                    
                    if (!columnMap.containsKey(columnName) && containsAllGroups(headerValue, groups)) {
                        columnMap.put(columnName, i);
                        break;
                    }
                }
            }
        }
        
        return columnMap;
    }
    
    /**
     * Normalise le nom d'une colonne pour faciliter la comparaison
     * Supprime les accents, convertit en minuscules, supprime les espaces
     * @param columnName Le nom de colonne à normaliser
     * @return Le nom de colonne normalisé
     */
    public static String normalizeColumnName(String columnName) {
        if (columnName == null) {
            return "";
        }
        // Supprimer les accents
        String normalized = Normalizer.normalize(columnName, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        // Convertir en minuscules et supprimer les espaces
        return normalized.toLowerCase().trim().replaceAll("\\s+", "");
    }
    
    /**
     * Vérifie si un nom de colonne normalisé contient l'un des mots-clés attendus (OU).
     * @param normalizedName Le nom de colonne normalisé
     * @param keywords Les mots-clés à rechercher (au moins un doit être présent)
     * @return true si le nom contient l'un des mots-clés
     */
    public static boolean containsKeyword(String normalizedName, String... keywords) {
        if (normalizedName == null || normalizedName.isEmpty()) {
            return false;
        }
        for (String keyword : keywords) {
            if (normalizedName.contains(normalizeColumnName(keyword))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vérifie si un nom de colonne contient au moins un mot de chaque groupe (ET entre groupes, OU dans un groupe).
     * @param normalizedName Le nom de colonne normalisé
     * @param groups Groupes de mots-clés (chaque groupe = alternatives)
     * @return true si tous les groupes ont au moins une correspondance
     */
    public static boolean containsAllGroups(String normalizedName, String[][] groups) {
        if (normalizedName == null || normalizedName.isEmpty() || groups == null || groups.length == 0) {
            return false;
        }
        for (String[] group : groups) {
            if (!containsKeyword(normalizedName, group)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Vérifie si une ligne est vide (toutes les cellules requises sont vides ou null)
     * @param row La ligne à vérifier
     * @param columnMap La map des colonnes
     * @return true si la ligne est vide, false sinon
     */
    public static boolean isRowEmpty(Row row, Map<String, Integer> columnMap) {
        if (row == null) {
            return true;
        }
        
        // Vérifier si toutes les cellules requises sont vides
        for (String columnName : columnMap.keySet()) {
            Integer cellIndex = columnMap.get(columnName);
            if (cellIndex != null) {
                String cellValue = getCellValueAsString(row, cellIndex);
                if (cellValue != null && !cellValue.trim().isEmpty()) {
                    // Au moins une cellule requise contient une valeur, la ligne n'est pas vide
                    return false;
                }
            }
        }
        
        // Toutes les cellules requises sont vides, la ligne est considérée comme vide
        return true;
    }
    
    /**
     * Récupère la valeur d'une cellule sous forme de chaîne de caractères
     * @param row La ligne contenant la cellule
     * @param cellIndex L'index de la cellule
     * @return La valeur de la cellule sous forme de chaîne, ou null si la cellule est vide
     */
    public static String getCellValueAsString(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    return dateFormat.format(cell.getDateCellValue());
                } else {
                    return DATA_FORMATTER.formatCellValue(cell);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return DATA_FORMATTER.formatCellValue(cell);
            default:
                return null;
        }
    }

    /**
     * Parse une chaîne de caractères en Date, en essayant plusieurs formats
     * @param dateStr La chaîne à parser
     * @return La Date correspondante
     * @throws ParseException si aucun format ne correspond
     */
    public static Date parseDateFromString(String dateStr) throws ParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new ParseException("La date ne peut pas être vide", 0);
        }
        
        String trimmedDate = dateStr.trim();
        
        // Essayer le format dd/MM/yyyy
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            return dateFormat.parse(trimmedDate);
        } catch (ParseException e) {
            // Essayer le format yyyy-MM-dd
            try {
                SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
                return dateFormat2.parse(trimmedDate);
            } catch (ParseException e2) {
                // Essayer le format Excel (nombre de jours depuis 1900)
                try {
                    double excelDate = Double.parseDouble(trimmedDate);
                    return org.apache.poi.ss.usermodel.DateUtil.getJavaDate(excelDate);
                } catch (NumberFormatException e3) {
                    throw new ParseException("Format de date invalide: " + dateStr, 0);
                }
            }
        }
    }

    /**
     * Normalise un numéro de téléphone : supprime les séparateurs (espaces, points, tirets),
     * gère le format international (+33, 0033) et rajoute le 0 si manquant (9 chiffres reçus).
     * @param phone Le numéro brut
     * @return Le numéro normalisé (ex: "0612345678"), ou null si vide
     */
    public static String normalizePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        String cleaned = phone.trim().replaceAll("[\\s.\\-]", "");
        if (cleaned.startsWith("+33")) {
            cleaned = "0" + cleaned.substring(3);
        } else if (cleaned.startsWith("0033")) {
            cleaned = "0" + cleaned.substring(4);
        }
        if (cleaned.matches("[1-9][0-9]{8}")) {
            cleaned = "0" + cleaned;
        }
        return cleaned;
    }

    /**
     * Formate une date en format français lisible (dd/MM/yyyy)
     * @param date La date à formater
     * @return La date formatée en chaîne de caractères
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(date);
    }
}
