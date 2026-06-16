package com.ensah.Core.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoupleTexteDTO {
    private Long id;
    private String texte1;
    private String texte2;
    // Peut contenir une liste d'annotations simplifiées si nécessaire, ou juste l'ID du dataset.
    private Long datasetId;
}
