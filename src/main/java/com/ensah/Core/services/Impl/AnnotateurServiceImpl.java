package com.ensah.Core.services.Impl;


import com.ensah.Core.dao.IAnnotateurRepository;
import com.ensah.Core.dao.IRoleRepository;
import com.ensah.Core.dao.IUtilisateurRepository;
import com.ensah.Core.model.Annotateur;

import com.ensah.Core.model.Role;
import com.ensah.Core.services.IAnnotateurService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AnnotateurServiceImpl implements IAnnotateurService {

    private IAnnotateurRepository annotateurRepository;
    private IUtilisateurRepository utilisateurRepository;
    private  IRoleRepository roleRepository;
    private  PasswordEncoder passwordEncoder;

    public AnnotateurServiceImpl(IAnnotateurRepository annotateurRepository,
                                 IUtilisateurRepository utilisateurRepository,
                                 IRoleRepository roleRepository,
                                 PasswordEncoder passwordEncoder) {
        this.annotateurRepository = annotateurRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Annotateur> listerAnnotateursActifs() {
        return annotateurRepository.findByActifTrue();
    }

    @Override
    public Annotateur creerAnnotateur(String nom, String prenom, String login) {
        if (utilisateurRepository.existsByLogin(login)) {
            throw new RuntimeException("Login déjà utilisé : " + login);
        }
        Annotateur a = new Annotateur();
        a.setNom(nom);
        a.setPrenom(prenom);
        a.setLogin(login);
        a.setActif(true);

        String rawPassword = genererMotDePasse(a);
        a.setPassword(passwordEncoder.encode(rawPassword));

        Role roleAnnot = roleRepository.findByNomRole("ANNOTATOR_ROLE")
                .orElseThrow(() -> new RuntimeException("Rôle ANNOTATOR_ROLE introuvable"));
        a.setRole(roleAnnot);

        annotateurRepository.save(a);
        return a;
    }

    @Override
    public Annotateur modifierAnnotateur(Long id, String nom, String prenom, String login) {
        Annotateur a = annotateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annotateur introuvable"));
        a.setNom(nom);
        a.setPrenom(prenom);
        a.setLogin(login);
        return annotateurRepository.save(a);
    }

    @Override
    public void supprimerAnnotateurLogique(Long id) {
        Annotateur a = annotateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annotateur introuvable"));
        a.setActif(false);
        annotateurRepository.save(a);
    }

    @Override
    public Annotateur getAnnotateurById(Long id) {
        return annotateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annotateur introuvable"));
    }

    @Override
    public String genererMotDePasse(Annotateur annotateur) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Override
    public boolean changerMotDePasse(Long id, String ancienMdp, String nouveauMdp) {
        Annotateur a = annotateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annotateur introuvable"));
        
        if (!passwordEncoder.matches(ancienMdp, a.getPassword())) {
            return false;
        }
        
        a.setPassword(passwordEncoder.encode(nouveauMdp));
        annotateurRepository.save(a);
        return true;
    }
}