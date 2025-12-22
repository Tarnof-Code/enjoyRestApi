package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
    Optional<Utilisateur> findByEmail(String email);
    Optional<Utilisateur> findByTokenId(String tokenId);
    List<Utilisateur> findByRole(Role role);
    void deleteByTokenId(String tokenId);
    boolean existsByEmail(String email);
    boolean existsByTelephone(String telephone);
}
