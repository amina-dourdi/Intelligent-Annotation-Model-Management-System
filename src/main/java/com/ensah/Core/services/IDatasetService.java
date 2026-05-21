package com.ensah.Core.services;


import com.ensah.Core.model.Dataset;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface IDatasetService {

    Dataset creerDataset(String nom, String description, List<String> classes, MultipartFile fichier) throws IOException;

    List<Dataset> listerDatasets();

    Dataset getDatasetById(Long id);

    int calculerPourcentageAvancement(Long datasetId);

    void supprimerDataset(Long id);
}