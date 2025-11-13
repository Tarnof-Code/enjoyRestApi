package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.RefreshToken;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findByToken (String token);
    Optional<RefreshToken> findByUtilisateur (Utilisateur utilisateur);
}
