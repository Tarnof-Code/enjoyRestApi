package com.tarnof.enjoyrestapi.enums;

public enum Genre {
    Masculin,
    Féminin;

    /**
     * Parse une chaîne en Genre. Accepte "Masculin", "Féminin", "Garçon", "Fille", "Homme", "Femme", "M", "F".
     */
    public static Genre parseGenre(String genreStr) {
        if (genreStr == null || genreStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Le genre ne peut pas être vide");
        }
        String normalized = genreStr.trim();
        if (normalized.equalsIgnoreCase("garçon") || normalized.equalsIgnoreCase("garcon")
                || normalized.equalsIgnoreCase("homme") || normalized.equalsIgnoreCase("m")) {
            return Masculin;
        }
        if (normalized.equalsIgnoreCase("fille") || normalized.equalsIgnoreCase("femme")
                || normalized.equalsIgnoreCase("f")) {
            return Féminin;
        }
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Genre invalide: " + genreStr + ". Valeurs acceptées: Masculin, Féminin, Garçon, Fille");
        }
    }
}
