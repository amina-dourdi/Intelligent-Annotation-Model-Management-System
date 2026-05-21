package com.ensah.Core.services;


import com.ensah.Core.model.Tache;
import java.time.LocalDate;
import java.util.List;

public interface IAffectationService {

    void affecterAnnotateurs(Long datasetId, List<Long> annotateurIds, LocalDate dateLimite);

    List<Tache> listerTachesParAnnotateur(Long annotateurId);

    List<Tache> listerTachesParDataset(Long datasetId);
}