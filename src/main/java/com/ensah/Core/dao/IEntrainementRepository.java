package com.ensah.Core.dao;

import com.ensah.Core.model.EntrainementModele;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IEntrainementRepository extends JpaRepository<EntrainementModele, Long> {
    List<EntrainementModele> findByDatasetIdOrderByDateDebutDesc(Long datasetId);
}
