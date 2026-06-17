package com.ensah.Core.services;

import com.ensah.Core.model.EntrainementModele;
import java.util.List;

public interface IMlTrainingService {
    EntrainementModele lancerEntrainement(Long datasetId, Long adminId, int epochs, double lr, int batchSize);
    List<EntrainementModele> getHistoriqueByDataset(Long datasetId);
}
