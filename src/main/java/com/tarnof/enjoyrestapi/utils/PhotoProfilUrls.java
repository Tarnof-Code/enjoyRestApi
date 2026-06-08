package com.tarnof.enjoyrestapi.utils;

public final class PhotoProfilUrls {

    private PhotoProfilUrls() {}

    public static String urlPhotoProfilUtilisateur(String tokenId) {
        return "/api/v1/utilisateurs/" + tokenId + "/photo-profil";
    }

    public static String urlPhotoProfilUtilisateur(String tokenId, String photoProfilCle) {
        if (photoProfilCle == null || photoProfilCle.isBlank()) {
            return null;
        }
        return urlPhotoProfilUtilisateur(tokenId);
    }
}
