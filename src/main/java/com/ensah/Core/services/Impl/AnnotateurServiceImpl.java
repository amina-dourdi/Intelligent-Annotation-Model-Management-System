package com.ensah.Core.services.Impl;

import com.ensah.Core.dao.*;
import com.ensah.Core.model.*;
import com.ensah.Core.services.Impl.EmailService;
import com.ensah.Core.services.IAnnotateurService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;

@Service
@Transactional
public class AnnotateurServiceImpl implements IAnnotateurService {

    private final IAnnotateurRepository annotateurRepository;
    private final IUtilisateurRepository utilisateurRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ITacheRepository tacheRepository;
    private final IAnnotationRepository annotationRepository;
    private final com.ensah.Core.mappers.EntityMapper entityMapper;

    public AnnotateurServiceImpl(IAnnotateurRepository annotateurRepository,
                                 IUtilisateurRepository utilisateurRepository,
                                 IRoleRepository roleRepository,
                                 PasswordEncoder passwordEncoder,
                                 EmailService emailService,
                                 ITacheRepository tacheRepository,
                                 IAnnotationRepository annotationRepository,
                                 com.ensah.Core.mappers.EntityMapper entityMapper) {
        this.annotateurRepository = annotateurRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.tacheRepository=tacheRepository;
        this.annotationRepository=annotationRepository;
        this.entityMapper = entityMapper;
    }

    @Override
    public List<com.ensah.Core.dtos.AnnotateurDTO> listerAnnotateursDTOActifs() {
        return annotateurRepository.findByActifTrue().stream()
                .map(entityMapper::toDTO)
                .toList();
    }

    @Override
    public List<Annotateur> listerAnnotateursActifs() {
        return annotateurRepository.findByActifTrue();
    }

    @Override
    public com.ensah.Core.dtos.AnnotateurDTO creerAnnotateur(String nom, String prenom, String login, String email) {
        if (utilisateurRepository.existsByLogin(login)) {
            throw new RuntimeException("Login déjà utilisé : " + login);
        }

        Annotateur a = new Annotateur();
        a.setNom(nom);
        a.setPrenom(prenom);
        a.setLogin(login);
        a.setEmail(email);
        a.setActif(true);
        a.setPasswordChanged(false);

        String token = UUID.randomUUID().toString();
        a.setFirstLoginToken(token);

        String rawPassword = genererMotDePasse();
        a.setPassword(passwordEncoder.encode(rawPassword));

        Role roleAnnot = roleRepository.findByNomRole("ANNOTATOR_ROLE")
                .orElseThrow(() -> new RuntimeException("Rôle ANNOTATOR introuvable"));
        a.setRole(roleAnnot);

        annotateurRepository.save(a);

        String nomComplet = prenom + " " + nom;

        try {
            emailService.envoyerMotDePasse(email, nomComplet, rawPassword, token);
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors du déclenchement de l'envoi d'email : " + e.getMessage());
        }

        System.out.println("========================================");
        System.out.println("🔐 NOUVEL ANNOTATEUR CRÉÉ");
        System.out.println("   Login : " + login);
        System.out.println("   Email : " + email);
        System.out.println("   Mot de passe temporaire : " + rawPassword);
        System.out.println("   Lien first-login : http://localhost:8080/first-login?token=" + token);
        System.out.println("========================================");

        return entityMapper.toDTO(a);
    }

    @Override
    public com.ensah.Core.dtos.AnnotateurDTO modifierAnnotateur(Long id, String nom, String prenom, String login, String email) {
        Annotateur a = annotateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annotateur introuvable"));

        if (!a.getLogin().equals(login) && utilisateurRepository.existsByLogin(login)) {
            throw new RuntimeException("Login déjà utilisé : " + login);
        }

        a.setNom(nom);
        a.setPrenom(prenom);
        a.setLogin(login);
        a.setEmail(email);

        Annotateur saved = annotateurRepository.save(a);
        return entityMapper.toDTO(saved);
    }

    @Override
    public void supprimerAnnotateurLogique(Long id) {
        Annotateur a = annotateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annotateur introuvable"));
        a.setActif(false);
        annotateurRepository.save(a);
    }

    @Override
    public com.ensah.Core.dtos.AnnotateurDTO getAnnotateurDTOById(Long id) {
        return entityMapper.toDTO(getAnnotateurById(id));
    }

    @Override
    public Annotateur getAnnotateurById(Long id) {
        return annotateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annotateur introuvable"));
    }
    @Override
    public Map<String, Object> getStatistiquesProgression(Long annotateurId) {
        Annotateur annotateur = annotateurRepository.findById(annotateurId)
                .orElseThrow(() -> new RuntimeException("Annotateur introuvable"));

        List<Tache> taches = tacheRepository.findByAnnotateurId(annotateurId);
        List<Map<String, Object>> progressionParDataset = new ArrayList<>();

        int totalAssignesGlobal = 0;
        int totalAnnotesGlobal = 0;

        for (Tache tache : taches) {
            Dataset ds = tache.getDataset();
            int totalCouples = tache.getCouples().size();
            int couplesAnnotes = 0;

            for (CoupleTexte ct : tache.getCouples()) {
                if (annotationRepository.findByCoupleTexteIdAndAnnotateurId(ct.getId(), annotateurId).isPresent()) {
                    couplesAnnotes++;
                }
            }

            int pourcentage = totalCouples == 0 ? 0 : (couplesAnnotes * 100) / totalCouples;

            Map<String, Object> info = new LinkedHashMap<>();
            info.put("datasetId", ds.getId());
            info.put("datasetNom", ds.getNomDataset());
            info.put("dateLimite", tache.getDateLimite());
            info.put("totalCouples", totalCouples);
            info.put("couplesAnnotes", couplesAnnotes);
            info.put("pourcentage", pourcentage);
            info.put("termine", pourcentage == 100);
            progressionParDataset.add(info);

            totalAssignesGlobal += totalCouples;
            totalAnnotesGlobal += couplesAnnotes;
        }

        int pourcentageGlobal = totalAssignesGlobal == 0 ? 0 : (totalAnnotesGlobal * 100) / totalAssignesGlobal;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("annotateur", annotateur);
        result.put("progressionParDataset", progressionParDataset);
        result.put("totalAssignesGlobal", totalAssignesGlobal);
        result.put("totalAnnotesGlobal", totalAnnotesGlobal);
        result.put("pourcentageGlobal", pourcentageGlobal);
        result.put("nbDatasets", taches.size());

        return result;
    }

    @Override
    public String genererMotDePasse() {
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
        a.setPasswordChanged(true);
        annotateurRepository.save(a);
        return true;
    }

    @Override
    public Annotateur getAnnotateurByFirstLoginToken(String token) {
        return annotateurRepository.findByFirstLoginToken(token)
                .orElseThrow(() -> new RuntimeException("Lien invalide ou expiré"));
    }

    @Override
    public com.ensah.Core.dtos.AnnotateurDTO getAnnotateurDTOByFirstLoginToken(String token) {
        return entityMapper.toDTO(getAnnotateurByFirstLoginToken(token));
    }

    @Override
    public boolean changerMotDePasseFirstLogin(String token, String nouveauMdp) {
        Annotateur a = annotateurRepository.findByFirstLoginToken(token)
                .orElseThrow(() -> new RuntimeException("Lien invalide ou expiré"));

        a.setPassword(passwordEncoder.encode(nouveauMdp));
        a.setPasswordChanged(true);
        a.setFirstLoginToken(null);
        annotateurRepository.save(a);
        return true;
    }
}