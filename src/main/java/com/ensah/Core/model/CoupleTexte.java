package com.ensah.Core.model;


import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class CoupleTexte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String texte1;

    @Column(nullable = false, length = 2000)
    private String texte2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @OneToMany(mappedBy = "coupleTexte", cascade = CascadeType.ALL)
    private List<Annotation> annotations = new ArrayList<>();

    @ManyToMany(mappedBy = "couples")
    private List<Tache> taches = new ArrayList<>();

}
