package com.ensah.Core.services;

import java.util.List;
import java.util.Map;

public interface ISpamDetectionService {

    List<Map<String, Object>> detecterSpammeurs(Long datasetId);

    /**
     * Détecte les spammeurs basés sur leur Kappa individuel par rapport aux autres
     */
    List<Map<String, Object>> detecterSpammeursParKappa(Long datasetId);
}