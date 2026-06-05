package com.ensah.Config;

import com.ensah.Core.dao.*;
import com.ensah.Core.model.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final IRoleRepository roleRepository;
    private final IUtilisateurRepository utilisateurRepository;
    private final IAnnotateurRepository annotateurRepository;
    private final IDatasetRepository datasetRepository;
    private final IClassePossibleRepository classePossibleRepository;
    private final ICoupleTexteRepository coupleTexteRepository;
    private final ITacheRepository tacheRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(IRoleRepository roleRepository,
                           IUtilisateurRepository utilisateurRepository,
                           IAnnotateurRepository annotateurRepository,
                           IDatasetRepository datasetRepository,
                           IClassePossibleRepository classePossibleRepository,
                           ICoupleTexteRepository coupleTexteRepository,
                           ITacheRepository tacheRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.annotateurRepository = annotateurRepository;
        this.datasetRepository = datasetRepository;
        this.classePossibleRepository = classePossibleRepository;
        this.coupleTexteRepository = coupleTexteRepository;
        this.tacheRepository = tacheRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        Role adminRole = roleRepository.findByNomRole("ADMIN_ROLE").orElse(null);
        if (adminRole == null) {
            adminRole = new Role();
            adminRole.setNomRole("ADMIN_ROLE");
            roleRepository.save(adminRole);
        }

        Role annotatorRole = roleRepository.findByNomRole("ANNOTATOR_ROLE").orElse(null);
        if (annotatorRole == null) {
            annotatorRole = new Role();
            annotatorRole.setNomRole("ANNOTATOR_ROLE");
            roleRepository.save(annotatorRole);
        }

        if (!utilisateurRepository.existsByLogin("admin")) {
            Administrateur admin = new Administrateur();
            admin.setNom("Admin");
            admin.setPrenom("System");
            admin.setLogin("admin");
            admin.setEmail("admin@annota.com");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setActif(true);
            admin.setPasswordChanged(true);
            admin.setRole(adminRole);
            utilisateurRepository.save(admin);
        }

        Annotateur user1 = null;
        if (!utilisateurRepository.existsByLogin("user1")) {
            user1 = new Annotateur();
            user1.setNom("USER1");
            user1.setPrenom("Annotateur Test");
            user1.setLogin("user1");
            user1.setEmail("user1@annota.com");
            user1.setPassword(passwordEncoder.encode("user1"));
            user1.setActif(true);
            user1.setPasswordChanged(true);
            user1.setRole(annotatorRole);
            annotateurRepository.save(user1);
        } else {
            Optional<Annotateur> opt = annotateurRepository.findById(
                    utilisateurRepository.findByLogin("user1").get().getId()
            );
            if (opt.isPresent()) user1 = opt.get();
        }

        if (datasetRepository.count() == 0) {
            Dataset ds = new Dataset();
            ds.setNomDataset("Similarité Textuelle NLI");
            ds.setDescription("Dataset de test pour évaluer la similarité sémantique et l'inférence textuelle (NLI).");
            ds.setFichierNom("nli_test_dataset.csv");
            datasetRepository.save(ds);

            List<String> classes = Arrays.asList("entails", "neutral", "contradiction");
            List<ClassePossible> cps = new ArrayList<>();
            for (String c : classes) {
                ClassePossible cp = new ClassePossible();
                cp.setTexteClasse(c);
                cp.setDataset(ds);
                classePossibleRepository.save(cp);
                cps.add(cp);
            }
            ds.setClassesPossibles(cps);

            List<CoupleTexte> couples = new ArrayList<>();

            CoupleTexte ct1 = new CoupleTexte();
            ct1.setTexte1("Un homme joue au football sous la pluie battante.");
            ct1.setTexte2("Un homme fait du sport en plein air.");
            ct1.setDataset(ds);
            coupleTexteRepository.save(ct1);
            couples.add(ct1);

            CoupleTexte ct2 = new CoupleTexte();
            ct2.setTexte1("Une femme lit attentivement un roman sur un banc public.");
            ct2.setTexte2("Une femme dort profondément dans sa chambre.");
            ct2.setDataset(ds);
            coupleTexteRepository.save(ct2);
            couples.add(ct2);

            CoupleTexte ct3 = new CoupleTexte();
            ct3.setTexte1("Deux magnifiques chiens golden retrievers courent joyeusement dans le parc.");
            ct3.setTexte2("Des animaux domestiques sont en mouvement à l'extérieur.");
            ct3.setDataset(ds);
            coupleTexteRepository.save(ct3);
            couples.add(ct3);

            CoupleTexte ct4 = new CoupleTexte();
            ct4.setTexte1("Un petit chat siamois dort paisiblement sur le canapé du salon.");
            ct4.setTexte2("Le félin court à toute vitesse après une souris grise.");
            ct4.setDataset(ds);
            coupleTexteRepository.save(ct4);
            couples.add(ct4);

            CoupleTexte ct5 = new CoupleTexte();
            ct5.setTexte1("Un groupe d'amis proches boit un expresso en terrasse.");
            ct5.setTexte2("Plusieurs personnes partagent un moment de convivialité en buvant un café.");
            ct5.setDataset(ds);
            coupleTexteRepository.save(ct5);
            couples.add(ct5);

            ds.setCouples(couples);
            datasetRepository.save(ds);

            if (user1 != null) {
                Tache tUser1 = new Tache();
                tUser1.setDataset(ds);
                tUser1.setAnnotateur(user1);
                tUser1.setDateLimite(LocalDate.now().plusDays(10));
                tUser1.setCouples(new ArrayList<>(couples));
                tacheRepository.save(tUser1);
            }
        }
    }
}