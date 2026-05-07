package com.tarnof.enjoyrestapi.utils;

import com.tarnof.enjoyrestapi.entities.Lieu;
import com.tarnof.enjoyrestapi.enums.UsageLieu;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class LieuUsageRules {

    private LieuUsageRules() {}

    /** Ordre d’affichage / sérialisation stable : même ordre que l’enum. */
    public static LinkedHashSet<UsageLieu> usagesEnOrdreDeclaration(Set<UsageLieu> effectifs) {
        return Arrays.stream(UsageLieu.values())
                .filter(effectifs::contains)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Lieux sans ligne dans {@code lieu_usage} (données antérieures) : traités comme lieu d’activité
     * uniquement pour la cohérence avec l’existant.
     */
    public static Set<UsageLieu> usagesEffectifs(Lieu lieu) {
        if (lieu.getUsages() == null || lieu.getUsages().isEmpty()) {
            return Set.of(UsageLieu.ACTIVITE);
        }
        return Collections.unmodifiableSet(lieu.getUsages());
    }

    public static boolean acceptePourActivite(Lieu lieu) {
        return usagesEffectifs(lieu).contains(UsageLieu.ACTIVITE);
    }

    public static boolean acceptePourPlanningSurveillanceOuRassemblement(Lieu lieu) {
        Set<UsageLieu> u = usagesEffectifs(lieu);
        return u.contains(UsageLieu.SURVEILLANCE) || u.contains(UsageLieu.RASSEMBLEMENT);
    }
}
