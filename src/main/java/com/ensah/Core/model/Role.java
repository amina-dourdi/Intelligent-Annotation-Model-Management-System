package com.ensah.Core.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor

@Setter
@Getter

@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nomRole;

    @OneToMany(mappedBy = "role")
    private Set<Utilisateur> utilisateurs = new HashSet<>();

    public Role() {}

    public Role(String nomRole) {
        this.nomRole = nomRole;
    }
}