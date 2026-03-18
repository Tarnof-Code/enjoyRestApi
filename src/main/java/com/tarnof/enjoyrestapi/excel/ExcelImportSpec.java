package com.tarnof.enjoyrestapi.excel;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.tarnof.enjoyrestapi.payload.response.ExcelImportColumnSpec;
import com.tarnof.enjoyrestapi.payload.response.ExcelImportSpecResponse;

/**
 * Spécification centralisée des colonnes pour l'import Excel des enfants.
 * Source unique de vérité : toute modification des colonnes ou de leurs noms
 * se fait ici et s'applique automatiquement à l'import et à l'API de notice.
 */
public final class ExcelImportSpec {

    private static final List<String> FORMATS_ACCEPTES = List.of(".xlsx", ".xls");

    /**
     * Définition d'une colonne (interne).
     * groupesMotsCles : chaque groupe = alternatives (OU), tous les groupes doivent matcher (ET).
     * Ex: [["email","mail"], ["parent"], ["1"]] = (email OU mail) ET parent ET 1
     */
    private record ColumnDef(String champ, String libelle, String[][] groupesMotsCles, boolean obligatoire) {}

    private static final List<ColumnDef> COLUMNS = List.of(
        // Colonnes obligatoires (1 groupe = 1 ou plusieurs alternatives)
        new ColumnDef("nom", "Nom", new String[][]{{"nom"}}, true),
        new ColumnDef("prenom", "Prénom", new String[][]{{"prenom"}}, true),
        new ColumnDef("genre", "Genre", new String[][]{{"genre", "sexe"}}, true),
        new ColumnDef("dateNaissance", "Date de naissance", new String[][]{{"naissance"}}, true),
        new ColumnDef("niveauScolaire", "Niveau scolaire", new String[][]{{"niveau", "classe"}}, true),
        // Colonnes optionnelles : email/mail ET parent ET 1 ou 2
        new ColumnDef("emailParent1", "Email parent 1", new String[][]{{"email", "mail"}, {"parent"}, {"1"}}, false),
        new ColumnDef("telephoneParent1", "Téléphone parent 1", new String[][]{{"telephone", "tel"}, {"parent"}, {"1"}}, false),
        new ColumnDef("emailParent2", "Email parent 2", new String[][]{{"email", "mail"}, {"parent"}, {"2"}}, false),
        new ColumnDef("telephoneParent2", "Téléphone parent 2", new String[][]{{"telephone", "tel"}, {"parent"}, {"2"}}, false),
        new ColumnDef("informationsMedicales", "Informations médicales", new String[][]{{"medicales", "medicale", "medical", "sanitaire","sanitaires"}}, false),
        new ColumnDef("pai", "PAI", new String[][]{{"pai"}}, false),
        new ColumnDef("informationsAlimentaires", "Informations alimentaires", new String[][]{{"alimentaire", "alimentaires"}}, false),
        new ColumnDef("traitementMatin", "Traitement matin", new String[][]{{"traitement", "medicament","traitements", "medicaments"}, {"matin"}}, false),
        new ColumnDef("traitementMidi", "Traitement midi", new String[][]{{"traitement", "medicament","traitements", "medicaments"}, {"midi"}}, false),
        new ColumnDef("traitementSoir", "Traitement soir", new String[][]{{"traitement", "medicament","traitements", "medicaments"}, {"soir"}}, false),
        new ColumnDef("traitementSiBesoin", "Traitement si besoin", new String[][]{{"traitement", "medicament","traitements", "medicaments"}, {"besoin"}}, false),
        new ColumnDef("autresInformations", "Autres informations", new String[][]{{"autres", "autre", "divers","diverses"}}, false),
        new ColumnDef("aPrendreEnSortie", "À prendre en sortie", new String[][]{{"sortie", "sorties"}}, false)
    );

    private static final ExcelImportSpec INSTANCE = new ExcelImportSpec();

    private ExcelImportSpec() {}

    /**
     * Retourne l'instance singleton de la spécification.
     */
    public static ExcelImportSpec getInstance() {
        return INSTANCE;
    }

    /**
     * Retourne les mappings de colonnes pour ExcelHelper.detectColumns().
     * Chaque colonne a des groupes : tous les groupes doivent matcher (ET),
     * au moins un mot par groupe (OU).
     */
    public Map<String, String[][]> getColumnMappings() {
        Map<String, String[][]> mappings = new HashMap<>();
        for (ColumnDef col : COLUMNS) {
            mappings.put(col.champ(), col.groupesMotsCles());
        }
        return mappings;
    }

    /**
     * Retourne la liste des clés de colonnes obligatoires.
     */
    public List<String> getRequiredColumnKeys() {
        return COLUMNS.stream()
                .filter(ColumnDef::obligatoire)
                .map(ColumnDef::champ)
                .toList();
    }

    /**
     * Génère le message d'erreur pour une colonne obligatoire manquante.
     */
    public String getErrorMessageForMissingColumn(String columnKey) {
        return COLUMNS.stream()
                .filter(c -> c.champ().equals(columnKey) && c.obligatoire())
                .findFirst()
                .map(col -> {
                    String desc = formatGroupesForMessage(col.groupesMotsCles());
                    return "Colonne " + desc + " introuvable. Le fichier Excel doit contenir une colonne correspondante (ex: '" + col.libelle() + "').";
                })
                .orElse("Colonne '" + columnKey + "' introuvable.");
    }

    /**
     * Génère le message récapitulatif pour les colonnes manquantes.
     */
    public String getSummaryErrorMessage() {
        String colonnes = COLUMNS.stream()
                .filter(ColumnDef::obligatoire)
                .map(c -> "une colonne " + formatGroupesForMessage(c.groupesMotsCles()))
                .collect(Collectors.joining(", "));
        return "Structure du fichier Excel attendue : Le fichier doit contenir les colonnes suivantes dans la première ligne (en-têtes) : "
                + colonnes
                + ". Les noms des colonnes peuvent contenir d'autres mots (ex: 'Nom de l'enfant' est accepté).";
    }

    private static String formatGroupesForMessage(String[][] groupes) {
        return Arrays.stream(groupes)
                .map(g -> "(" + String.join(" ou ", g) + ")")
                .collect(Collectors.joining(" ET "));
    }

    /**
     * Retourne la spécification formatée pour l'API frontend.
     */
    public ExcelImportSpecResponse getSpecForApi() {
        List<ExcelImportColumnSpec> obligatoires = COLUMNS.stream()
                .filter(ColumnDef::obligatoire)
                .map(ExcelImportSpec::toColumnSpec)
                .toList();

        List<ExcelImportColumnSpec> optionnelles = COLUMNS.stream()
                .filter(c -> !c.obligatoire())
                .map(ExcelImportSpec::toColumnSpec)
                .toList();

        return new ExcelImportSpecResponse(
                obligatoires,
                optionnelles,
                Collections.unmodifiableList(FORMATS_ACCEPTES)
        );
    }

    private static ExcelImportColumnSpec toColumnSpec(ColumnDef def) {
        List<String> motsCles = Arrays.stream(def.groupesMotsCles())
                .map(g -> g.length == 1 ? g[0] : "(" + String.join(" ou ", g) + ")")
                .toList();
        return new ExcelImportColumnSpec(
                def.champ(),
                def.libelle(),
                motsCles,
                def.obligatoire()
        );
    }
}
