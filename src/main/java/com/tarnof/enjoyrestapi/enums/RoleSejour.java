package com.tarnof.enjoyrestapi.enums;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum RoleSejour {
    ANIM(
            Set.of(Privilege.ACCES_SEJOUR)
    ),
    AS(
            Set.of(Privilege.ACCES_SEJOUR, Privilege.GESTION_SANITAIRE)
    ),
    ADJOINT(
            Set.of(Privilege.GESTION_SEJOURS, Privilege.GESTION_SANITAIRE, Privilege.ACCES_SEJOUR)
    ),
    SB(
            Set.of(Privilege.ACCES_SEJOUR)
    ),
    AUTRE(
            Set.of(Privilege.ACCES_SEJOUR)
    );

    private final Set<Privilege> privileges;

    RoleSejour(Set<Privilege> privileges) {
        this.privileges = privileges;
    }

    public Set<Privilege> getPrivileges() {
        return privileges;
    }

    public List<SimpleGrantedAuthority> getAuthorities(){
        List<SimpleGrantedAuthority> authorities = getPrivileges()
                .stream()
                .map(privilege -> new SimpleGrantedAuthority(privilege.name()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_SEJOUR_"+this.name()));
        return authorities;
    }
}

