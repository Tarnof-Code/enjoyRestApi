package com.tarnof.enjoyrestapi.services.impl;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tarnof.enjoyrestapi.enums.Genre;
import com.tarnof.enjoyrestapi.enums.NiveauScolaire;
import com.tarnof.enjoyrestapi.payload.response.EnfantDto;
import com.tarnof.enjoyrestapi.payload.response.ExcelImportResponse;
import com.tarnof.enjoyrestapi.entities.Enfant;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.SejourEnfant;
import com.tarnof.enjoyrestapi.entities.SejourEnfantId;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.CreateEnfantRequest;
import com.tarnof.enjoyrestapi.repositories.EnfantRepository;
import com.tarnof.enjoyrestapi.repositories.SejourRepository;
import com.tarnof.enjoyrestapi.repositories.SejourEnfantRepository;
import com.tarnof.enjoyrestapi.services.EnfantService;
import com.tarnof.enjoyrestapi.utils.ExcelHelper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnfantServiceImpl implements EnfantService {

    private final EnfantRepository enfantRepository;
    private final SejourRepository sejourRepository;
    private final SejourEnfantRepository sejourEnfantRepository;

    @Override
    @Transactional
    public void creerEtAjouterEnfantAuSejour(int sejourId, CreateEnfantRequest request) {
        Sejour sejour = sejourRepository.findById(sejourId)
                .orElseThrow(() -> new ResourceNotFoundException("Séjour non trouvé avec l'ID: " + sejourId));
        
        if (sejour.getEnfants() == null) {
            sejour.setEnfants(new ArrayList<>());
        }
        
        Enfant enfantExistant = enfantRepository.findByNomAndPrenomAndGenreAndDateNaissance(
                request.nom(),
                request.prenom(),
                request.genre(),
                request.dateNaissance()
        ).orElse(null);
        
        Enfant enfantSauvegarde;
        
        if (enfantExistant != null) {
            enfantSauvegarde = enfantExistant;
            
            boolean dejaDansSejour = sejour.getEnfants().stream()
                    .anyMatch(se -> se.getEnfant().getId() == enfantSauvegarde.getId());
            
            if (dejaDansSejour) {
                String nee = request.genre() == Genre.Féminin ? "née" : "né";
                throw new ResourceAlreadyExistsException(
                    request.prenom() + " " + request.nom() + " " + nee + " le " + 
                    ExcelHelper.formatDate(request.dateNaissance()) + " existe déjà dans ce séjour"
                );
            }
        } else {
            Enfant enfant = Enfant.builder()
                    .nom(request.nom())
                    .prenom(request.prenom())
                    .genre(request.genre())
                    .dateNaissance(request.dateNaissance())
                    .niveauScolaire(request.niveauScolaire())
                    .build();
            
            @SuppressWarnings("null")
            Enfant enfantCree = enfantRepository.save(enfant);
            enfantSauvegarde = enfantCree;
        }
        
        SejourEnfant sejourEnfant = SejourEnfant.builder()
                .sejour(sejour)
                .enfant(enfantSauvegarde)
                .build();
        
        sejour.getEnfants().add(sejourEnfant);
        sejourRepository.save(sejour);
    }

    @Override
    @Transactional
    public EnfantDto modifierEnfant(int sejourId, int enfantId, CreateEnfantRequest request) {
        SejourEnfantId sejourEnfantId = new SejourEnfantId(sejourId, enfantId);
        SejourEnfant sejourEnfant = sejourEnfantRepository.findById(sejourEnfantId)
                .orElseThrow(() -> new ResourceNotFoundException("L'enfant n'est pas inscrit à ce séjour"));
        
        Enfant enfantActuel = sejourEnfant.getEnfant();
        Sejour sejour = sejourEnfant.getSejour();
        
        Enfant enfantExistant = enfantRepository.findByNomAndPrenomAndGenreAndDateNaissance(
                request.nom(),
                request.prenom(),
                request.genre(),
                request.dateNaissance()
        ).orElse(null);
        
        if (enfantExistant != null && enfantExistant.getId() != enfantActuel.getId()) {
            boolean dejaDansSejour = sejour.getEnfants() != null && sejour.getEnfants().stream()
                    .anyMatch(se -> se.getEnfant().getId() == enfantExistant.getId());
            
            if (dejaDansSejour) {
                String nee = request.genre() == Genre.Féminin ? "née" : "né";
                throw new ResourceAlreadyExistsException(
                    request.prenom() + " " + request.nom() + " " + nee + " le " + 
                    ExcelHelper.formatDate(request.dateNaissance()) + " existe déjà dans ce séjour"
                );
            } else {
                // L'enfant existant n'est pas dans le séjour, remplacer l'enfant actuel par l'enfant existant
                // Comme la propriété enfant fait partie de la clé primaire, on doit supprimer l'ancienne relation
                // et créer une nouvelle relation avec l'enfant existant
                
                sejourEnfantRepository.delete(sejourEnfant);
                sejourEnfantRepository.flush();
                
                sejour.getEnfants().remove(sejourEnfant);
                
                SejourEnfantId nouveauSejourEnfantId = new SejourEnfantId(sejourId, enfantExistant.getId());
                SejourEnfant nouveauSejourEnfant = SejourEnfant.builder()
                        .sejour(sejour)
                        .enfant(enfantExistant)
                        .build();
                nouveauSejourEnfant.setId(nouveauSejourEnfantId);
                
                sejourEnfantRepository.save(nouveauSejourEnfant);
                
                if (sejour.getEnfants() == null) {
                    sejour.setEnfants(new ArrayList<>());
                }
                sejour.getEnfants().add(nouveauSejourEnfant);
                sejourRepository.save(sejour);
                
                long nombreSejours = sejourEnfantRepository.countByEnfantId(enfantActuel.getId());
                if (nombreSejours == 0) {
                    enfantRepository.delete(enfantActuel);
                    enfantRepository.flush();
                }
                
                return mapToEnfantDto(enfantExistant);
            }
        }
        
        enfantActuel.setNom(request.nom());
        enfantActuel.setPrenom(request.prenom());
        enfantActuel.setGenre(request.genre());
        enfantActuel.setDateNaissance(request.dateNaissance());
        enfantActuel.setNiveauScolaire(request.niveauScolaire());
        
        Enfant enfantModifie = enfantRepository.save(enfantActuel);
        
        return mapToEnfantDto(enfantModifie);
    }

    @Override
    @Transactional
    public void supprimerEnfantDuSejour(int sejourId, int enfantId) {
        SejourEnfantId sejourEnfantId = new SejourEnfantId(sejourId, enfantId);
        SejourEnfant sejourEnfant = sejourEnfantRepository.findById(sejourEnfantId)
                .orElseThrow(() -> new ResourceNotFoundException("L'enfant n'est pas inscrit à ce séjour"));
        
        Enfant enfant = sejourEnfant.getEnfant();
        
        long nombreSejours = sejourEnfantRepository.countByEnfantId(enfantId);
        
        sejourEnfantRepository.deleteById(sejourEnfantId);
        sejourEnfantRepository.flush();
        
        // Si c'était le dernier séjour (ou le seul), supprimer l'enfant de la base de données
        if (nombreSejours <= 1) {
            // enfant ne peut pas être null car récupéré depuis sejourEnfant
            Enfant enfantASupprimer = Objects.requireNonNull(enfant, "L'enfant ne peut pas être null");
            enfantRepository.delete(enfantASupprimer);
            enfantRepository.flush();
        }
    }

    @Override
    @Transactional
    public void supprimerTousLesEnfantsDuSejour(int sejourId) {
        Sejour sejour = sejourRepository.findById(sejourId)
                .orElseThrow(() -> new ResourceNotFoundException("Séjour non trouvé avec l'ID: " + sejourId));
        
        if (sejour.getEnfants() == null || sejour.getEnfants().isEmpty()) {
            return;
        }
        
        List<Enfant> enfantsASupprimer = new ArrayList<>();
        
        for (SejourEnfant sejourEnfant : sejour.getEnfants()) {
            Enfant enfant = sejourEnfant.getEnfant();
            if (enfant != null) {
                // Compter combien de séjours l'enfant a actuellement
                long nombreSejours = sejourEnfantRepository.countByEnfantId(enfant.getId());
                
                // Si c'est le seul séjour (ou le dernier), marquer l'enfant pour suppression
                if (nombreSejours <= 1) {
                    enfantsASupprimer.add(enfant);
                }
            }
        }
        
        // Supprimer toutes les relations SejourEnfant du séjour
        sejour.getEnfants().clear();
        sejourRepository.save(sejour);
        sejourRepository.flush();
        
        for (Enfant enfant : enfantsASupprimer) {
            // enfant ne peut pas être null car vérifié avant d'être ajouté à la liste
            Enfant enfantASupprimer = Objects.requireNonNull(enfant, "L'enfant ne peut pas être null");
            enfantRepository.delete(enfantASupprimer);
        }
        enfantRepository.flush();
    }

    @Override
    public List<EnfantDto> getEnfantsDuSejour(int sejourId) {
        Sejour sejour = sejourRepository.findById(sejourId)
                .orElseThrow(() -> new ResourceNotFoundException("Séjour non trouvé avec l'ID: " + sejourId));
        
        if (sejour.getEnfants() == null || sejour.getEnfants().isEmpty()) {
            return new ArrayList<>();
        }
        
        return sejour.getEnfants().stream()
                .map(SejourEnfant::getEnfant)
                .map(this::mapToEnfantDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ExcelImportResponse importerEnfantsDepuisExcel(int sejourId, MultipartFile file) {
        List<String> messagesErreur = new ArrayList<>();
        int enfantsCrees = 0;
        int enfantsDejaExistants = 0;
        int totalLignes = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Lire la première ligne pour détecter les colonnes
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Le fichier Excel ne contient pas d'en-têtes");
            }
            
            // Définir les mappings de colonnes spécifiques aux enfants
            Map<String, String[]> columnMappings = new HashMap<>();
            columnMappings.put("nom", new String[]{"nom"});
            columnMappings.put("prenom", new String[]{"prenom"});
            columnMappings.put("genre", new String[]{"genre", "sexe"});
            columnMappings.put("dateNaissance", new String[]{"datenaissance", "naissance"});
            columnMappings.put("niveauScolaire", new String[]{"niveau", "classe"});
            
            Map<String, Integer> columnMap = ExcelHelper.detectColumns(headerRow, columnMappings);
            
            // Vérifier que toutes les colonnes requises sont présentes
            boolean colonnesManquantes = false;
            
            if (!columnMap.containsKey("nom")) {
                messagesErreur.add("Colonne contenant 'nom' introuvable. Le fichier Excel doit contenir une colonne avec 'nom' dans son en-tête (ex: 'Nom', 'Nom de l'enfant', etc.)");
                colonnesManquantes = true;
            }
            if (!columnMap.containsKey("prenom")) {
                messagesErreur.add("Colonne contenant 'prénom' introuvable. Le fichier Excel doit contenir une colonne avec 'prénom' dans son en-tête (ex: 'Prénom', 'Prénom de l'enfant', etc.)");
                colonnesManquantes = true;
            }
            if (!columnMap.containsKey("genre")) {
                messagesErreur.add("Colonne contenant 'genre' ou 'sexe' introuvable. Le fichier Excel doit contenir une colonne avec 'genre' ou 'sexe' dans son en-tête (ex: 'Genre', 'Sexe', etc.)");
                colonnesManquantes = true;
            }
            if (!columnMap.containsKey("dateNaissance")) {
                messagesErreur.add("Colonne contenant 'date' et 'naissance' introuvable. Le fichier Excel doit contenir une colonne avec 'date' et 'naissance' dans son en-tête (ex: 'Date de naissance', 'Naissance', etc.)");
                colonnesManquantes = true;
            }
            if (!columnMap.containsKey("niveauScolaire")) {
                messagesErreur.add("Colonne contenant 'niveau' ou 'classe' introuvable. Le fichier Excel doit contenir une colonne avec 'niveau' ou 'classe' dans son en-tête (ex: 'Niveau scolaire', 'Classe', etc.)");
                colonnesManquantes = true;
            }
            
            if (colonnesManquantes) {
                messagesErreur.add(0, "Structure du fichier Excel attendue : Le fichier doit contenir les colonnes suivantes dans la première ligne (en-têtes) : une colonne avec 'nom', une colonne avec 'prénom', une colonne avec 'genre' ou 'sexe', une colonne avec 'date' et 'naissance', et une colonne avec 'niveau' ou 'classe'. Les noms des colonnes peuvent contenir d'autres mots (ex: 'Nom de l'enfant' est accepté).");
                return new ExcelImportResponse(0, 0, 0, messagesErreur.size(), messagesErreur);
            }
            
            // Traiter les lignes de données (à partir de la ligne 2, index 1)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                
                // Vérifier si la ligne est vide (ignore les lignes vides)
                if (ExcelHelper.isRowEmpty(row, columnMap)) {
                    continue;
                }
                
                totalLignes++;
                
                try {
                    // Lire les données de la ligne en utilisant les noms de colonnes
                    String nom = ExcelHelper.getCellValueAsString(row, columnMap.get("nom"));
                    String prenom = ExcelHelper.getCellValueAsString(row, columnMap.get("prenom"));
                    String genreStr = ExcelHelper.getCellValueAsString(row, columnMap.get("genre"));
                    String dateNaissanceStr = ExcelHelper.getCellValueAsString(row, columnMap.get("dateNaissance"));
                    String niveauScolaireStr = ExcelHelper.getCellValueAsString(row, columnMap.get("niveauScolaire"));
                    
                    // Valider que toutes les colonnes sont remplies
                    if (nom == null || nom.trim().isEmpty() ||
                        prenom == null || prenom.trim().isEmpty() ||
                        genreStr == null || genreStr.trim().isEmpty() ||
                        dateNaissanceStr == null || dateNaissanceStr.trim().isEmpty() ||
                        niveauScolaireStr == null || niveauScolaireStr.trim().isEmpty()) {
                        messagesErreur.add("Ligne " + (i + 1) + ": Données incomplètes");
                        continue;
                    }
                    
                    // Parser le genre
                    Genre genre;
                    try {
                        genre = Genre.parseGenre(genreStr.trim());
                    } catch (IllegalArgumentException e) {
                        messagesErreur.add("Ligne " + (i + 1) + ": " + e.getMessage());
                        continue;
                    }
                    
                    // Parser la date de naissance
                    Date dateNaissance;
                    try {
                        dateNaissance = ExcelHelper.parseDateFromString(dateNaissanceStr);
                    } catch (ParseException e) {
                        messagesErreur.add("Ligne " + (i + 1) + ": Format de date invalide (" + dateNaissanceStr + "). Format attendu: dd/MM/yyyy");
                        continue;
                    }
                    
                    // Parser le niveau scolaire
                    NiveauScolaire niveauScolaire;
                    try {
                        niveauScolaire = NiveauScolaire.valueOf(niveauScolaireStr.trim().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        messagesErreur.add("Ligne " + (i + 1) + ": Niveau scolaire invalide (" + niveauScolaireStr + ")");
                        continue;
                    }
                    
                    // Créer la requête
                    CreateEnfantRequest request = new CreateEnfantRequest(
                        nom.trim(),
                        prenom.trim(),
                        genre,
                        dateNaissance,
                        niveauScolaire
                    );
                    
                    // Essayer de créer l'enfant
                    try {
                        creerEtAjouterEnfantAuSejour(sejourId, request);
                        enfantsCrees++;
                    } catch (ResourceAlreadyExistsException e) {
                        enfantsDejaExistants++;
                        messagesErreur.add("Ligne " + (i + 1) + ": " + e.getMessage());
                    } catch (Exception e) {
                        messagesErreur.add("Ligne " + (i + 1) + ": Erreur lors de la création - " + e.getMessage());
                    }
                    
                } catch (Exception e) {
                    messagesErreur.add("Ligne " + (i + 1) + ": Erreur inattendue - " + e.getMessage());
                }
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier Excel: " + e.getMessage(), e);
        }
        
        return new ExcelImportResponse(
            totalLignes,
            enfantsCrees,
            enfantsDejaExistants,
            messagesErreur.size(),
            messagesErreur
        );
    }

    private EnfantDto mapToEnfantDto(Enfant enfant) {
        return new EnfantDto(
            enfant.getId(),
            enfant.getNom(),
            enfant.getPrenom(),
            enfant.getGenre(),
            enfant.getDateNaissance(),
            enfant.getNiveauScolaire()
        );
    }
}
