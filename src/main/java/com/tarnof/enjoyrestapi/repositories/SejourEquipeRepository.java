package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.SejourEquipe;
import com.tarnof.enjoyrestapi.entities.SejourEquipeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SejourEquipeRepository extends JpaRepository<SejourEquipe, SejourEquipeId> {
}

