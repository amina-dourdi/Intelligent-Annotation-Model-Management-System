package com.ensah.Core.services;

import com.ensah.Core.model.Dataset;
import com.ensah.Core.dtos.DatasetDTO;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface IDatasetService {

    DatasetDTO creerDataset(String nom, String description, List<String> classes, MultipartFile fichier) throws IOException;

    List<DatasetDTO> listerDatasets();

    DatasetDTO getDatasetDTOById(Long id);
    
    Dataset getDatasetEntityById(Long id);

    int calculerPourcentageAvancement(Long datasetId);

    void supprimerDataset(Long id);
}