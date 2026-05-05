package com.tarnof.enjoyrestapi.enums;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum Role {
    ADMIN(
            Set.of(Privilege.GESTION_UTILISATEURS,
                    Privilege.GESTION_SEJOURS,
                    Privilege.GESTION_SANITAIRE,
                    Privilege.ACCES_SEJOUR)
    ),
    DIRECTION(
            Set.of(Privilege.GESTION_SEJOURS,
                    Privilege.GESTION_SANITAIRE,
                    Privilege.ACCES_SEJOUR)
    ),
    BASIC_USER(
            Set.of(Privilege.ACCES_SEJOUR)
    );

    private final Set<Privilege> privileges;

    Role(Set<Privilege> privileges) {
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
        authorities.add(new SimpleGrantedAuthority("ROLE_"+this.name()));
        return authorities;
    }
}
