package com.ensah.Core.web;


import com.ensah.Core.model.Annotateur;
import com.ensah.Core.model.Dataset;
import com.ensah.Core.services.IAnnotateurService;
import com.ensah.Core.services.IAffectationService;
import com.ensah.Core.services.IDatasetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/datasets")
public class DatasetController {

    private final IDatasetService datasetService;
    private final IAnnotateurService annotateurService;
    private final IAffectationService affectationService;

    public DatasetController(IDatasetService datasetService,
                             IAnnotateurService annotateurService,
                             IAffectationService affectationService) {
        this.datasetService = datasetService;
        this.annotateurService = annotateurService;
        this.affectationService = affectationService;
    }

    @GetMapping
    public String lister(Model model) {
        model.addAttribute("datasets", datasetService.listerDatasets());
        return "admin/datasets";
    }

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

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        Dataset ds = datasetService.getDatasetById(id);
        int avancement = datasetService.calculerPourcentageAvancement(id);
        List<Annotateur> annotateurs = annotateurService.listerAnnotateursActifs();
        model.addAttribute("dataset", ds);
        model.addAttribute("avancement", avancement);
        model.addAttribute("annotateurs", annotateurs);
        return "admin/dataset-detail";
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
}