package com.ensah.Core.services;

import com.ensah.Core.model.Annotateur;
import java.util.List;
import java.util.Map;

public interface IAnnotateurService {

    List<Annotateur> listerAnnotateursActifs();

    Annotateur creerAnnotateur(String nom, String prenom, String login, String email);

    Annotateur modifierAnnotateur(Long id, String nom, String prenom, String login, String email);

    void supprimerAnnotateurLogique(Long id);

    Annotateur getAnnotateurById(Long id);

    String genererMotDePasse();

    boolean changerMotDePasse(Long id, String ancienMdp, String nouveauMdp);

    Annotateur getAnnotateurByFirstLoginToken(String token);

    /**
     * Récupère les statistiques de progression d'un annotateur
     */
    Map<String, Object> getStatistiquesProgression(Long annotateurId);
    boolean changerMotDePasseFirstLogin(String token, String nouveauMdp);
}