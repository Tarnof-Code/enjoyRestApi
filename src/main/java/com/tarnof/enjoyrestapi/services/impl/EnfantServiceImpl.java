package com.tarnof.enjoyrestapi.services.impl;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
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
import com.tarnof.enjoyrestapi.payload.response.DossierEnfantDto;
import com.tarnof.enjoyrestapi.payload.response.EnfantDto;
import com.tarnof.enjoyrestapi.payload.response.ExcelImportResponse;
import com.tarnof.enjoyrestapi.entities.DossierEnfant;
import com.tarnof.enjoyrestapi.entities.Enfant;
import com.tarnof.enjoyrestapi.entities.Groupe;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.SejourEnfant;
import com.tarnof.enjoyrestapi.entities.SejourEnfantId;
import org.springframework.security.access.AccessDeniedException;

import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.CreateEnfantRequest;
import com.tarnof.enjoyrestapi.payload.request.UpdateDossierEnfantRequest;
import com.tarnof.enjoyrestapi.repositories.DossierEnfantRepository;
import com.tarnof.enjoyrestapi.repositories.EnfantRepository;
import com.tarnof.enjoyrestapi.repositories.GroupeRepository;
import com.tarnof.enjoyrestapi.repositories.SejourRepository;
import com.tarnof.enjoyrestapi.repositories.SejourEnfantRepository;
import com.tarnof.enjoyrestapi.excel.ExcelImportSpec;
import com.tarnof.enjoyrestapi.services.EnfantService;
import com.tarnof.enjoyrestapi.utils.ExcelHelper;

import jakarta.transaction.Transactional;

@Service
public class EnfantServiceImpl implements EnfantService {

    private final EnfantRepository enfantRepository;
    private final SejourRepository sejourRepository;
    private final SejourEnfantRepository sejourEnfantRepository;
    private final GroupeRepository groupeRepository;
    private final DossierEnfantRepository dossierEnfantRepository;

    public EnfantServiceImpl(EnfantRepository enfantRepository, SejourRepository sejourRepository,
                             SejourEnfantRepository sejourEnfantRepository, GroupeRepository groupeRepository,
                             DossierEnfantRepository dossierEnfantRepository) {
        this.enfantRepository = enfantRepository;
        this.sejourRepository = sejourRepository;
        this.sejourEnfantRepository = sejourEnfantRepository;
        this.groupeRepository = groupeRepository;
        this.dossierEnfantRepository = dossierEnfantRepository;
    }

    @Override
    @Transactional
    public void creerEtAjouterEnfantAuSejour(int sejourId, CreateEnfantRequest request) {
        creerEtAjouterEnfantAuSejour(sejourId, request, new DossierEnfant());
    }

