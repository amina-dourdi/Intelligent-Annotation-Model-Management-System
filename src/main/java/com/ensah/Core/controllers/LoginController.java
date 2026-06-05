package com.ensah.Core.controllers;

import com.ensah.Core.dao.IAnnotateurRepository;
import com.ensah.Core.model.Annotateur;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class LoginController {

    private final IAnnotateurRepository annotateurRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginController(IAnnotateurRepository annotateurRepository,
                           PasswordEncoder passwordEncoder) {
        this.annotateurRepository = annotateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/first-login")
    public String showFirstLogin(@RequestParam("token") String token, Model model) {
        Optional<Annotateur> opt = annotateurRepository.findByFirstLoginToken(token);
        if (opt.isEmpty()) {
            return "redirect:/login?error=token";
        }
        model.addAttribute("token", token);
        model.addAttribute("login", opt.get().getLogin());
        return "first-login";
    }

    @PostMapping("/first-login")
    public String processFirstLogin(@RequestParam("token") String token,
                                    @RequestParam("tempPassword") String tempPassword,
                                    @RequestParam("nouveauMdp") String nouveauMdp,
                                    @RequestParam("confirmationMdp") String confirmationMdp,
                                    RedirectAttributes redirectAttrs) {

        Optional<Annotateur> opt = annotateurRepository.findByFirstLoginToken(token);
        if (opt.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Lien invalide ou expiré.");
            return "redirect:/login";
        }

        Annotateur a = opt.get();

        if (!passwordEncoder.matches(tempPassword, a.getPassword())) {
            redirectAttrs.addFlashAttribute("error", "Mot de passe temporaire incorrect.");
            return "redirect:/first-login?token=" + token;
        }

        if (!nouveauMdp.equals(confirmationMdp)) {
            redirectAttrs.addFlashAttribute("error", "Les mots de passe ne correspondent pas.");
            return "redirect:/first-login?token=" + token;
        }

        if (nouveauMdp.length() < 6) {
            redirectAttrs.addFlashAttribute("error", "Minimum 6 caractères.");
            return "redirect:/first-login?token=" + token;
        }

        a.setPassword(passwordEncoder.encode(nouveauMdp));
        a.setPasswordChanged(true);
        a.setFirstLoginToken(null);
        annotateurRepository.save(a);

        redirectAttrs.addFlashAttribute("success", "Mot de passe changé. Connectez-vous.");
        return "redirect:/login";
    }
}