package com.ensah.Core.web;

import com.ensah.Core.dao.ICoupleTexteRepository;
import com.ensah.Core.dao.ITacheRepository;
import com.ensah.Core.model.Annotateur;
import com.ensah.Core.model.CoupleTexte;
import com.ensah.Core.model.Dataset;
import com.ensah.Core.model.Tache;
import com.ensah.Core.model.Annotation;
import com.ensah.Core.model.Utilisateur;
import com.ensah.Core.services.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/admin/datasets")
public class DatasetController {
    private final IKappaService kappaService;
    private final IDatasetService datasetService;
    private final IAnnotateurService annotateurService;
    private final IAffectationService affectationService;
    private final ICoupleTexteRepository coupleTexteRepository;
    private final ITacheRepository tacheRepository;
    private final ISpamDetectionService spamDetectionService;
    private final com.ensah.Core.dao.IAnnotateurRepository annotateurRepository;
    private final com.ensah.Core.dao.IAnnotationRepository annotationRepository;
    private final com.ensah.Core.dao.IUtilisateurRepository utilisateurRepository;
    private final IMlTrainingService mlTrainingService;
    private final com.ensah.Core.mappers.EntityMapper entityMapper;

    public DatasetController(IDatasetService datasetService,
                             IAnnotateurService annotateurService,
                             IAffectationService affectationService,
                             ICoupleTexteRepository coupleTexteRepository,
                             ITacheRepository tacheRepository,
                             ISpamDetectionService spamDetectionService,
                             IKappaService kappaService,
                             com.ensah.Core.dao.IAnnotateurRepository annotateurRepository,
                             com.ensah.Core.dao.IAnnotationRepository annotationRepository,
                             com.ensah.Core.dao.IUtilisateurRepository utilisateurRepository,
                             IMlTrainingService mlTrainingService,
                             com.ensah.Core.mappers.EntityMapper entityMapper) {
        this.datasetService = datasetService;
        this.annotateurService = annotateurService;
        this.affectationService = affectationService;
        this.coupleTexteRepository = coupleTexteRepository;
        this.tacheRepository = tacheRepository;
        this.spamDetectionService = spamDetectionService;
        this.kappaService = kappaService;
        this.annotateurRepository = annotateurRepository;
        this.annotationRepository = annotationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.mlTrainingService = mlTrainingService;
        this.entityMapper = entityMapper;
    }

    // ==================== UC3 : Liste des datasets ====================
    @GetMapping
    public String lister(Model model) {
        List<com.ensah.Core.dtos.DatasetDTO> datasets = datasetService.listerDatasets();

        Map<Long, Integer> avancements = new HashMap<>();
        for (com.ensah.Core.dtos.DatasetDTO ds : datasets) {
            avancements.put(ds.getId(), datasetService.calculerPourcentageAvancement(ds.getId()));
        }

        model.addAttribute("datasets", datasets);
        model.addAttribute("avancements", avancements);
        return "admin/datasets";
    }

    // ==================== Création ====================
    @GetMapping("/nouveau")
    public String formulaireCreation() {
        return "admin/dataset-create";
    }

    @PostMapping("/nouveau")
    public String creer(@RequestParam String nom,
                        @RequestParam(required = false) String description,
                        @RequestParam String classes,
                        @RequestParam("fichier") MultipartFile fichier,
                        RedirectAttributes redirectAttributes) {
        try {
            List<String> listeClasses = Arrays.asList(classes.split(";"));
            datasetService.creerDataset(nom, description, listeClasses, fichier);
            redirectAttributes.addFlashAttribute("success", "Dataset créé avec succès");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'import : " + e.getMessage());
        }
        return "redirect:/admin/datasets";
    }

    // ==================== UC3.1 : Détails dataset ====================
    @GetMapping("/{id}")
    public String details(@PathVariable Long id,
                          @RequestParam(defaultValue = "0") int page,
                          Model model) {
        Dataset ds = datasetService.getDatasetEntityById(id);
        int avancement = datasetService.calculerPourcentageAvancement(id);

        // Pagination : 100 couples par page
        Pageable pageable = PageRequest.of(page, 100);
        Page<com.ensah.Core.dtos.CoupleTexteDTO> couplesPage = coupleTexteRepository.findByDatasetId(id, pageable).map(entityMapper::toDTO);

        model.addAttribute("dataset", ds);
        model.addAttribute("avancement", avancement);
        model.addAttribute("couplesPage", couplesPage);
        return "admin/dataset-detail";
    }

