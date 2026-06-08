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

    @Query("SELECT COUNT(a) FROM Annotation a WHERE a.coupleTexte.dataset.id = :datasetId")
    long countByCoupleTexteDatasetId(@Param("datasetId") Long datasetId);


    @Query("SELECT a.classeChoisie, COUNT(a) FROM Annotation a WHERE a.annotateur.id = :annotateurId GROUP BY a.classeChoisie")
    List<Object[]> countAnnotationsByClassForAnnotateur(@Param("annotateurId") Long annotateurId);

    @Query("SELECT COUNT(a) FROM Annotation a WHERE a.annotateur.id = :annotateurId AND a.coupleTexte.id IN (SELECT ct.id FROM Tache t JOIN t.couples ct WHERE t.id = :tacheId)")
    long countByTacheIdAndAnnotateurId(@Param("tacheId") Long tacheId, @Param("annotateurId") Long annotateurId);
}
