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
     */
    private record ColumnDef(String champ, String libelle, String[] motsCles, boolean obligatoire) {}

    private static final List<ColumnDef> COLUMNS = List.of(
        // Colonnes obligatoires
        new ColumnDef("nom", "Nom", new String[]{"nom"}, true),
        new ColumnDef("prenom", "Prénom", new String[]{"prenom"}, true),
        new ColumnDef("genre", "Genre", new String[]{"genre", "sexe"}, true),
        new ColumnDef("dateNaissance", "Date de naissance", new String[]{"datenaissance", "naissance"}, true),
        new ColumnDef("niveauScolaire", "Niveau scolaire", new String[]{"niveau", "classe"}, true),
        // Colonnes optionnelles du dossier
        new ColumnDef("emailParent1", "Email parent 1", new String[]{"emailparent1", "emailparent", "mailparent1", "mailparent", "mail", "email"}, false),
        new ColumnDef("telephoneParent1", "Téléphone parent 1", new String[]{"telephoneparent1", "telparent1", "telephoneparent", "telparent", "tel", "tél", "tel1", "tél1", "telephone", "téléphone"}, false),
        new ColumnDef("emailParent2", "Email parent 2", new String[]{"emailparent2", "mailparent2"}, false),
        new ColumnDef("telephoneParent2", "Téléphone parent 2", new String[]{"telephoneparent2", "telparent2", "tel2", "tél2", "téléphone2"}, false),
        new ColumnDef("informationsMedicales", "Informations médicales", new String[]{"informationsmédicales", "informationsmédicale", "infomédicale", "infosmédicales", "médical", "medical", "informationssanitaires", "informationsanitaire", "infossanitaires", "infosanitaire", "sanitaire", "sanitaires"}, false),
        new ColumnDef("pai", "PAI", new String[]{"pai"}, false),
        new ColumnDef("informationsAlimentaires", "Informations alimentaires", new String[]{"informationsalimentaires", "informationsalimentaire", "alimentaire", "alimentaires", "régimealimentaire", "regimealimentaire", "infosalimentaires", "infoalimentaire"}, false),
        new ColumnDef("traitementMatin", "Traitement matin", new String[]{"traitementmatin", "medicamentmatin", "medicamentlematin", "traitementdumatin", "traitementlematin"}, false),
        new ColumnDef("traitementMidi", "Traitement midi", new String[]{"traitementmidi", "medicamentmidi", "traitementdumidi", "traitementlemidi"}, false),
        new ColumnDef("traitementSoir", "Traitement soir", new String[]{"traitementsoir", "medicamentsoir", "traitementdusoir", "traitementlesoir"}, false),
        new ColumnDef("traitementSiBesoin", "Traitement si besoin", new String[]{"traitementsibesoin", "traitementsibésoin", "sibesoin", "sibésoin", "traitementbesoin", "traitementsbesoins"}, false),
        new ColumnDef("autresInformations", "Autres informations", new String[]{"autresinformations", "autresinfo", "autreinfo", "autresinformation", "autre", "autres", "divers"}, false),
        new ColumnDef("aPrendreEnSortie", "À prendre en sortie", new String[]{"aprendreensortie", "sortie", "prendreensortie"}, false)
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
     */
    public Map<String, String[]> getColumnMappings() {
        Map<String, String[]> mappings = new HashMap<>();
        for (ColumnDef col : COLUMNS) {
            mappings.put(col.champ(), col.motsCles());
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
                    String motsClesStr = String.join("' ou '", col.motsCles());
                    return "Colonne contenant '" + motsClesStr + "' introuvable. Le fichier Excel doit contenir une colonne avec '" + motsClesStr + "' dans son en-tête (ex: '" + col.libelle() + "', etc.)";
                })
                .orElse("Colonne '" + columnKey + "' introuvable.");
    }

    /**
     * Génère le message récapitulatif pour les colonnes manquantes.
     */
    public String getSummaryErrorMessage() {
        String colonnes = COLUMNS.stream()
                .filter(ColumnDef::obligatoire)
                .map(c -> "une colonne avec '" + String.join("' ou '", c.motsCles()) + "'")
                .collect(Collectors.joining(", "));
        return "Structure du fichier Excel attendue : Le fichier doit contenir les colonnes suivantes dans la première ligne (en-têtes) : "
                + colonnes
                + ". Les noms des colonnes peuvent contenir d'autres mots (ex: 'Nom de l'enfant' est accepté).";
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
        return new ExcelImportColumnSpec(
                def.champ(),
                def.libelle(),
                Arrays.asList(def.motsCles()),
                def.obligatoire()
        );
    }
}
