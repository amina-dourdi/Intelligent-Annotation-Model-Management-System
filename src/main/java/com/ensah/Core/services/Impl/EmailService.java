package com.ensah.Core.services.Impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void envoyerMotDePasse(String destinataire, String nomComplet, String motDePasse, String token) {
        try {
            String lien = "http://localhost:8080/first-login?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinataire);
            message.setSubject("Vos identifiants d'accès - Annota");
            message.setText(String.format(
                    "Bonjour %s,\n\n" +
                            "Votre compte annotateur a été créé sur Annota.\n\n" +
                            "Login : %s\n" +
                            "Mot de passe temporaire : %s\n\n" +
                            "Pour votre première connexion, cliquez sur ce lien pour définir votre propre mot de passe :\n%s\n\n" +
                            "Cordialement,\nL'équipe Annota",
                    nomComplet, destinataire, motDePasse, lien
            ));

            mailSender.send(message);
            System.out.println("✅ Email envoyé avec succès à " + destinataire);

        } catch (MailException e) {
            System.err.println("❌ Échec envoi email à " + destinataire + " : " + e.getMessage());
            System.err.println("   Cause probable : mauvaise config SMTP ou mot de passe d'application Gmail incorrect");
        }
    }
}