    // ==================== UC3.3 : Page affectation annotateurs ====================
    @GetMapping("/{id}/annotateurs")
    public String pageAffectationAnnotateurs(@PathVariable Long id, Model model) {
        com.ensah.Core.dtos.DatasetDTO ds = datasetService.getDatasetDTOById(id);
        List<com.ensah.Core.dtos.AnnotateurDTO> annotateurs = annotateurService.listerAnnotateursDTOActifs();

        List<com.ensah.Core.dtos.TacheDTO> tachesExistantes = affectationService.listerTachesDTOParDataset(id);
        List<Long> idsAffectes = tachesExistantes.stream()
                .map(t -> t.getAnnotateur().getId())
                .toList();

        model.addAttribute("dataset", ds);
        model.addAttribute("annotateurs", annotateurs);
        model.addAttribute("idsAffectes", idsAffectes);
        return "admin/dataset-annotateurs";
    }

    @PostMapping("/{id}/affecter")
    public String affecter(@PathVariable Long id,
                           @RequestParam(required = false) List<Long> annotateurIds,
                           @RequestParam LocalDate dateLimite,
                           RedirectAttributes redirectAttributes) {
        try {
            affectationService.affecterAnnotateurs(id, annotateurIds, dateLimite);
            redirectAttributes.addFlashAttribute("success",
                    annotateurIds.size() + " annotateurs affectés avec succès");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de l'affectation : " + e.getMessage());
        }
        return "redirect:/admin/datasets/" + id;
    }

    // ==================== UC3.2 : Désaffectation (conservation annotations) ====================
    @PostMapping("/{datasetId}/taches/{tacheId}/supprimer")
    public String supprimerTache(@PathVariable Long datasetId,
                                 @PathVariable Long tacheId,
                                 RedirectAttributes redirectAttributes) {

        // 🔒 Minimum 3 annotateurs requis
        List<Tache> tachesDataset = tacheRepository.findByDatasetId(datasetId);
        if (tachesDataset.size() <= 3) {
            redirectAttributes.addFlashAttribute("error",
                    "Désaffectation impossible : un dataset doit conserver au moins 3 annotateurs");
            return "redirect:/admin/datasets/" + datasetId;
        }

        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche introuvable"));
        Long annotateurId = tache.getAnnotateur().getId();

        if (!tache.getDataset().getId().equals(datasetId)) {
            redirectAttributes.addFlashAttribute("error", "Opération invalide");
            return "redirect:/admin/datasets/" + datasetId;
        }

        // 1️⃣ Supprimer la tâche (désaffectation)
        tacheRepository.deleteById(tacheId);

        // 2️⃣ 🔁 Rééquilibrer automatiquement les couples orphelins
        try {
            affectationService.reequilibrerApresDesaffectation(datasetId, annotateurId);
            redirectAttributes.addFlashAttribute("success",
                    "Annotateur désaffecté et couples rééquilibrés. Ses annotations sont conservées.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("warning",
                    "Désaffectation faite, mais rééquilibrage incomplet : " + e.getMessage());
        }

        return "redirect:/admin/datasets/" + datasetId;
    }
    @GetMapping("/{id}/metrique")
    public String afficherMetrique(@PathVariable Long id, Model model) {
        com.ensah.Core.dtos.DatasetDTO ds = datasetService.getDatasetDTOById(id);
        IKappaService.KappaResult kappa = kappaService.calculerFleissKappa(id);

        model.addAttribute("dataset", ds);
        model.addAttribute("kappa", kappa);
        return "admin/dataset-metrique";
    }

    // ==================== UC5.2 : Détection des spammeurs ====================
    @GetMapping("/{id}/spammeurs")
    public String detecterSpammeurs(@PathVariable Long id, Model model) {
        com.ensah.Core.dtos.DatasetDTO ds = datasetService.getDatasetDTOById(id);
        List<Map<String, Object>> analyses = spamDetectionService.detecterSpammeursParKappa(id);

        long spammeurCount = analyses.stream().filter(a -> Boolean.TRUE.equals(a.get("estSpammeur"))).count();
        long suspectCount = analyses.stream().filter(a -> Boolean.TRUE.equals(a.get("estSuspect"))).count();

        model.addAttribute("dataset", ds);
        model.addAttribute("analyses", analyses);
        model.addAttribute("spammeurCount", spammeurCount);
        model.addAttribute("suspectCount", suspectCount);
        return "admin/dataset-spammeurs";
    }

