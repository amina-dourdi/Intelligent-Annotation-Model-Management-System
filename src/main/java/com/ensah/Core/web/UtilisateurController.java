package com.ensah.Core.web;


import com.ensah.Core.model.Annotateur;
import com.ensah.Core.services.IAnnotateurService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/annotateurs")
public class UtilisateurController {

    private final IAnnotateurService annotateurService;

    public UtilisateurController(IAnnotateurService annotateurService) {
        this.annotateurService = annotateurService;
    }

    @GetMapping
    public String lister(Model model) {
        model.addAttribute("annotateurs", annotateurService.listerAnnotateursActifs());
        return "admin/users";
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
                        RedirectAttributes redirectAttributes) {
        Annotateur a = annotateurService.creerAnnotateur(nom, prenom, login);
        redirectAttributes.addFlashAttribute("message",
                "Annotateur créé. Mot de passe généré : " + annotateurService.genererMotDePasse(a));
        return "redirect:/admin/annotateurs";
    }

    @PostMapping("/{id}/modifier")
    public String modifier(@PathVariable Long id,
                           @RequestParam String nom,
                           @RequestParam String prenom,
                           @RequestParam String login) {
        annotateurService.modifierAnnotateur(id, nom, prenom, login);
        return "redirect:/admin/annotateurs";
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id) {
        annotateurService.supprimerAnnotateurLogique(id);
        return "redirect:/admin/annotateurs";
    }
}