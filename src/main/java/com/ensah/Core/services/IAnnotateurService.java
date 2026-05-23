package com.ensah.Core.services;


import com.ensah.Core.model.Annotateur;
import java.util.List;

public interface IAnnotateurService {

    List<Annotateur> listerAnnotateursActifs();

    Annotateur creerAnnotateur(String nom, String prenom, String login);

    Annotateur modifierAnnotateur(Long id, String nom, String prenom, String login);

    void supprimerAnnotateurLogique(Long id);

    Annotateur getAnnotateurById(Long id);

    String genererMotDePasse(Annotateur annotateur);

    boolean changerMotDePasse(Long id, String ancienMdp, String nouveauMdp);
}