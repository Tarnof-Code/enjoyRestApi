package com.tarnof.enjoyrestapi.config;

import com.tarnof.enjoyrestapi.repositories.SejourRepository;
import com.tarnof.enjoyrestapi.services.TypeActiviteService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pour chaque séjour existant : assure la présence des types d'activité par défaut (et {@code predefini} pour les
 * libellés connus déjà en base).
 */
@Component
@Order
public class TypeActiviteInitializer implements ApplicationRunner {

    private final SejourRepository sejourRepository;
    private final TypeActiviteService typeActiviteService;

    public TypeActiviteInitializer(SejourRepository sejourRepository, TypeActiviteService typeActiviteService) {
        this.sejourRepository = sejourRepository;
        this.typeActiviteService = typeActiviteService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        sejourRepository
                .findAll()
                .forEach(s -> typeActiviteService.assurerTypesParDefautPourSejour(s.getId()));
    }
}
