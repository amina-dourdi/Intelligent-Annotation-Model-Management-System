package com.ensah.Core.controllers;

import com.ensah.Core.dao.*;
import com.ensah.Core.model.*;
import com.ensah.Core.services.IAffectationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class AnnotateurController {

    private final IUtilisateurRepository utilisateurRepository;
    private final IAnnotateurRepository annotateurRepository;
    private final IAffectationService affectationService;
    private final com.ensah.Core.services.IAnnotateurService annotateurService;
    private final com.ensah.Core.services.IAnnotationWorkspaceService annotationWorkspaceService;
    private final com.ensah.Core.dao.IAnnotationRepository annotationRepository;
    private final com.ensah.Core.mappers.EntityMapper entityMapper;

    public AnnotateurController(IUtilisateurRepository utilisateurRepository,
                                 IAnnotateurRepository annotateurRepository,
                                 IAffectationService affectationService,
                                 com.ensah.Core.services.IAnnotateurService annotateurService,
                                 com.ensah.Core.services.IAnnotationWorkspaceService annotationWorkspaceService,
                                 com.ensah.Core.dao.IAnnotationRepository annotationRepository,
                                 com.ensah.Core.mappers.EntityMapper entityMapper) {
        this.utilisateurRepository = utilisateurRepository;
        this.annotateurRepository = annotateurRepository;
        this.affectationService = affectationService;
        this.annotateurService = annotateurService;
        this.annotationWorkspaceService = annotationWorkspaceService;
        this.annotationRepository = annotationRepository;
        this.entityMapper = entityMapper;
    }

    private com.ensah.Core.dtos.AnnotateurDTO getCurrentAnnotateurDTO() {
        return entityMapper.toDTO(getCurrentAnnotateur());
    }

    private Annotateur getCurrentAnnotateur() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }
        String login = auth.getName();
        Utilisateur u = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + login));
        return annotateurRepository.findById(u.getId())
                .orElseThrow(() -> new RuntimeException("Annotateur introuvable pour l'id : " + u.getId()));
    }

    @GetMapping("/annotator")
    public String dashboard(@RequestParam(value = "filter", defaultValue = "all") String filter, Model model) {
        com.ensah.Core.dtos.AnnotateurDTO annotateur = getCurrentAnnotateurDTO();
        Long annotateurId = annotateur.getId();

        List<com.ensah.Core.dtos.TacheDTO> toutesLesTaches = affectationService.listerTachesDTOParAnnotateur(annotateurId);
        List<Map<String, Object>> taskWrappers = annotationWorkspaceService.getTaskWrappers(annotateurId, filter);
        Map<String, Integer> stats = annotationWorkspaceService.getDashboardStatsDTO(annotateurId, toutesLesTaches);

        model.addAttribute("annotator", annotateur);
        model.addAttribute("tasks", taskWrappers);
        model.addAttribute("activeFilter", filter);
        model.addAttribute("totalTasks", toutesLesTaches.size());
        model.addAttribute("completedTasks", stats.get("completedTasksCount"));
        model.addAttribute("pendingTasks", toutesLesTaches.size() - stats.get("completedTasksCount"));
        model.addAttribute("totalAssigned", stats.get("totalAssigned"));
        model.addAttribute("totalAnnotated", stats.get("totalAnnotatedCount"));
        model.addAttribute("globalProgress", stats.get("globalProgress"));

        return "annotator/dashboard";
    }

    @GetMapping("/annotator/task/{tacheId}")
    public String workspace(@PathVariable("tacheId") Long tacheId,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "saved", defaultValue = "false") boolean saved,
                            Model model) {
        com.ensah.Core.dtos.AnnotateurDTO annotateur = getCurrentAnnotateurDTO();
        Long annotateurId = annotateur.getId();

        com.ensah.Core.dtos.TacheDTO tache = annotationWorkspaceService.getTacheDTOForAnnotateur(tacheId, annotateurId);

        Pageable pageable = PageRequest.of(page, 1);
        Page<com.ensah.Core.dtos.CoupleTexteDTO> couplePage = annotationWorkspaceService.getCoupleDTOPage(tacheId, pageable);

        com.ensah.Core.dtos.CoupleTexteDTO couple = null;
        com.ensah.Core.dtos.AnnotationDTO existingAnnotation = null;
        if (couplePage.hasContent()) {
            couple = couplePage.getContent().get(0);
            existingAnnotation = annotationWorkspaceService.getExistingAnnotationDTO(couple.getId(), annotateurId).orElse(null);
        }

        int totalCouples = tache.getTotalCouples();
        long annotatedCouples = annotationWorkspaceService.getAnnotatedCount(tacheId, annotateurId);
        int progressPercent = annotationWorkspaceService.getProgressPercent(tacheId, annotateurId, totalCouples);

        model.addAttribute("task", tache);
        model.addAttribute("couplePage", couplePage);
        model.addAttribute("couple", couple);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", couplePage.getTotalPages());
        model.addAttribute("classes", tache.getDataset().getClassesPossibles());
        model.addAttribute("existingAnnotation", existingAnnotation);
        model.addAttribute("progressPercent", progressPercent);
        model.addAttribute("annotatedCount", annotatedCouples);
        model.addAttribute("totalCount", totalCouples);
        model.addAttribute("saved", saved);

        return "annotator/workspace";
    }

    @PostMapping("/annotator/task/{tacheId}/annotate")
    public String annotate(@PathVariable("tacheId") Long tacheId,
                           @RequestParam("coupleTexteId") Long coupleTexteId,
                           @RequestParam("classeChoisie") String classeChoisie,
                           @RequestParam("page") int page) {
        com.ensah.Core.dtos.AnnotateurDTO annotateur = getCurrentAnnotateurDTO();
        Long annotateurId = annotateur.getId();

        annotationWorkspaceService.saveAnnotation(tacheId, annotateurId, coupleTexteId, classeChoisie);

        com.ensah.Core.dtos.TacheDTO tache = annotationWorkspaceService.getTacheDTOForAnnotateur(tacheId, annotateurId);
        int totalCouples = tache.getTotalCouples();
        
        if (page + 1 < totalCouples) {
            return "redirect:/annotator/task/" + tacheId + "?page=" + (page + 1);
        } else {
            return "redirect:/annotator/task/" + tacheId + "?page=" + page + "&saved=true";
        }
    }

    @GetMapping("/annotator/stats")
    public String stats(Model model) {
        com.ensah.Core.dtos.AnnotateurDTO annotateur = getCurrentAnnotateurDTO();
        Long annotateurId = annotateur.getId();

        List<com.ensah.Core.dtos.TacheDTO> taches = affectationService.listerTachesDTOParAnnotateur(annotateurId);
        int totalAssigned = 0;
        for (com.ensah.Core.dtos.TacheDTO t : taches) {
            totalAssigned += t.getTotalCouples();
        }

        List<Annotation> annotations = annotationRepository.findByAnnotateurId(annotateurId);
        int totalAnnotated = annotations.size();

        int progressPercent = totalAssigned == 0 ? 0 : (totalAnnotated * 100) / totalAssigned;

        // Calcul de la distribution par classe
        List<Object[]> classCounts = annotationRepository.countAnnotationsByClassForAnnotateur(annotateurId);
        List<Map<String, Object>> distribution = new ArrayList<>();
        for (Object[] row : classCounts) {
            Map<String, Object> map = new HashMap<>();
            map.put("classe", row[0]);
            map.put("count", row[1]);
            distribution.add(map);
        }

        // Récupérer les 10 dernières annotations
        List<com.ensah.Core.dtos.AnnotationDTO> recentAnnotations = annotations.stream()
                .sorted((a1, a2) -> {
                    if (a1.getDateAnnotation() == null) return 1;
                    if (a2.getDateAnnotation() == null) return -1;
                    return a2.getDateAnnotation().compareTo(a1.getDateAnnotation());
                })
                .limit(10)
                .map(entityMapper::toDTO)
                .toList();

        model.addAttribute("annotator", annotateur);
        model.addAttribute("totalAssigned", totalAssigned);
        model.addAttribute("totalAnnotated", totalAnnotated);
        model.addAttribute("progressPercent", progressPercent);
        model.addAttribute("distribution", distribution);
        model.addAttribute("recentAnnotations", recentAnnotations);

        return "annotator/stats";
    }

    @GetMapping("/annotator/profile")
    public String profile(Model model) {
        com.ensah.Core.dtos.AnnotateurDTO annotateur = getCurrentAnnotateurDTO();
        model.addAttribute("annotator", annotateur);
        return "annotator/profile";
    }

    @PostMapping("/annotator/profile/update")
    public String updateProfile(@RequestParam("nom") String nom,
                                @RequestParam("prenom") String prenom,
                                @RequestParam("login") String login,
                                org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        com.ensah.Core.dtos.AnnotateurDTO annotateur = getCurrentAnnotateurDTO();
        try {
            annotateurService.modifierAnnotateur(annotateur.getId(), nom, prenom, login, annotateur.getEmail());
            redirectAttributes.addFlashAttribute("successMessage", "Profil mis à jour avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la mise à jour : " + e.getMessage());
        }
        return "redirect:/annotator/profile";
    }

    @PostMapping("/annotator/profile/password")
    public String updatePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        com.ensah.Core.dtos.AnnotateurDTO annotateur = getCurrentAnnotateurDTO();
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorPassword", "Les nouveaux mots de passe ne correspondent pas.");
            return "redirect:/annotator/profile";
        }
        
        boolean success = annotateurService.changerMotDePasse(annotateur.getId(), currentPassword, newPassword);
        if (success) {
            redirectAttributes.addFlashAttribute("successPassword", "Mot de passe modifié avec succès.");
        } else {
            redirectAttributes.addFlashAttribute("errorPassword", "Mot de passe actuel incorrect.");
        }
        
        return "redirect:/annotator/profile";
    }

    @GetMapping("/admin")
    public String adminFallback(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("username", auth != null ? auth.getName() : "Admin");
        return "admin_fallback";
    }
}