    @Transactional
    public void creerEtAjouterEnfantAuSejour(int sejourId, CreateEnfantRequest request, DossierEnfant donneesDossier) {
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

            // Créer automatiquement le dossier de l'enfant (vide ou pré-rempli depuis l'import)
            donneesDossier.setEnfant(enfantSauvegarde);
            dossierEnfantRepository.save(donneesDossier);
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
                // Retirer l'enfant actuel de tous les groupes du séjour avant la suppression
                retirerEnfantDesGroupesDuSejour(sejourId, enfantActuel.getId());
                
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
        
        retirerEnfantDesGroupesDuSejour(sejourId, enfantId);
        
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
        
        // Retirer tous les enfants des groupes du séjour avant de supprimer les relations
        for (SejourEnfant sejourEnfant : sejour.getEnfants()) {
            Enfant e = sejourEnfant.getEnfant();
            if (e != null) {
                retirerEnfantDesGroupesDuSejour(sejourId, e.getId());
            }
        }
        
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
    public DossierEnfantDto getDossierEnfant(int sejourId, int enfantId, String utilisateurTokenId) {
        SejourEnfantId sejourEnfantId = new SejourEnfantId(sejourId, enfantId);
        SejourEnfant sejourEnfant = sejourEnfantRepository.findById(sejourEnfantId)
                .orElseThrow(() -> new ResourceNotFoundException("L'enfant n'est pas inscrit à ce séjour"));

        Sejour sejour = sejourEnfant.getSejour();
        boolean estDirecteur = sejour.getDirecteur() != null && sejour.getDirecteur().getTokenId().equals(utilisateurTokenId);
        boolean estDansEquipe = sejour.getEquipeRoles() != null && sejour.getEquipeRoles().stream()
                .anyMatch(se -> se.getUtilisateur() != null && se.getUtilisateur().getTokenId().equals(utilisateurTokenId));
        if (!estDirecteur && !estDansEquipe) {
            throw new AccessDeniedException("Vous ne participez pas à ce séjour");
        }

        DossierEnfant dossier = dossierEnfantRepository.findByEnfantId(enfantId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier non trouvé pour cet enfant"));

        return mapToDossierEnfantDto(dossier);
    }

    @Override
    @Transactional
    public DossierEnfantDto modifierDossierEnfant(int sejourId, int enfantId, UpdateDossierEnfantRequest request, String utilisateurTokenId) {
        SejourEnfantId sejourEnfantId = new SejourEnfantId(sejourId, enfantId);
        SejourEnfant sejourEnfant = sejourEnfantRepository.findById(sejourEnfantId)
                .orElseThrow(() -> new ResourceNotFoundException("L'enfant n'est pas inscrit à ce séjour"));

        Sejour sejour = sejourEnfant.getSejour();
        boolean estDirecteur = sejour.getDirecteur() != null && sejour.getDirecteur().getTokenId().equals(utilisateurTokenId);
        boolean estDansEquipe = sejour.getEquipeRoles() != null && sejour.getEquipeRoles().stream()
                .anyMatch(se -> se.getUtilisateur() != null && se.getUtilisateur().getTokenId().equals(utilisateurTokenId));
        if (!estDirecteur && !estDansEquipe) {
            throw new AccessDeniedException("Vous ne participez pas à ce séjour");
        }

        DossierEnfant dossier = dossierEnfantRepository.findByEnfantId(enfantId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier non trouvé pour cet enfant"));

        dossier.setEmailParent1(emptyToNull(request.emailParent1()));
        dossier.setTelephoneParent1(emptyToNull(request.telephoneParent1()));
        dossier.setEmailParent2(emptyToNull(request.emailParent2()));
        dossier.setTelephoneParent2(emptyToNull(request.telephoneParent2()));
        dossier.setInformationsMedicales(emptyToNull(request.informationsMedicales()));
        dossier.setPai(emptyToNull(request.pai()));
        dossier.setInformationsAlimentaires(emptyToNull(request.informationsAlimentaires()));
        dossier.setTraitementMatin(emptyToNull(request.traitementMatin()));
        dossier.setTraitementMidi(emptyToNull(request.traitementMidi()));
        dossier.setTraitementSoir(emptyToNull(request.traitementSoir()));
        dossier.setTraitementSiBesoin(emptyToNull(request.traitementSiBesoin()));
        dossier.setAutresInformations(emptyToNull(request.autresInformations()));
        dossier.setAPrendreEnSortie(emptyToNull(request.aPrendreEnSortie()));

        DossierEnfant dossierModifie = dossierEnfantRepository.save(dossier);
        return mapToDossierEnfantDto(dossierModifie);
    }

    private static String emptyToNull(String value) {
        return value != null && value.isBlank() ? null : value;
    }

    /** Retire un enfant de tous les groupes du séjour (règle métier : enfant supprimé du séjour = retiré de tous les groupes). */
    private void retirerEnfantDesGroupesDuSejour(int sejourId, int enfantId) {
        List<Groupe> groupes = groupeRepository.findBySejourId(sejourId);
        for (Groupe groupe : groupes) {
            if (groupe.getEnfants() != null && groupe.getEnfants().removeIf(e -> e.getId() == enfantId)) {
                groupeRepository.save(groupe);
            }
        }
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
            
            ExcelImportSpec spec = ExcelImportSpec.getInstance();
            Map<String, Integer> columnMap = ExcelHelper.detectColumns(headerRow, spec.getColumnMappings());

            // Vérifier que toutes les colonnes requises sont présentes
            boolean colonnesManquantes = false;
            for (String requiredKey : spec.getRequiredColumnKeys()) {
                if (!columnMap.containsKey(requiredKey)) {
                    messagesErreur.add(spec.getErrorMessageForMissingColumn(requiredKey));
                    colonnesManquantes = true;
                }
            }

            if (colonnesManquantes) {
                messagesErreur.add(0, spec.getSummaryErrorMessage());
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

                    // Créer l'enfant directement (dans la même transaction)
                    Sejour sejour = sejourRepository.findById(sejourId)
                            .orElseThrow(() -> new ResourceNotFoundException("Séjour non trouvé avec l'ID: " + sejourId));

                    if (sejour.getEnfants() == null) {
                        sejour.setEnfants(new ArrayList<>());
                    }

                    Enfant enfantExistant = enfantRepository.findByNomAndPrenomAndGenreAndDateNaissance(
                            request.nom(), request.prenom(), request.genre(), request.dateNaissance()
                    ).orElse(null);

                    Enfant enfantSauvegarde;

                    if (enfantExistant != null) {
                        boolean dejaDansSejour = sejour.getEnfants().stream()
                                .anyMatch(se -> se.getEnfant().getId() == enfantExistant.getId());
                        if (dejaDansSejour) {
                            String nee = request.genre() == Genre.Féminin ? "née" : "né";
                            enfantsDejaExistants++;
                            messagesErreur.add("Ligne " + (i + 1) + ": " + request.prenom() + " " + request.nom()
                                    + " " + nee + " le " + ExcelHelper.formatDate(request.dateNaissance())
                                    + " existe déjà dans ce séjour");
                            continue;
                        }
                        enfantSauvegarde = enfantExistant;
                        // Mettre à jour le dossier existant ou en créer un nouveau si absent
                        DossierEnfant dossier = dossierEnfantRepository.findByEnfantId(enfantExistant.getId())
                                .orElseGet(() -> {
                                    DossierEnfant d = new DossierEnfant();
                                    d.setEnfant(enfantSauvegarde);
                                    return d;
                                });
                        populateDossierFromExcelRow(dossier, row, columnMap);
                        dossierEnfantRepository.save(Objects.requireNonNull(dossier, "Dossier enfant requis"));
                    } else {
                        Enfant nouvelEnfant = Enfant.builder()
                                .nom(request.nom())
                                .prenom(request.prenom())
                                .genre(request.genre())
                                .dateNaissance(request.dateNaissance())
                                .niveauScolaire(request.niveauScolaire())
                                .build();
                        @SuppressWarnings("null")
                        Enfant saved = enfantRepository.save(nouvelEnfant);
                        enfantSauvegarde = saved;
                        DossierEnfant dossier = new DossierEnfant();
                        dossier.setEnfant(enfantSauvegarde);
                        populateDossierFromExcelRow(dossier, row, columnMap);
                        dossierEnfantRepository.save(dossier);
                    }

                    SejourEnfant sejourEnfant = SejourEnfant.builder()
                            .sejour(sejour)
                            .enfant(enfantSauvegarde)
                            .build();
                    sejour.getEnfants().add(sejourEnfant);
                    sejourRepository.save(sejour);
                    enfantsCrees++;
                    
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

    private String getOptionalColumn(Row row, Map<String, Integer> columnMap, String columnKey) {
        Integer idx = columnMap.get(columnKey);
        if (idx == null) return null;
        String val = ExcelHelper.getCellValueAsString(row, idx);
        return (val != null && !val.trim().isEmpty()) ? val.trim() : null;
    }

    private void populateDossierFromExcelRow(DossierEnfant dossier, Row row, Map<String, Integer> columnMap) {
        dossier.setEmailParent1(getOptionalColumn(row, columnMap, "emailParent1"));
        dossier.setTelephoneParent1(ExcelHelper.normalizePhone(getOptionalColumn(row, columnMap, "telephoneParent1")));
        dossier.setEmailParent2(getOptionalColumn(row, columnMap, "emailParent2"));
        dossier.setTelephoneParent2(ExcelHelper.normalizePhone(getOptionalColumn(row, columnMap, "telephoneParent2")));
        dossier.setInformationsMedicales(getOptionalColumn(row, columnMap, "informationsMedicales"));
        dossier.setInformationsAlimentaires(getOptionalColumn(row, columnMap, "informationsAlimentaires"));
        dossier.setTraitementMatin(getOptionalColumn(row, columnMap, "traitementMatin"));
        dossier.setTraitementMidi(getOptionalColumn(row, columnMap, "traitementMidi"));
        dossier.setTraitementSoir(getOptionalColumn(row, columnMap, "traitementSoir"));
        dossier.setTraitementSiBesoin(getOptionalColumn(row, columnMap, "traitementSiBesoin"));
        dossier.setAutresInformations(getOptionalColumn(row, columnMap, "autresInformations"));
        dossier.setPai(getOptionalColumn(row, columnMap, "pai"));
        dossier.setAPrendreEnSortie(getOptionalColumn(row, columnMap, "aPrendreEnSortie"));
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

    private DossierEnfantDto mapToDossierEnfantDto(DossierEnfant dossier) {
        return new DossierEnfantDto(
            dossier.getId(),
            dossier.getEnfant().getId(),
            dossier.getEmailParent1(),
            dossier.getTelephoneParent1(),
            dossier.getEmailParent2(),
            dossier.getTelephoneParent2(),
            dossier.getInformationsMedicales(),
            dossier.getPai(),
            dossier.getInformationsAlimentaires(),
            dossier.getTraitementMatin(),
            dossier.getTraitementMidi(),
            dossier.getTraitementSoir(),
            dossier.getTraitementSiBesoin(),
            dossier.getAutresInformations(),
            dossier.getAPrendreEnSortie()
        );
    }
}
