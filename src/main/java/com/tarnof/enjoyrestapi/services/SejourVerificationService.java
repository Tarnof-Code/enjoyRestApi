package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.repositories.SejourRepository;
import org.springframework.stereotype.Service;

@Service
public class SejourVerificationService {

    private final SejourRepository sejourRepository;

    public SejourVerificationService(SejourRepository sejourRepository) {
        this.sejourRepository = sejourRepository;
    }

    public Sejour verifierSejourExiste(int sejourId) {
        return sejourRepository
                .findById(sejourId)
                .orElseThrow(() -> new ResourceNotFoundException("Séjour non trouvé avec l'ID: " + sejourId));
    }
}
