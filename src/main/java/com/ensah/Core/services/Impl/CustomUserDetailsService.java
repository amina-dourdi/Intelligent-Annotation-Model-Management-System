package com.ensah.Core.services.Impl;



import com.ensah.Core.dao.IUtilisateurRepository;
import com.ensah.Core.model.Utilisateur;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final IUtilisateurRepository utilisateurRepository;

    public CustomUserDetailsService(IUtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Utilisateur u = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + login));

        if (!u.isActif()) {
            throw new UsernameNotFoundException("Compte désactivé");
        }

        return new User(
                u.getLogin(),
                u.getPassword(),
                u.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getNomRole()))
                        .collect(Collectors.toList())
        );
    }
}