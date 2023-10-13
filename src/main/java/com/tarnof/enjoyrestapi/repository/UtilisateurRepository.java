package com.tarnof.enjoyrestapi.repository;

import com.tarnof.enjoyrestapi.entity.Utilisateur;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UtilisateurRepository extends CrudRepository<Utilisateur, Integer> {

    Utilisateur findByEmail(String email);
}
