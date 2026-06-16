package com.ensah.Core.web;

import com.ensah.Core.model.Annotateur;
import com.ensah.Core.services.IAnnotateurService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin/annotateurs")
public class UtilisateurController {

    private final IAnnotateurService annotateurService;

    public UtilisateurController(IAnnotateurService annotateurService) {
        this.annotateurService = annotateurService;
    }

    @GetMapping
    public String lister(Model model) {
        model.addAttribute("annotateurs", annotateurService.listerAnnotateursDTOActifs());
        return "admin/users";
    }

    @GetMapping("/{id}/modifier")
    public String formulaireModification(@PathVariable Long id, Model model) {
        com.ensah.Core.dtos.AnnotateurDTO annotateur = annotateurService.getAnnotateurDTOById(id);
        model.addAttribute("annotateur", annotateur);
        return "admin/user-edit";
    }

    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        model.addAttribute("annotateur", new Annotateur());
        return "admin/user-create";
    }

    @PostMapping("/nouveau")
    public String creer(@RequestParam String nom,
                        @RequestParam String prenom,
                        @RequestParam String login,
                        @RequestParam String email,
                        RedirectAttributes redirectAttributes) {
        try {
            annotateurService.creerAnnotateur(nom, prenom, login, email);
            redirectAttributes.addFlashAttribute("message",
                    "Annotateur créé avec succès. Un email avec le mot de passe a été envoyé à " + email);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/annotateurs";
    }

    @PostMapping("/{id}/modifier")
    public String modifier(@PathVariable Long id,
                           @RequestParam String nom,
                           @RequestParam String prenom,
                           @RequestParam String login,
                           @RequestParam String email,
                           RedirectAttributes redirectAttributes) {
        try {
            annotateurService.modifierAnnotateur(id, nom, prenom, login, email);
            redirectAttributes.addFlashAttribute("message", "Annotateur modifié avec succès");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/annotateurs";
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id) {
        annotateurService.supprimerAnnotateurLogique(id);
        return "redirect:/admin/annotateurs";
    }
    @GetMapping("/{id}/progression")
    public String voirProgression(@PathVariable Long id, Model model) {
        Map<String, Object> stats = annotateurService.getStatistiquesProgression(id);
        model.addAttribute("stats", stats);
        return "admin/annotateur-progression";
    }
}