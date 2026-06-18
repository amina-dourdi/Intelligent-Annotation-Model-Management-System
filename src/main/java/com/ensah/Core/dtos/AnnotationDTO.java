package com.ensah.Core.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnotationDTO {
    private Long id;
    private String classeChoisie;
    private LocalDateTime dateAnnotation;
    private Long annotateurId;
    private String annotateurNomComplet; // optionnel pour l'affichage
    private CoupleTexteDTO coupleTexte;
}
