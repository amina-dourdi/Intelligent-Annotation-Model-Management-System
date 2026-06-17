package com.ensah.Core.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TacheDTO {
    private Long id;
    private LocalDate dateLimite;
    private DatasetDTO dataset;
    private AnnotateurDTO annotateur;
    private int totalCouples;
}
