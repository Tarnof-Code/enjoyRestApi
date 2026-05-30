package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.ActivitePrestataire;
import com.tarnof.enjoyrestapi.entities.Groupe;
import com.tarnof.enjoyrestapi.entities.Moment;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.payload.request.SaveActivitePrestataireRequest;
import com.tarnof.enjoyrestapi.payload.response.ActivitePrestataireDto;
import com.tarnof.enjoyrestapi.payload.response.NonParticipationPrestataireDto;
import com.tarnof.enjoyrestapi.repositories.ActivitePrestataireRepository;
import com.tarnof.enjoyrestapi.repositories.GroupeRepository;
import com.tarnof.enjoyrestapi.repositories.MomentRepository;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivitePrestataireServiceImpl — non-participations")
@SuppressWarnings("null")
class ActivitePrestataireServiceImplTest {

    @Mock
    private ActivitePrestataireRepository activitePrestataireRepository;
    @Mock
    private SejourVerificationService sejourVerificationService;
    @Mock
    private MomentRepository momentRepository;
    @Mock
    private GroupeRepository groupeRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private ActivitePrestataireServiceImpl service;

    @Test
    @DisplayName("création avec non-participation pour un référent concerné")
    void creer_persisteNonParticipation() {
        Sejour sejour = new Sejour();
        sejour.setId(10);
        sejour.setDateDebut(java.sql.Date.valueOf("2026-07-01"));
        sejour.setDateFin(java.sql.Date.valueOf("2026-07-31"));

        Moment matin = moment(1, "Matin", sejour, 0);
        Utilisateur referent = Utilisateur.builder().id(5).tokenId("tok-ref").build();
        Groupe groupe = groupeAvecReferent(3, sejour, referent);

        when(sejourVerificationService.verifierSejourExiste(10)).thenReturn(sejour);
        when(momentRepository.countBySejourId(10)).thenReturn(1L);
        when(momentRepository.findByIdAndSejourId(1, 10)).thenReturn(Optional.of(matin));
        when(groupeRepository.findById(3)).thenReturn(Optional.of(groupe));
        when(utilisateurRepository.findByTokenId("tok-ref")).thenReturn(Optional.of(referent));
        when(activitePrestataireRepository.save(any(ActivitePrestataire.class)))
                .thenAnswer(inv -> {
                    ActivitePrestataire a = inv.getArgument(0);
                    if (a.getId() == null) {
                        a.setId(99);
                    }
                    return a;
                });

        SaveActivitePrestataireRequest request = new SaveActivitePrestataireRequest(
                "Sortie",
                LocalDate.of(2026, 7, 15),
                List.of(1),
                null,
                null,
                null,
                null,
                List.of(3),
                List.of(new NonParticipationPrestataireDto("tok-ref", 1)));

        ActivitePrestataireDto dto = service.creerActivitePrestataire(10, request);

        assertThat(dto.nonParticipations()).containsExactly(new NonParticipationPrestataireDto("tok-ref", 1));

        ArgumentCaptor<ActivitePrestataire> captor = ArgumentCaptor.forClass(ActivitePrestataire.class);
        verify(activitePrestataireRepository).save(captor.capture());
        assertThat(captor.getValue().getNonParticipations()).hasSize(1);
    }

