package com.ensah.Core.services;

import com.ensah.Core.model.Annotation;
import com.ensah.Core.model.CoupleTexte;
import com.ensah.Core.model.Tache;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IAnnotationWorkspaceService {
    
    List<Map<String, Object>> getTaskWrappers(Long annotateurId, String filter);
    
    Map<String, Integer> getDashboardStats(Long annotateurId, List<Tache> toutesLesTaches);
    
    Map<String, Integer> getDashboardStatsDTO(Long annotateurId, List<com.ensah.Core.dtos.TacheDTO> toutesLesTaches);
    
    Tache getTacheForAnnotateur(Long tacheId, Long annotateurId);
    
    com.ensah.Core.dtos.TacheDTO getTacheDTOForAnnotateur(Long tacheId, Long annotateurId);
    
    Page<CoupleTexte> getCouplePage(Long tacheId, Pageable pageable);
    
    Page<com.ensah.Core.dtos.CoupleTexteDTO> getCoupleDTOPage(Long tacheId, Pageable pageable);
    
    Optional<Annotation> getExistingAnnotation(Long coupleTexteId, Long annotateurId);
    
    Optional<com.ensah.Core.dtos.AnnotationDTO> getExistingAnnotationDTO(Long coupleTexteId, Long annotateurId);
    
    int getProgressPercent(Long tacheId, Long annotateurId, int totalCouples);
    
    long getAnnotatedCount(Long tacheId, Long annotateurId);
    
    void saveAnnotation(Long tacheId, Long annotateurId, Long coupleTexteId, String classeChoisie);
}
