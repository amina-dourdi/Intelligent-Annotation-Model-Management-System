package com.ensah.Config;

import com.ensah.Core.dao.IRoleRepository;
import com.ensah.Core.dao.IUtilisateurRepository;
import com.ensah.Core.model.Administrateur;
import com.ensah.Core.model.Role;
import com.Utils.PasswordGen;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// @Component  // Désactivé : DataInitializer gère déjà l'initialisation complète (rôles + admin + données de test)
public class DbInitializer implements CommandLineRunner {

    private final IRoleRepository roleRepository;
    private final IUtilisateurRepository utilisateurRepository;

    public DbInitializer(IRoleRepository roleRepository, IUtilisateurRepository utilisateurRepository) {
        this.roleRepository = roleRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public void run(String... args) {
        // Crée les rôles si absents
        if (roleRepository.findByNomRole("ADMIN_ROLE").isEmpty()) {
            roleRepository.save(new Role("ADMIN_ROLE"));
        }
        if (roleRepository.findByNomRole("ANNOTATOR_ROLE").isEmpty()) {
            roleRepository.save(new Role("ANNOTATOR_ROLE"));
        }

        // Crée l'admin si absent
        if (utilisateurRepository.findByLogin("admin").isEmpty()) {
            Administrateur admin = new Administrateur();
            admin.setNom("Admin");
            admin.setPrenom("System");
            admin.setLogin("admin");
            admin.setPassword(PasswordGen.encode("admin"));
            admin.setActif(true);

            Role adminRole = roleRepository.findByNomRole("ADMIN_ROLE").get();
            admin.setRole(adminRole);

            utilisateurRepository.save(admin);
            System.out.println(" Admin créé : login=admin, password=admin");
        }
    }
}