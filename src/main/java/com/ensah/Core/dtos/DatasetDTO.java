package com.ensah.Core.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatasetDTO {
    private Long id;
    private String nomDataset;
    private java.time.LocalDateTime dateCreation;
    private String description;
    private String fichierNom;
    private List<String> classesPossibles;
    
    // Pour des besoins d'affichage (ex: compteurs)
    private int numberOfCouples;
}
