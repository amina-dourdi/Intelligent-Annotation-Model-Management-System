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
    private final ITacheRepository tacheRepository;
    private final ICoupleTexteRepository coupleTexteRepository;
    private final IAnnotationRepository annotationRepository;
    private final IAffectationService affectationService;
    private final com.ensah.Core.services.IAnnotateurService annotateurService;

    public AnnotateurController(IUtilisateurRepository utilisateurRepository,
                                 IAnnotateurRepository annotateurRepository,
                                 ITacheRepository tacheRepository,
                                 ICoupleTexteRepository coupleTexteRepository,
                                 IAnnotationRepository annotationRepository,
                                 IAffectationService affectationService,
                                 com.ensah.Core.services.IAnnotateurService annotateurService) {
        this.utilisateurRepository = utilisateurRepository;
        this.annotateurRepository = annotateurRepository;
        this.tacheRepository = tacheRepository;
        this.coupleTexteRepository = coupleTexteRepository;
        this.annotationRepository = annotationRepository;
        this.affectationService = affectationService;
        this.annotateurService = annotateurService;
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
        Annotateur annotateur = getCurrentAnnotateur();
        Long annotateurId = annotateur.getId();

        List<Tache> toutesLesTaches = affectationService.listerTachesParAnnotateur(annotateurId);
        List<Map<String, Object>> taskWrappers = new ArrayList<>();

        int totalAssigned = 0;
        int totalAnnotatedCount = 0;
        int completedTasksCount = 0;

        for (Tache t : toutesLesTaches) {
            int totalCouples = t.getCouples().size();
            totalAssigned += totalCouples;

            int annotatedCouples = 0;
            for (CoupleTexte ct : t.getCouples()) {
                if (annotationRepository.findByCoupleTexteIdAndAnnotateurId(ct.getId(), annotateurId).isPresent()) {
                    annotatedCouples++;
                }
            }
            totalAnnotatedCount += annotatedCouples;

            int percent = totalCouples == 0 ? 0 : (annotatedCouples * 100) / totalCouples;
            boolean isCompleted = percent == 100;
            if (isCompleted) completedTasksCount++;

            Map<String, Object> wrapper = new HashMap<>();
            wrapper.put("tache", t);
            wrapper.put("percent", percent);
            wrapper.put("annotatedCount", annotatedCouples);
            wrapper.put("totalCount", totalCouples);
            wrapper.put("status", isCompleted ? "Terminé" : "À faire");

            if ("all".equals(filter) ||
                ("todo".equals(filter) && !isCompleted) ||
                ("done".equals(filter) && isCompleted)) {
                taskWrappers.add(wrapper);
            }
        }

        int globalProgress = totalAssigned == 0 ? 0 : (totalAnnotatedCount * 100) / totalAssigned;

        model.addAttribute("annotator", annotateur);
        model.addAttribute("tasks", taskWrappers);
        model.addAttribute("activeFilter", filter);
        model.addAttribute("totalTasks", toutesLesTaches.size());
        model.addAttribute("completedTasks", completedTasksCount);
        model.addAttribute("pendingTasks", toutesLesTaches.size() - completedTasksCount);
        model.addAttribute("totalAssigned", totalAssigned);
        model.addAttribute("totalAnnotated", totalAnnotatedCount);
        model.addAttribute("globalProgress", globalProgress);

        return "annotator/dashboard";
    }

    @GetMapping("/annotator/task/{tacheId}")
    public String workspace(@PathVariable("tacheId") Long tacheId,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "saved", defaultValue = "false") boolean saved,
                            Model model) {
        Annotateur annotateur = getCurrentAnnotateur();
        Long annotateurId = annotateur.getId();

        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche introuvable : " + tacheId));

        if (!tache.getAnnotateur().getId().equals(annotateurId)) {
            throw new RuntimeException("Accès refusé : Cette tâche ne vous est pas assignée.");
        }

        Pageable pageable = PageRequest.of(page, 1);
        Page<CoupleTexte> couplePage = coupleTexteRepository.findCouplesByTacheId(tacheId, pageable);

        CoupleTexte couple = null;
        Annotation existingAnnotation = null;
        if (couplePage.hasContent()) {
            couple = couplePage.getContent().get(0);
            Optional<Annotation> optAnn = annotationRepository.findByCoupleTexteIdAndAnnotateurId(couple.getId(), annotateurId);
            if (optAnn.isPresent()) {
                existingAnnotation = optAnn.get();
            }
        }

        // Calcul de la progression en temps réel sur la tâche
        int totalCouples = tache.getCouples().size();
        int annotatedCouples = 0;
        for (CoupleTexte ct : tache.getCouples()) {
            if (annotationRepository.findByCoupleTexteIdAndAnnotateurId(ct.getId(), annotateurId).isPresent()) {
                annotatedCouples++;
            }
        }
        int progressPercent = totalCouples == 0 ? 0 : (annotatedCouples * 100) / totalCouples;

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
        Annotateur annotateur = getCurrentAnnotateur();
        Long annotateurId = annotateur.getId();

        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche introuvable : " + tacheId));

        if (!tache.getAnnotateur().getId().equals(annotateurId)) {
            throw new RuntimeException("Accès refusé.");
        }

        CoupleTexte ct = coupleTexteRepository.findById(coupleTexteId)
                .orElseThrow(() -> new RuntimeException("CoupleTexte introuvable : " + coupleTexteId));

        Optional<Annotation> optAnn = annotationRepository.findByCoupleTexteIdAndAnnotateurId(coupleTexteId, annotateurId);
        Annotation annotation;
        if (optAnn.isPresent()) {
            annotation = optAnn.get();
            annotation.setClasseChoisie(classeChoisie);
        } else {
            annotation = new Annotation();
            annotation.setCoupleTexte(ct);
            annotation.setAnnotateur(annotateur);
            annotation.setClasseChoisie(classeChoisie);
        }
        annotationRepository.save(annotation);

        int totalCouples = tache.getCouples().size();
        if (page + 1 < totalCouples) {
            return "redirect:/annotator/task/" + tacheId + "?page=" + (page + 1);
        } else {
            return "redirect:/annotator/task/" + tacheId + "?page=" + page + "&saved=true";
        }
    }

    @GetMapping("/annotator/stats")
    public String stats(Model model) {
        Annotateur annotateur = getCurrentAnnotateur();
        Long annotateurId = annotateur.getId();

        List<Tache> taches = affectationService.listerTachesParAnnotateur(annotateurId);
        int totalAssigned = 0;
        for (Tache t : taches) {
            totalAssigned += t.getCouples().size();
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
        List<Annotation> recentAnnotations = annotations.stream()
                .sorted((a1, a2) -> {
                    if (a1.getDateAnnotation() == null) return 1;
                    if (a2.getDateAnnotation() == null) return -1;
                    return a2.getDateAnnotation().compareTo(a1.getDateAnnotation());
                })
                .limit(10)
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
        Annotateur annotateur = getCurrentAnnotateur();
        model.addAttribute("annotator", annotateur);
        return "annotator/profile";
    }

    @PostMapping("/annotator/profile/update")
    public String updateProfile(@RequestParam("nom") String nom,
                                @RequestParam("prenom") String prenom,
                                @RequestParam("login") String login,
                                org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Annotateur annotateur = getCurrentAnnotateur();
        try {
            annotateurService.modifierAnnotateur(annotateur.getId(), nom, prenom, login);
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
        Annotateur annotateur = getCurrentAnnotateur();
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
