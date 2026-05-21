package com.ensah.Core.dao;


import com.ensah.Core.model.CoupleTexte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICoupleTexteRepository extends JpaRepository<CoupleTexte, Long> {

    List<CoupleTexte> findByDatasetId(Long datasetId);

    long countByDatasetId(Long datasetId);
}