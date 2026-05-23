package com.ensah.Core.services.Impl;


import com.ensah.Core.dao.*;
import com.ensah.Core.model.ClassePossible;


import com.ensah.Core.model.CoupleTexte;
import com.ensah.Core.model.Dataset;
import com.ensah.Core.services.ICsvHelper;
import com.ensah.Core.services.IDatasetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class DatasetServiceImpl implements IDatasetService {

    private final IDatasetRepository datasetRepository;
    private final ICoupleTexteRepository coupleTexteRepository;
    private final IClassePossibleRepository classePossibleRepository;
    private final IAnnotationRepository annotationRepository;
    private final ICsvHelper csvHelper;

    public DatasetServiceImpl(IDatasetRepository datasetRepository,
                              ICoupleTexteRepository coupleTexteRepository,
                              IClassePossibleRepository classePossibleRepository,
                              IAnnotationRepository annotationRepository,
                              ICsvHelper csvHelper) {
        this.datasetRepository = datasetRepository;
        this.coupleTexteRepository = coupleTexteRepository;
        this.classePossibleRepository = classePossibleRepository;
        this.annotationRepository = annotationRepository;
        this.csvHelper = csvHelper;
    }

    @Override
    public Dataset creerDataset(String nom, String description, List<String> classes, MultipartFile fichier) throws IOException {
        Dataset ds = new Dataset();
        ds.setNomDataset(nom);
        ds.setDescription(description);
        ds.setFichierNom(fichier.getOriginalFilename());
        datasetRepository.save(ds);

        for (String c : classes) {
            if (c != null && !c.isBlank()) {
                ClassePossible cp = new ClassePossible();
                cp.setTexteClasse(c.trim());
                cp.setDataset(ds);
                classePossibleRepository.save(cp);
            }
        }

        List<CoupleTexte> couples = csvHelper.parseFichier(fichier);
        for (CoupleTexte ct : couples) {
            ct.setDataset(ds);
            coupleTexteRepository.save(ct);
        }

        return ds;
    }

    @Override
    public List<Dataset> listerDatasets() {
        return datasetRepository.findAll();
    }

    @Override
    public Dataset getDatasetById(Long id) {
        return datasetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dataset introuvable"));
    }

    @Override
    public int calculerPourcentageAvancement(Long datasetId) {
        long total = coupleTexteRepository.countByDatasetId(datasetId);
        if (total == 0) return 0;
        long annotes = annotationRepository.countByCoupleTexteDatasetId(datasetId);
        return (int) ((annotes * 100) / total);
    }

    @Override
    public void supprimerDataset(Long id) {
        datasetRepository.deleteById(id);
    }
}
