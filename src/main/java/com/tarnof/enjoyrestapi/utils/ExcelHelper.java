package com.tarnof.enjoyrestapi.utils;

import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 * Classe utilitaire pour le traitement des fichiers Excel
 */
public class ExcelHelper {

    private ExcelHelper() {
        // Classe utilitaire, constructeur privé pour empêcher l'instanciation
    }

    /**
     * Détecte les colonnes en analysant les en-têtes de la première ligne de manière générique
     * @param headerRow La première ligne contenant les en-têtes
     * @param columnMappings Map associant le nom de colonne normalisé à un tableau de mots-clés à rechercher
     *                       (ex: "nom" -> ["nom"], "dateNaissance" -> ["date", "naissance"])
     * @return Une map associant les noms de colonnes normalisés aux indices de colonnes
     */
    public static Map<String, Integer> detectColumns(Row headerRow, Map<String, String[]> columnMappings) {
        Map<String, Integer> columnMap = new HashMap<>();
        
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String headerValue = normalizeColumnName(getCellValueAsString(headerRow, i));
                
                // Parcourir les mappings pour trouver une correspondance
                for (Map.Entry<String, String[]> entry : columnMappings.entrySet()) {
                    String columnName = entry.getKey();
                    String[] keywords = entry.getValue();
                    
                    // Si la colonne n'a pas encore été trouvée et que le header correspond
                    if (!columnMap.containsKey(columnName) && containsKeyword(headerValue, keywords)) {
                        columnMap.put(columnName, i);
                        break; // Passer à la colonne suivante une fois qu'une correspondance est trouvée
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
     * Vérifie si un nom de colonne normalisé contient l'un des mots-clés attendus
     * Permet une détection flexible (ex: "nom de l'enfant" sera détecté car il contient "nom")
     * @param normalizedName Le nom de colonne normalisé
     * @param keywords Les mots-clés à rechercher (au moins un doit être présent)
     * @return true si le nom contient l'un des mots-clés
     */
    public static boolean containsKeyword(String normalizedName, String... keywords) {
        if (normalizedName == null || normalizedName.isEmpty()) {
            return false;
        }
        
        for (String keyword : keywords) {
            String normalizedKeyword = normalizeColumnName(keyword);
            // Vérifie si le nom de colonne contient le mot-clé
            if (normalizedName.contains(normalizedKeyword)) {
                return true;
            }
        }
        return false;
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
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    return dateFormat.format(cell.getDateCellValue());
                } else {
                    // Convertir le nombre en entier si c'est un entier, sinon en décimal
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
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
