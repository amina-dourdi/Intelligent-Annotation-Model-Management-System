package com.ensah.Core.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
//personnalisation du colonne descriminante DTYPEgi
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("Utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String login;

    @Email
    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private boolean actif = true;

    private boolean passwordChanged = false;

    @Column(unique = true)
    private String firstLoginToken;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;
}