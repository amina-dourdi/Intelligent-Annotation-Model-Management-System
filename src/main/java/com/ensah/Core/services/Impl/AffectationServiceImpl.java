package com.ensah.Core.services.Impl;



import com.ensah.Core.model.*;

import com.ensah.Core.dao.*;
import com.ensah.Core.services.IAffectationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class AffectationServiceImpl implements IAffectationService {

    private final IDatasetRepository datasetRepository;
    private final IAnnotateurRepository annotateurRepository;
    private final ITacheRepository tacheRepository;
    private final ICoupleTexteRepository coupleTexteRepository;

    public AffectationServiceImpl(IDatasetRepository datasetRepository,
                                  IAnnotateurRepository annotateurRepository,
                                  ITacheRepository tacheRepository,
                                  ICoupleTexteRepository coupleTexteRepository) {
        this.datasetRepository = datasetRepository;
        this.annotateurRepository = annotateurRepository;
        this.tacheRepository = tacheRepository;
        this.coupleTexteRepository = coupleTexteRepository;
    }

    @Override
    public void affecterAnnotateurs(Long datasetId, List<Long> annotateurIds, LocalDate dateLimite) {
        Dataset ds = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new RuntimeException("Dataset introuvable"));

        List<CoupleTexte> tousLesCouples = coupleTexteRepository.findByDatasetId(datasetId);

        for (Long aid : annotateurIds) {
            Annotateur a = annotateurRepository.findById(aid)
                    .orElseThrow(() -> new RuntimeException("Annotateur introuvable : " + aid));

            Tache tache;
            var existing = tacheRepository.findByDatasetIdAndAnnotateurId(datasetId, aid);
            if (existing.isPresent()) {
                tache = existing.get();
            } else {
                tache = new Tache();
                tache.setDataset(ds);
                tache.setAnnotateur(a);
                tache.setDateLimite(dateLimite);
                tacheRepository.save(tache);
            }

            for (CoupleTexte ct : tousLesCouples) {
                if (!tache.getCouples().contains(ct)) {
                    tache.ajouterCouple(ct);
                }
            }
        }
    }

    @Override
    public List<Tache> listerTachesParAnnotateur(Long annotateurId) {
        return tacheRepository.findByAnnotateurId(annotateurId);
    }

    @Override
    public List<Tache> listerTachesParDataset(Long datasetId) {
        return tacheRepository.findByDatasetId(datasetId);
    }
}