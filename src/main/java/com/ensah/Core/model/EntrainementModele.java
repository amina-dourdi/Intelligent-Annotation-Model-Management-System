package com.ensah.Core.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class EntrainementModele {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrateur_id", nullable = false)
    private Administrateur administrateur;

    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    // Hyperparamètres sous forme de texte JSON ou clé=valeur
    private String hyperparametres;

    // Statut : EN_COURS, TERMINE, ERREUR
    private String statut;

    // Métriques obtenues après le test
    private Double accuracy;
    private Double f1Score;
    
    @Column(length = 1000)
    private String confusionMatrix;

    @Column(columnDefinition = "TEXT")
    private String logsConsole;

    @PrePersist
    protected void onCreate() {
        this.dateDebut = LocalDateTime.now();
        if (this.statut == null) {
            this.statut = "EN_COURS";
        }
    }
}
