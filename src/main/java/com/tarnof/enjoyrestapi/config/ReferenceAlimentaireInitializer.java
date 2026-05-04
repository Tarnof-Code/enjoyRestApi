package com.tarnof.enjoyrestapi.config;

import com.tarnof.enjoyrestapi.entities.ReferenceAlimentaire;
import com.tarnof.enjoyrestapi.enums.TypeReferenceAlimentaire;
import com.tarnof.enjoyrestapi.repositories.ReferenceAlimentaireRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Insertion idempotente des allergènes (référentiel UE) et des régimes / préférences les plus courants.
 */
@Component
@Order
public class ReferenceAlimentaireInitializer implements ApplicationRunner {

    private final ReferenceAlimentaireRepository referenceAlimentaireRepository;

    public ReferenceAlimentaireInitializer(ReferenceAlimentaireRepository referenceAlimentaireRepository) {
        this.referenceAlimentaireRepository = referenceAlimentaireRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        insererSiAbsent(TypeReferenceAlimentaire.ALLERGENE, "Gluten", 10);
        insererSiAbsent(TypeReferenceAlimentaire.ALLERGENE, "Crustacés", 20);
        insererSiAbsent(TypeReferenceAlimentaire.ALLERGENE, "Œufs", 30);
        insererSiAbsent(TypeReferenceAlimentaire.ALLERGENE, "Poisson", 40);
        insererSiAbsent(TypeReferenceAlimentaire.ALLERGENE, "Arachides", 50);
        insererSiAbsent(TypeReferenceAlimentaire.ALLERGENE, "Soja", 60);
        insererSiAbsent(TypeReferenceAlimentaire.ALLERGENE, "Lait", 70);
        insererSiAbsent(TypeReferenceAlimentaire.ALLERGENE, "Fruits à coque", 80);
        insererSiAbsent(TypeReferenceAlimentaire.ALLERGENE, "Céleri", 90);
        insererSiAbsent(TypeReferenceAlimentaire.ALLERGENE, "Moutarde", 100);
        insererSiAbsent(TypeReferenceAlimentaire.ALLERGENE, "Graines de sésame", 110);

        insererSiAbsent(TypeReferenceAlimentaire.REGIME_PREFERENCE, "Sans porc", 10);
        insererSiAbsent(TypeReferenceAlimentaire.REGIME_PREFERENCE, "Sans viande", 20);
    }

    private void insererSiAbsent(TypeReferenceAlimentaire type, String libelle, int ordre) {
        if (!referenceAlimentaireRepository.existsByTypeAndLibelleIgnoreCase(type, libelle)) {
            ReferenceAlimentaire r = new ReferenceAlimentaire();
            r.setType(type);
            r.setLibelle(libelle);
            r.setOrdre(ordre);
            r.setActif(true);
            referenceAlimentaireRepository.save(r);
        }
    }
}
