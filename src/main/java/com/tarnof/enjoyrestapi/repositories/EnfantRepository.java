package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.Enfant;
import com.tarnof.enjoyrestapi.enums.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface EnfantRepository extends JpaRepository<Enfant, Integer> {
    /**
     * Recherche un enfant par nom, prénom, genre et date de naissance
     * @param nom Le nom de famille
     * @param prenom Le prénom
     * @param genre Le genre
     * @param dateNaissance La date de naissance
     * @return L'enfant trouvé ou Optional.empty() si aucun enfant ne correspond
     */
    Optional<Enfant> findByNomAndPrenomAndGenreAndDateNaissance(String nom, String prenom, Genre genre, Date dateNaissance);
}