    @Test
    @DisplayName("non-participation refusée si animateur non référent des groupes")
    void creer_refuseNonParticipationAnimateurNonConcerne() {
        Sejour sejour = new Sejour();
        sejour.setId(10);
        sejour.setDateDebut(java.sql.Date.valueOf("2026-07-01"));
        sejour.setDateFin(java.sql.Date.valueOf("2026-07-31"));

        Moment matin = moment(1, "Matin", sejour, 0);
        Groupe groupe = groupeAvecReferent(3, sejour, Utilisateur.builder().tokenId("tok-autre").build());

        when(sejourVerificationService.verifierSejourExiste(10)).thenReturn(sejour);
        when(momentRepository.countBySejourId(10)).thenReturn(1L);
        when(momentRepository.findByIdAndSejourId(1, 10)).thenReturn(Optional.of(matin));
        when(groupeRepository.findById(3)).thenReturn(Optional.of(groupe));

        SaveActivitePrestataireRequest request = new SaveActivitePrestataireRequest(
                "Sortie",
                LocalDate.of(2026, 7, 15),
                List.of(1),
                null,
                null,
                null,
                null,
                List.of(3),
                List.of(new NonParticipationPrestataireDto("tok-inconnu", 1)));

        assertThatThrownBy(() -> service.creerActivitePrestataire(10, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("n'est pas référent");
    }

    @Test
    @DisplayName("modification sans nonParticipations élaguer les entrées invalides")
    void modifier_elagueNonParticipationsOrphelines() {
        Sejour sejour = new Sejour();
        sejour.setId(10);
        sejour.setDateDebut(java.sql.Date.valueOf("2026-07-01"));
        sejour.setDateFin(java.sql.Date.valueOf("2026-07-31"));

        Moment apresMidi = moment(2, "Après-midi", sejour, 1);
        Utilisateur referent = Utilisateur.builder().id(5).tokenId("tok-ref").build();
        Groupe groupe = groupeAvecReferent(3, sejour, referent);

        ActivitePrestataire existante = new ActivitePrestataire();
        existante.setId(7);
        existante.setSejour(sejour);
        existante.setNom("Sortie");
        existante.setDate(LocalDate.of(2026, 7, 15));
        existante.getMoments().add(moment(1, "Matin", sejour, 0));
        existante.getGroupes().add(groupe);

        var np = new com.tarnof.enjoyrestapi.entities.ActivitePrestataireNonParticipation();
        np.setActivitePrestataire(existante);
        np.setUtilisateur(referent);
        np.setMoment(existante.getMoments().getFirst());
        existante.getNonParticipations().add(np);

        when(activitePrestataireRepository.findByIdAndSejour_Id(7, 10)).thenReturn(Optional.of(existante));
        when(momentRepository.countBySejourId(10)).thenReturn(2L);
        when(momentRepository.findByIdAndSejourId(2, 10)).thenReturn(Optional.of(apresMidi));
        when(groupeRepository.findById(3)).thenReturn(Optional.of(groupe));
        when(activitePrestataireRepository.save(any(ActivitePrestataire.class)))
                .thenAnswer(inv -> {
                    ActivitePrestataire a = inv.getArgument(0);
                    if (a.getId() == null) {
                        a.setId(99);
                    }
                    return a;
                });

        SaveActivitePrestataireRequest request = new SaveActivitePrestataireRequest(
                "Sortie",
                LocalDate.of(2026, 7, 15),
                List.of(2),
                null,
                null,
                null,
                null,
                List.of(3),
                null);

        ActivitePrestataireDto dto = service.modifierActivitePrestataire(10, 7, request);

        assertThat(dto.nonParticipations()).isEmpty();
        assertThat(existante.getNonParticipations()).isEmpty();
    }

    @Test
    @DisplayName("second PUT avec la même non-participation réutilise la ligne existante")
    void modifier_rePutMemeNonParticipation_reutiliseEntiteExistante() {
        Sejour sejour = new Sejour();
        sejour.setId(10);
        sejour.setDateDebut(java.sql.Date.valueOf("2026-07-01"));
        sejour.setDateFin(java.sql.Date.valueOf("2026-07-31"));

        Moment matin = moment(1, "Matin", sejour, 0);
        Utilisateur referent = Utilisateur.builder().id(5).tokenId("tok-ref").build();
        Groupe groupe = groupeAvecReferent(3, sejour, referent);

        ActivitePrestataire existante = new ActivitePrestataire();
        existante.setId(7);
        existante.setSejour(sejour);
        existante.setNom("Sortie");
        existante.setDate(LocalDate.of(2026, 7, 15));
        existante.getMoments().add(matin);
        existante.getGroupes().add(groupe);

        var npExistante = new com.tarnof.enjoyrestapi.entities.ActivitePrestataireNonParticipation();
        npExistante.setId(42);
        npExistante.setActivitePrestataire(existante);
        npExistante.setUtilisateur(referent);
        npExistante.setMoment(matin);
        existante.getNonParticipations().add(npExistante);

        when(activitePrestataireRepository.findByIdAndSejour_Id(7, 10)).thenReturn(Optional.of(existante));
        when(momentRepository.countBySejourId(10)).thenReturn(1L);
        when(momentRepository.findByIdAndSejourId(1, 10)).thenReturn(Optional.of(matin));
        when(groupeRepository.findById(3)).thenReturn(Optional.of(groupe));
        when(activitePrestataireRepository.save(any(ActivitePrestataire.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SaveActivitePrestataireRequest request = new SaveActivitePrestataireRequest(
                "Sortie",
                LocalDate.of(2026, 7, 15),
                List.of(1),
                null,
                null,
                null,
                null,
                List.of(3),
                List.of(new NonParticipationPrestataireDto("tok-ref", 1)));

        service.modifierActivitePrestataire(10, 7, request);

        assertThat(existante.getNonParticipations()).hasSize(1);
        assertThat(existante.getNonParticipations().getFirst()).isSameAs(npExistante);
        assertThat(existante.getNonParticipations().getFirst().getId()).isEqualTo(42);
    }

    @Test
    @DisplayName("création refusée si une sortie existe déjà pour le même groupe, date et moment")
    void creer_refuseDoublonMemeGroupeDateMoment() {
        Sejour sejour = new Sejour();
        sejour.setId(10);
        sejour.setDateDebut(java.sql.Date.valueOf("2026-07-01"));
        sejour.setDateFin(java.sql.Date.valueOf("2026-07-31"));

        Moment matin = moment(1, "Matin", sejour, 0);
        Groupe groupe = groupeAvecReferent(3, sejour, Utilisateur.builder().tokenId("tok-ref").build());
        groupe.setNom("Les ados");

        when(sejourVerificationService.verifierSejourExiste(10)).thenReturn(sejour);
        when(momentRepository.countBySejourId(10)).thenReturn(1L);
        when(momentRepository.findByIdAndSejourId(1, 10)).thenReturn(Optional.of(matin));
        when(groupeRepository.findById(3)).thenReturn(Optional.of(groupe));
        when(activitePrestataireRepository.countAutreSortieMemeDateMomentGroupe(
                        eq(10), eq(LocalDate.of(2026, 7, 15)), eq(1), eq(3), isNull()))
                .thenReturn(1L);

        SaveActivitePrestataireRequest request = new SaveActivitePrestataireRequest(
                "Sortie",
                LocalDate.of(2026, 7, 15),
                List.of(1),
                null,
                null,
                null,
                null,
                List.of(3),
                null);

        assertThatThrownBy(() -> service.creerActivitePrestataire(10, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Les ados")
                .hasMessageContaining("Matin");
    }

    @Test
    @DisplayName("modification de la même sortie sans conflit avec elle-même")
    void modifier_exclutLaSortieCouranteDuControleDoublon() {
        Sejour sejour = new Sejour();
        sejour.setId(10);
        sejour.setDateDebut(java.sql.Date.valueOf("2026-07-01"));
        sejour.setDateFin(java.sql.Date.valueOf("2026-07-31"));

        Moment matin = moment(1, "Matin", sejour, 0);
        Groupe groupe = groupeAvecReferent(3, sejour, Utilisateur.builder().tokenId("tok-ref").build());
        groupe.setNom("Les ados");

        ActivitePrestataire existante = new ActivitePrestataire();
        existante.setId(7);
        existante.setSejour(sejour);
        existante.setNom("Sortie");
        existante.setDate(LocalDate.of(2026, 7, 15));
        existante.getMoments().add(matin);
        existante.getGroupes().add(groupe);

        when(activitePrestataireRepository.findByIdAndSejour_Id(7, 10)).thenReturn(Optional.of(existante));
        when(momentRepository.countBySejourId(10)).thenReturn(1L);
        when(momentRepository.findByIdAndSejourId(1, 10)).thenReturn(Optional.of(matin));
        when(groupeRepository.findById(3)).thenReturn(Optional.of(groupe));
        when(activitePrestataireRepository.countAutreSortieMemeDateMomentGroupe(
                        eq(10), eq(LocalDate.of(2026, 7, 15)), eq(1), eq(3), eq(7)))
                .thenReturn(0L);
        when(activitePrestataireRepository.save(any(ActivitePrestataire.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SaveActivitePrestataireRequest request = new SaveActivitePrestataireRequest(
                "Sortie modifiée",
                LocalDate.of(2026, 7, 15),
                List.of(1),
                null,
                null,
                null,
                null,
                List.of(3),
                null);

        ActivitePrestataireDto dto = service.modifierActivitePrestataire(10, 7, request);

        assertThat(dto.nom()).isEqualTo("Sortie modifiée");
        verify(activitePrestataireRepository)
                .countAutreSortieMemeDateMomentGroupe(
                        eq(10), eq(LocalDate.of(2026, 7, 15)), eq(1), eq(3), eq(7));
    }

    private static Moment moment(int id, String nom, Sejour sejour, int ordre) {
        Moment m = new Moment();
        m.setId(id);
        m.setNom(nom);
        m.setSejour(sejour);
        m.setOrdre(ordre);
        return m;
    }

    private static Groupe groupeAvecReferent(int id, Sejour sejour, Utilisateur referent) {
        Groupe g = new Groupe();
        g.setId(id);
        g.setSejour(sejour);
        g.setReferents(new ArrayList<>(List.of(referent)));
        return g;
    }
}
