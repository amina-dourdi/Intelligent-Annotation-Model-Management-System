package com.ensah.Core.dao;

import com.ensah.Core.model.Annotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IAnnotationRepository extends JpaRepository<Annotation, Long> {

    List<Annotation> findByCoupleTexteId(Long coupleTexteId);

    List<Annotation> findByAnnotateurId(Long annotateurId);

    Optional<Annotation> findByCoupleTexteIdAndAnnotateurId(Long coupleTexteId, Long annotateurId);

    long countByCoupleTexteDatasetId(Long datasetId);

    @Query("SELECT a.classeChoisie, COUNT(a) FROM Annotation a WHERE a.annotateur.id = :annotateurId GROUP BY a.classeChoisie")
    List<Object[]> countAnnotationsByClassForAnnotateur(@Param("annotateurId") Long annotateurId);
}
