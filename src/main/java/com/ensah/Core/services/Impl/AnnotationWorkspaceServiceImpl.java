package com.ensah.Core.services.Impl;

import com.ensah.Core.dao.IAnnotationRepository;
import com.ensah.Core.dao.ICoupleTexteRepository;
import com.ensah.Core.dao.ITacheRepository;
import com.ensah.Core.model.Annotation;
import com.ensah.Core.model.Annotateur;
import com.ensah.Core.model.CoupleTexte;
import com.ensah.Core.model.Tache;
import com.ensah.Core.services.IAffectationService;
import com.ensah.Core.services.IAnnotationWorkspaceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class AnnotationWorkspaceServiceImpl implements IAnnotationWorkspaceService {

    private final ITacheRepository tacheRepository;
    private final ICoupleTexteRepository coupleTexteRepository;
    private final IAnnotationRepository annotationRepository;
    private final IAffectationService affectationService;
    private final com.ensah.Core.mappers.EntityMapper entityMapper;

    public AnnotationWorkspaceServiceImpl(ITacheRepository tacheRepository,
                                          ICoupleTexteRepository coupleTexteRepository,
                                          IAnnotationRepository annotationRepository,
                                          IAffectationService affectationService,
                                          com.ensah.Core.mappers.EntityMapper entityMapper) {
        this.tacheRepository = tacheRepository;
        this.coupleTexteRepository = coupleTexteRepository;
        this.annotationRepository = annotationRepository;
        this.affectationService = affectationService;
        this.entityMapper = entityMapper;
    }

    @Override
    public List<Map<String, Object>> getTaskWrappers(Long annotateurId, String filter) {
        List<Tache> toutesLesTaches = affectationService.listerTachesParAnnotateur(annotateurId);
        List<Map<String, Object>> taskWrappers = new ArrayList<>();

        for (Tache t : toutesLesTaches) {
            int totalCouples = t.getCouples().size();
            long annotatedCouples = annotationRepository.countByTacheIdAndAnnotateurId(t.getId(), annotateurId);

            int percent = totalCouples == 0 ? 0 : (int) ((annotatedCouples * 100) / totalCouples);
            boolean isCompleted = percent == 100;

            Map<String, Object> wrapper = new HashMap<>();
            wrapper.put("tache", t);
            wrapper.put("percent", percent);
            wrapper.put("annotatedCount", annotatedCouples);
            wrapper.put("totalCount", totalCouples);
            wrapper.put("status", isCompleted ? "Terminé" : "À faire");

            if ("all".equals(filter) ||
                ("todo".equals(filter) && !isCompleted) ||
                ("done".equals(filter) && isCompleted)) {
                taskWrappers.add(wrapper);
            }
        }
        return taskWrappers;
    }

    @Override
    public Map<String, Integer> getDashboardStats(Long annotateurId, List<Tache> toutesLesTaches) {
        int totalAssigned = 0;
        int totalAnnotatedCount = 0;
        int completedTasksCount = 0;

        for (Tache t : toutesLesTaches) {
            int totalCouples = t.getCouples().size();
            totalAssigned += totalCouples;

            long annotatedCouples = annotationRepository.countByTacheIdAndAnnotateurId(t.getId(), annotateurId);
            totalAnnotatedCount += annotatedCouples;

            if (totalCouples > 0 && annotatedCouples == totalCouples) {
                completedTasksCount++;
            }
        }

        int globalProgress = totalAssigned == 0 ? 0 : (totalAnnotatedCount * 100) / totalAssigned;

        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalAssigned", totalAssigned);
        stats.put("totalAnnotatedCount", totalAnnotatedCount);
        stats.put("completedTasksCount", completedTasksCount);
        stats.put("globalProgress", globalProgress);
        return stats;
    }

    @Override
    public Map<String, Integer> getDashboardStatsDTO(Long annotateurId, List<com.ensah.Core.dtos.TacheDTO> toutesLesTaches) {
        int totalAssigned = 0;
        int totalAnnotatedCount = 0;
        int completedTasksCount = 0;

        for (com.ensah.Core.dtos.TacheDTO t : toutesLesTaches) {
            int totalCouples = t.getTotalCouples();
            totalAssigned += totalCouples;

            long annotatedCouples = annotationRepository.countByTacheIdAndAnnotateurId(t.getId(), annotateurId);
            totalAnnotatedCount += annotatedCouples;

            if (totalCouples > 0 && annotatedCouples == totalCouples) {
                completedTasksCount++;
            }
        }

        int globalProgress = totalAssigned == 0 ? 0 : (totalAnnotatedCount * 100) / totalAssigned;

        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalAssigned", totalAssigned);
        stats.put("totalAnnotatedCount", totalAnnotatedCount);
        stats.put("completedTasksCount", completedTasksCount);
        stats.put("globalProgress", globalProgress);
        return stats;
    }

    @Override
    public Tache getTacheForAnnotateur(Long tacheId, Long annotateurId) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche introuvable : " + tacheId));

        if (!tache.getAnnotateur().getId().equals(annotateurId)) {
            throw new RuntimeException("Accès refusé : Cette tâche ne vous est pas assignée.");
        }
        return tache;
    }

    @Override
    public com.ensah.Core.dtos.TacheDTO getTacheDTOForAnnotateur(Long tacheId, Long annotateurId) {
        return entityMapper.toDTO(getTacheForAnnotateur(tacheId, annotateurId));
    }

    @Override
    public Page<CoupleTexte> getCouplePage(Long tacheId, Pageable pageable) {
        return coupleTexteRepository.findCouplesByTacheId(tacheId, pageable);
    }

    @Override
    public Page<com.ensah.Core.dtos.CoupleTexteDTO> getCoupleDTOPage(Long tacheId, Pageable pageable) {
        return getCouplePage(tacheId, pageable).map(entityMapper::toDTO);
    }

    @Override
    public Optional<Annotation> getExistingAnnotation(Long coupleTexteId, Long annotateurId) {
        return annotationRepository.findByCoupleTexteIdAndAnnotateurId(coupleTexteId, annotateurId);
    }

    @Override
    public Optional<com.ensah.Core.dtos.AnnotationDTO> getExistingAnnotationDTO(Long coupleTexteId, Long annotateurId) {
        return getExistingAnnotation(coupleTexteId, annotateurId).map(entityMapper::toDTO);
    }

    @Override
    public long getAnnotatedCount(Long tacheId, Long annotateurId) {
        return annotationRepository.countByTacheIdAndAnnotateurId(tacheId, annotateurId);
    }

    @Override
    public int getProgressPercent(Long tacheId, Long annotateurId, int totalCouples) {
        long annotatedCouples = getAnnotatedCount(tacheId, annotateurId);
        return totalCouples == 0 ? 0 : (int) ((annotatedCouples * 100) / totalCouples);
    }

    @Override
    public void saveAnnotation(Long tacheId, Long annotateurId, Long coupleTexteId, String classeChoisie) {
        Tache tache = getTacheForAnnotateur(tacheId, annotateurId);

        CoupleTexte ct = coupleTexteRepository.findById(coupleTexteId)
                .orElseThrow(() -> new RuntimeException("CoupleTexte introuvable : " + coupleTexteId));

        Optional<Annotation> optAnn = annotationRepository.findByCoupleTexteIdAndAnnotateurId(coupleTexteId, annotateurId);
        Annotation annotation;
        if (optAnn.isPresent()) {
            annotation = optAnn.get();
            annotation.setClasseChoisie(classeChoisie);
        } else {
            annotation = new Annotation();
            annotation.setCoupleTexte(ct);
            
            // To set annotateur, we just need a reference.
            Annotateur annotateurRef = new Annotateur();
            annotateurRef.setId(annotateurId);
            annotation.setAnnotateur(annotateurRef);
            
            annotation.setClasseChoisie(classeChoisie);
        }
        annotationRepository.save(annotation);
    }
}
