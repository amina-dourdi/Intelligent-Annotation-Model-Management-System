package com.ensah.Core.dao;


import com.ensah.Core.model.Tache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ITacheRepository extends JpaRepository<Tache, Long> {

    Optional<Tache> findByDatasetIdAndAnnotateurId(Long datasetId, Long annotateurId);
    List<Tache> findAllByDatasetIdAndAnnotateurId(Long datasetId, Long annotateurId);
    List<Tache> findByAnnotateurId(Long annotateurId);

    List<Tache> findByDatasetId(Long datasetId);
    
    List<Tache> findByDateLimite(java.time.LocalDate dateLimite);
}