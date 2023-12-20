package com.tarnof.enjoyrestapi.enums;

import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tarnof.enjoyrestapi.enums.Privilege.*;

@RequiredArgsConstructor
public enum Role {
    ADMIN(
            Set.of(GESTION_UTILISATEURS,
                    GESTION_SEJOURS,
                    GESTION_SANITAIRE,
                    GESTION_PLANNINGS)
    ),
    DIRECTION(
            Set.of(GESTION_SEJOURS,
                    GESTION_SANITAIRE,
                    GESTION_PLANNINGS)
    ),
    ANIM(
            Set.of(GESTION_PLANNINGS)
    ),
    ANIM_AS(
            Set.of(GESTION_SANITAIRE,
                    GESTION_PLANNINGS)
    );


    @Getter
    private final Set<Privilege> privileges;

    public List<SimpleGrantedAuthority> getAuthorities(){
        List<SimpleGrantedAuthority> authorities = getPrivileges()
                .stream()
                .map(privilege -> new SimpleGrantedAuthority(privilege.name()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_"+this.name()));
        return authorities;
    }
}