    @PostMapping("/{datasetId}/spammeurs/{annotateurId}/supprimer")
    public String supprimerSpammeur(@PathVariable Long datasetId,
                                    @PathVariable Long annotateurId,
                                    RedirectAttributes redirectAttributes) {
        try {
            // 1. Vérifier qu'il restera au moins 3 annotateurs après suppression
            List<Tache> tachesDataset = tacheRepository.findByDatasetId(datasetId);
            long nbAnnotateursUniques = tachesDataset.stream()
                    .map(t -> t.getAnnotateur().getId())
                    .distinct()
                    .filter(id -> !id.equals(annotateurId))
                    .count();

            if (nbAnnotateursUniques < 3) {
                redirectAttributes.addFlashAttribute("error",
                        "Suppression impossible : il resterait moins de 3 annotateurs sur ce dataset");
                return "redirect:/admin/datasets/" + datasetId + "/spammeurs";
            }

            // 2. Supprimer les tâches de cet annotateur sur ce dataset
            Optional<Tache> tacheASupprimer = tacheRepository.findByDatasetIdAndAnnotateurId(datasetId, annotateurId);
            tacheASupprimer.ifPresent(tacheRepository::delete);

            // 3. 🔁 Rééquilibrer les couples qui sont passés sous 3 annotateurs
            affectationService.reequilibrerApresDesaffectation(datasetId, annotateurId);

            // 4. Supprimer l'annotateur (logique — conserve les annotations en base)
            annotateurService.supprimerAnnotateurLogique(annotateurId);

            redirectAttributes.addFlashAttribute("success",
                    "Spammeur supprimé, couples rééquilibrés, annotations conservées");

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("warning",
                    "Annotateur supprimé, mais rééquilibrage incomplet : " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la suppression : " + e.getMessage());
        }

        return "redirect:/admin/datasets/" + datasetId + "/spammeurs";
    }

    // Supprimé : Auto-Annotation (ML Python)

    // ==================== UC5 : Entraînement NLP ====================
    @GetMapping("/{id}/train")
    public String pageEntrainement(@PathVariable Long id, Model model) {
        Dataset ds = datasetService.getDatasetEntityById(id);
        List<com.ensah.Core.model.EntrainementModele> historique = mlTrainingService.getHistoriqueByDataset(id);
        
        model.addAttribute("dataset", ds);
        model.addAttribute("historique", historique);
        return "admin/dataset-train";
    }

    @PostMapping("/{id}/train")
    public String lancerEntrainement(@PathVariable Long id,
                                     @RequestParam int epochs,
                                     @RequestParam double lr,
                                     @RequestParam int batchSize,
                                     org.springframework.security.core.Authentication auth,
                                     RedirectAttributes redirectAttributes) {
        try {
            String login = auth.getName();
            Utilisateur u = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + login));
            
            com.ensah.Core.model.EntrainementModele modele = mlTrainingService.lancerEntrainement(id, u.getId(), epochs, lr, batchSize);
            
            if ("ERREUR".equals(modele.getStatut())) {
                // Check if the logs contain our specific ValueError (with or without accents)
                if (modele.getLogsConsole() != null && (modele.getLogsConsole().contains("Pas assez de données") || modele.getLogsConsole().contains("Pas assez de donnees"))) {
                    redirectAttributes.addFlashAttribute("error", "L'entraînement a échoué : Ce dataset n'a pas encore assez de données annotées par des humains. Veuillez annoter au moins 5 textes.");
                } else {
                    redirectAttributes.addFlashAttribute("error", "L'entraînement a échoué. Veuillez consulter les logs (bouton 'Logs' dans le tableau) pour voir l'erreur exacte.");
                }
            } else {
                redirectAttributes.addFlashAttribute("success", "L'entraînement a été lancé et terminé avec succès.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'entraînement : " + e.getMessage());
        }
        return "redirect:/admin/datasets/" + id + "/train";
    }
}