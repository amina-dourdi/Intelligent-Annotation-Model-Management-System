package com.ensah.Core.services;

import com.ensah.Core.model.Annotateur;
import java.util.List;
import java.util.Map;

public interface IAnnotateurService {

    List<com.ensah.Core.dtos.AnnotateurDTO> listerAnnotateursDTOActifs();
    
    List<Annotateur> listerAnnotateursActifs();

    com.ensah.Core.dtos.AnnotateurDTO creerAnnotateur(String nom, String prenom, String login, String email);

    com.ensah.Core.dtos.AnnotateurDTO modifierAnnotateur(Long id, String nom, String prenom, String login, String email);

    void supprimerAnnotateurLogique(Long id);

    com.ensah.Core.dtos.AnnotateurDTO getAnnotateurDTOById(Long id);

    Annotateur getAnnotateurById(Long id);

    String genererMotDePasse();

    boolean changerMotDePasse(Long id, String ancienMdp, String nouveauMdp);

    com.ensah.Core.dtos.AnnotateurDTO getAnnotateurDTOByFirstLoginToken(String token);

    Annotateur getAnnotateurByFirstLoginToken(String token);

    /**
     * Récupère les statistiques de progression d'un annotateur
     */
    Map<String, Object> getStatistiquesProgression(Long annotateurId);
    boolean changerMotDePasseFirstLogin(String token, String nouveauMdp);
}