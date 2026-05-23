package com.ensah.Core.web;

import com.ensah.Core.model.Dataset;
import com.ensah.Core.services.IAnnotateurService;
import com.ensah.Core.services.IDatasetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final IDatasetService datasetService;
    private final IAnnotateurService annotateurService;

    public AdminController(IDatasetService datasetService, IAnnotateurService annotateurService) {
        this.datasetService = datasetService;
        this.annotateurService = annotateurService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Dataset> datasets = datasetService.listerDatasets();

        // Statistiques
        int totalDatasets = datasets.size();
        int totalAnnotateurs = annotateurService.listerAnnotateursActifs().size();
        int totalCouples = datasets.stream().mapToInt(ds -> ds.getCouples().size()).sum();

        model.addAttribute("datasets", datasets);
        model.addAttribute("totalDatasets", totalDatasets);
        model.addAttribute("totalAnnotateurs", totalAnnotateurs);
        model.addAttribute("totalCouples", totalCouples);

        return "admin/dashboard";
    }
}