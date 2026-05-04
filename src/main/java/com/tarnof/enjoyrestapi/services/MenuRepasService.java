package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.payload.request.SaveMenuRepasRequest;
import com.tarnof.enjoyrestapi.payload.response.MenuRepasDto;

import java.time.LocalDate;
import java.util.List;

public interface MenuRepasService {

    List<MenuRepasDto> listerParJour(int sejourId, LocalDate date);

    List<MenuRepasDto> listerParPeriode(int sejourId, LocalDate dateDebutInclusive, LocalDate dateFinInclusive);

    MenuRepasDto get(int sejourId, int menuId);

    MenuRepasDto creer(int sejourId, SaveMenuRepasRequest request);

    MenuRepasDto modifier(int sejourId, int menuId, SaveMenuRepasRequest request);

    void supprimer(int sejourId, int menuId);
}
