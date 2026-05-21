package com.ensah.Core.model;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class Annotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String classeChoisie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_texte_id", nullable = false)
    private CoupleTexte coupleTexte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annotateur_id", nullable = false)
    private Annotateur annotateur;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateAnnotation;
    private LocalDateTime updatedAt;
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.dateAnnotation = now;
        this.updatedAt = now; // optionnel : initialiser updatedAt aussi à la création
    }
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}