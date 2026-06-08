package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.RefreshToken;
import com.tarnof.enjoyrestapi.enums.Genre;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.exceptions.EmailDejaUtiliseException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.exceptions.UtilisateurException;
import com.tarnof.enjoyrestapi.payload.request.UpdateUserRequest;
import com.tarnof.enjoyrestapi.payload.response.PhotoProfilContenu;
import com.tarnof.enjoyrestapi.payload.response.ProfilDto;
import com.tarnof.enjoyrestapi.repositories.RefreshTokenRepository;
import com.tarnof.enjoyrestapi.repositories.SejourRepository;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.services.UtilisateurService;
import com.tarnof.enjoyrestapi.services.storage.ObjectStorageService;
import com.tarnof.enjoyrestapi.utils.ImageUploadValidator;
import com.tarnof.enjoyrestapi.utils.PhotoProfilUrls;

import jakarta.transaction.Transactional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UtilisateurServiceImpl implements UtilisateurService {
    private final UtilisateurRepository utilisateurRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SejourRepository sejourRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ObjectStorageService objectStorageService;

    public UtilisateurServiceImpl(UtilisateurRepository utilisateurRepository, RefreshTokenRepository refreshTokenRepository,
                                  SejourRepository sejourRepository, BCryptPasswordEncoder bCryptPasswordEncoder,
                                  ObjectStorageService objectStorageService) {
        this.utilisateurRepository = utilisateurRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.sejourRepository = sejourRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.objectStorageService = objectStorageService;
    }

    @Override
    public Utilisateur creerUtilisateur(Utilisateur utilisateur) {
        if(utilisateurRepository.existsByEmail(utilisateur.getEmail()) || utilisateurRepository.existsByTelephone(utilisateur.getTelephone())){
            throw new EmailDejaUtiliseException("L'email ou le numéro de téléphone est déjà utilisé par un autre compte.");
        }
        utilisateur.setMotDePasse(bCryptPasswordEncoder.encode(utilisateur.getMotDePasse()));
        return utilisateurRepository.save(utilisateur);
    }

    @Override
    public List<ProfilDto> getAllUtilisateursDTO() {
        List<Utilisateur> listeUtilisateurs = utilisateurRepository.findAll();
        return listeUtilisateurs.stream()
                .map(this::mapUtilisateurToProfilDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProfilDto> getUtilisateursByRole(Role role) {
        List<Utilisateur> listeUtilisateurs = utilisateurRepository.findByRole(role);
        return listeUtilisateurs.stream()
                .map(this::mapUtilisateurToProfilDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Utilisateur> profilUtilisateur(String tokenId) {
        return utilisateurRepository.findByTokenId(tokenId);
    }

    @Override
    public Optional<Utilisateur> getUtilisateurByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    @Override
    public ProfilDto mapUtilisateurToProfilDTO(Utilisateur utilisateur) {
        return new ProfilDto(
            utilisateur.getTokenId(),
            utilisateur.getRole(),
            null,
            utilisateur.getNom(),
            utilisateur.getPrenom(),
            utilisateur.getGenre(),
            utilisateur.getEmail(),
            utilisateur.getTelephone(),
            utilisateur.getDateNaissance(),
            utilisateur.getDateExpirationCompte(),
            PhotoProfilUrls.urlPhotoProfilUtilisateur(utilisateur.getTokenId(), utilisateur.getPhotoProfilCle())
        );
    }

    @Override
    public Utilisateur modifUserByUser(Utilisateur utilisateur, UpdateUserRequest request) {
        return modifUser(utilisateur, request, false, false);
    }

    @Override
    public Utilisateur modifUserByDirector(Utilisateur utilisateur, UpdateUserRequest request) {
        return modifUser(utilisateur, request, false, true);
    }

    @Override
    public Utilisateur modifUserByAdmin(Utilisateur utilisateur, UpdateUserRequest request) {
        return modifUser(utilisateur, request, true, true);
    }

    private Utilisateur modifUser(Utilisateur utilisateur, UpdateUserRequest request, boolean isAdmin, boolean allowEmailChange) {
        if (!allowEmailChange && !utilisateur.getEmail().equals(request.email())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier l'adresse email");
        }
        // Vérifier si l'email est déjà utilisé par un autre utilisateur
        if (!utilisateur.getEmail().equals(request.email()) && utilisateurRepository.existsByEmail(request.email())) {
            throw new EmailDejaUtiliseException("L'email est déjà utilisé par un autre compte.");
        }
        Utilisateur.UtilisateurBuilder builder = utilisateur.toBuilder()
                .prenom(request.prenom())
                .nom(request.nom())
                .genre(Genre.parseGenre(request.genre()))
                .email(request.email())
                .telephone(request.telephone())
                .dateNaissance(request.dateNaissance());

        if (isAdmin) {
              // Si le rôle change de DIRECTION vers autre chose, retirer le directeur des séjours
            if (request.role() != null && utilisateur.getRole() == Role.DIRECTION && request.role() != Role.DIRECTION) {
                List<Sejour> sejoursDiriges = sejourRepository.findByDirecteur(utilisateur);
                if (!sejoursDiriges.isEmpty()) {
                    sejoursDiriges.forEach(sejour -> sejour.setDirecteur(null));
                    sejourRepository.saveAll(sejoursDiriges);
                }
            }
            builder.role(request.role());
            if (utilisateur.getRefreshToken() != null){
                RefreshToken refreshToken = utilisateur.getRefreshToken();
                Instant nouvelleDateExpiration = request.dateExpirationCompte();
                refreshToken.setExpiryDate(nouvelleDateExpiration);
                refreshTokenRepository.save(refreshToken);
            }
        }

        Utilisateur utilisateurModifie = builder.build();
        Objects.requireNonNull(utilisateurModifie, "L'utilisateur n'a pas pu être modifié");
        utilisateurRepository.save(utilisateurModifie);
        return utilisateurModifie;
    }



    @Override
    @Transactional
    public void supprimerUtilisateur(String tokenId) {
        Optional<Utilisateur> utilisateur = utilisateurRepository.findByTokenId(tokenId);
        if (utilisateur.isPresent()) {
            supprimerPhotoProfilStockage(utilisateur.get());
            utilisateurRepository.deleteByTokenId(tokenId);
        } else {
            throw new UtilisateurException("L'utilisateur n'existe pas");
        }
    }

    @Override
    @Transactional
    public ProfilDto mettreAJourPhotoProfil(String tokenId, MultipartFile file, String appelantTokenId, boolean appelantEstAdmin) {
        verifierDroitModificationPhotoProfil(tokenId, appelantTokenId, appelantEstAdmin);
        ImageUploadValidator.validerPhotoProfil(file);

        Utilisateur utilisateur = utilisateurRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec le token ID: " + tokenId));

        String mimeType = file.getContentType();
        String extension = ImageUploadValidator.extensionDepuisMimeType(mimeType);
        String nouvelleCle = objectStorageService.buildPhotoProfilUtilisateurKey(tokenId, extension);

        supprimerPhotoProfilStockage(utilisateur);

        try {
            objectStorageService.upload(nouvelleCle, file.getInputStream(), file.getSize(), mimeType);
        } catch (Exception e) {
            throw new RuntimeException("Impossible d'enregistrer la photo de profil", e);
        }

        utilisateur.setPhotoProfilCle(nouvelleCle);
        utilisateur.setPhotoProfilMimeType(mimeType);
        Utilisateur saved = utilisateurRepository.save(utilisateur);
        return mapUtilisateurToProfilDTO(saved);
    }

    @Override
    @Transactional
    public void supprimerPhotoProfil(String tokenId, String appelantTokenId, boolean appelantEstAdmin) {
        verifierDroitModificationPhotoProfil(tokenId, appelantTokenId, appelantEstAdmin);

        Utilisateur utilisateur = utilisateurRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec le token ID: " + tokenId));

        supprimerPhotoProfilStockage(utilisateur);
        utilisateur.setPhotoProfilCle(null);
        utilisateur.setPhotoProfilMimeType(null);
        utilisateurRepository.save(utilisateur);
    }

    @Override
    public PhotoProfilContenu chargerPhotoProfil(String tokenId) {
        Utilisateur utilisateur = utilisateurRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec le token ID: " + tokenId));

        String cle = utilisateur.getPhotoProfilCle();
        if (cle == null || cle.isBlank()) {
            throw new ResourceNotFoundException("Aucune photo de profil pour cet utilisateur");
        }

        ObjectStorageService.StoredObject storedObject = objectStorageService.download(cle)
                .orElseThrow(() -> new ResourceNotFoundException("Photo de profil introuvable"));

        String mimeType = Objects.requireNonNull(Objects.requireNonNullElse(
                utilisateur.getPhotoProfilMimeType(),
                storedObject.contentType()));

        return new PhotoProfilContenu(storedObject.content(), storedObject.size(), mimeType);
    }

    private void verifierDroitModificationPhotoProfil(String tokenId, String appelantTokenId, boolean appelantEstAdmin) {
        if (appelantEstAdmin || tokenId.equals(appelantTokenId)) {
            return;
        }
        throw new AccessDeniedException("Vous ne pouvez modifier que votre propre photo de profil");
    }

    private void supprimerPhotoProfilStockage(Utilisateur utilisateur) {
        String cle = utilisateur.getPhotoProfilCle();
        if (cle != null && !cle.isBlank()) {
            objectStorageService.delete(cle);
        }
    }

    @Override
    public Utilisateur changerMotDePasseParAdmin(String tokenId, String nouveauMotDePasse) {
        Utilisateur utilisateur = utilisateurRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new UtilisateurException("Utilisateur non trouvé"));
        utilisateur.setMotDePasse(bCryptPasswordEncoder.encode(nouveauMotDePasse));
        return utilisateurRepository.save(utilisateur);
    }

    @Override
    public Utilisateur changerMotDePasseParUtilisateur(String tokenId, String ancienMotDePasse, String nouveauMotDePasse) {
        Utilisateur utilisateur = utilisateurRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new UtilisateurException("Utilisateur non trouvé"));
        if (!bCryptPasswordEncoder.matches(ancienMotDePasse, utilisateur.getMotDePasse())) {
            throw new UtilisateurException("L'ancien mot de passe est incorrect");
        }
        utilisateur.setMotDePasse(bCryptPasswordEncoder.encode(nouveauMotDePasse));
        return utilisateurRepository.save(utilisateur);
    }

}
