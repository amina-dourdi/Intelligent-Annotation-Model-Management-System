package com.ensah.Core.dao;


import com.ensah.Core.model.CoupleTexte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICoupleTexteRepository extends JpaRepository<CoupleTexte, Long> {

    List<CoupleTexte> findByDatasetId(Long datasetId);

    long countByDatasetId(Long datasetId);

    @Query("SELECT ct FROM CoupleTexte ct JOIN ct.taches t WHERE t.id = :tacheId ORDER BY ct.id ASC")
    Page<CoupleTexte> findCouplesByTacheId(@Param("tacheId") Long tacheId, Pageable pageable);
}