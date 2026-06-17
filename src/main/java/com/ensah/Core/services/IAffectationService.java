package com.ensah.Core.services;

import com.ensah.Core.model.Tache;
import com.ensah.Core.dtos.TacheDTO;
import java.time.LocalDate;
import java.util.List;

public interface IAffectationService {

    void affecterAnnotateurs(Long datasetId, List<Long> annotateurIds, LocalDate dateLimite);

    List<TacheDTO> listerTachesDTOParAnnotateur(Long annotateurId);

    List<Tache> listerTachesParAnnotateur(Long annotateurId);

    List<TacheDTO> listerTachesDTOParDataset(Long datasetId);

    List<Tache> listerTachesParDataset(Long datasetId);

    void reequilibrerApresDesaffectation(Long datasetId, Long annotateurRetireId);
}