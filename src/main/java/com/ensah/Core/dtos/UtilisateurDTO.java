package com.ensah.Core.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String login;
    private String email;
    private String type; // ex: "ADMIN", "ANNOTATEUR"
}
