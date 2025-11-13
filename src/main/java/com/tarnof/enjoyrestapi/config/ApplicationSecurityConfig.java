package com.tarnof.enjoyrestapi.config;

import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationSecurityConfig {
    private final UtilisateurRepository utilisateurRepository;

    private boolean isEmail(String identifier) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z\\p{L}]{2,7}$";
        return identifier.matches(emailRegex);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return identifier -> {
            if(isEmail(identifier)){
                return utilisateurRepository.findByEmail(identifier)
                        .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email"));
            } else {
                return utilisateurRepository.findByTokenId(identifier)
                        .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec le tokenId"));
            }
        };
    }
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
