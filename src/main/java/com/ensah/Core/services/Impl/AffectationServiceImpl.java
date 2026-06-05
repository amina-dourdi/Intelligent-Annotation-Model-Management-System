package com.ensah.Core.services.Impl;

import com.ensah.Core.model.Annotateur;
import com.ensah.Core.model.CoupleTexte;
import com.ensah.Core.model.Dataset;
import com.ensah.Core.model.Tache;
import com.ensah.Core.dao.IAnnotateurRepository;
import com.ensah.Core.dao.ICoupleTexteRepository;
import com.ensah.Core.dao.IDatasetRepository;
import com.ensah.Core.dao.ITacheRepository;
import com.ensah.Core.services.IAffectationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class AffectationServiceImpl implements IAffectationService {

    private final IDatasetRepository datasetRepository;
    private final IAnnotateurRepository annotateurRepository;
    private final ITacheRepository tacheRepository;
    private final ICoupleTexteRepository coupleTexteRepository;

    public AffectationServiceImpl(IDatasetRepository datasetRepository,
                                  IAnnotateurRepository annotateurRepository,
                                  ITacheRepository tacheRepository,
                                  ICoupleTexteRepository coupleTexteRepository) {
        this.datasetRepository = datasetRepository;
        this.annotateurRepository = annotateurRepository;
        this.tacheRepository = tacheRepository;
        this.coupleTexteRepository = coupleTexteRepository;
    }

    @Override
    public void affecterAnnotateurs(Long datasetId, List<Long> annotateurIds, LocalDate dateLimite) {
        // ✅ Validation : minimum 3 annotateurs (car chaque couple doit en avoir 3)
        if (annotateurIds == null || annotateurIds.size() < 3) {
            throw new IllegalArgumentException("Il faut sélectionner au moins 3 annotateurs (actuellement : "
                    + (annotateurIds == null ? 0 : annotateurIds.size()) + ")");
        }

        Dataset ds = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new RuntimeException("Dataset introuvable"));

        List<CoupleTexte> tousLesCouples = coupleTexteRepository.findByDatasetId(datasetId);
        if (tousLesCouples.isEmpty()) {
            throw new IllegalArgumentException("Le dataset ne contient aucun couple à annoter");
        }

        // Récupérer ou créer les tâches pour tous les annotateurs sélectionnés
        java.util.Map<Long, Tache> tachesParAnnotateur = new java.util.HashMap<>();
        for (Long aid : annotateurIds) {
            Annotateur a = annotateurRepository.findById(aid)
                    .orElseThrow(() -> new RuntimeException("Annotateur introuvable : " + aid));

            Tache tache = tacheRepository.findByDatasetIdAndAnnotateurId(datasetId, aid)
                    .orElseGet(() -> {
                        Tache nouvelleTache = new Tache();
                        nouvelleTache.setDataset(ds);
                        nouvelleTache.setAnnotateur(a);
                        nouvelleTache.setDateLimite(dateLimite);
                        return tacheRepository.save(nouvelleTache);
                    });

            tache.setDateLimite(dateLimite);
            tachesParAnnotateur.put(aid, tache);
        }

        // 🔀 Pour chaque couple : assigner aléatoirement à exactement 3 annotateurs
        java.util.Random random = new java.util.Random();

        for (CoupleTexte ct : tousLesCouples) {
            // Qui a déjà ce couple parmi les annotateurs sélectionnés ?
            java.util.List<Long> idsAyantDejaCeCouple = new java.util.ArrayList<>();
            for (Long aid : annotateurIds) {
                if (tachesParAnnotateur.get(aid).getCouples().contains(ct)) {
                    idsAyantDejaCeCouple.add(aid);
                }
            }

            // Combien manque-t-il pour atteindre 3 ?
            int nbManquant = 3 - idsAyantDejaCeCouple.size();
            if (nbManquant > 0) {
                // Annotateurs qui n'ont PAS encore ce couple
                java.util.List<Long> idsDisponibles = new java.util.ArrayList<>(annotateurIds);
                idsDisponibles.removeAll(idsAyantDejaCeCouple);

                // Mélanger et prendre ce qu'il manque
                java.util.Collections.shuffle(idsDisponibles, random);
                java.util.List<Long> idsAAjouter = idsDisponibles.subList(0, nbManquant);

                for (Long aid : idsAAjouter) {
                    tachesParAnnotateur.get(aid).ajouterCouple(ct);
                }
            }
        }

        // Sauvegarder toutes les tâches modifiées
        for (Tache t : tachesParAnnotateur.values()) {
            tacheRepository.save(t);
        }
    }

    @Override
    public List<Tache> listerTachesParAnnotateur(Long annotateurId) {
        return tacheRepository.findByAnnotateurId(annotateurId);
    }

    @Override
    public List<Tache> listerTachesParDataset(Long datasetId) {
        return tacheRepository.findByDatasetId(datasetId);
    }
    @Override
    public void reequilibrerApresDesaffectation(Long datasetId, Long annotateurRetireId) {
        Dataset ds = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new RuntimeException("Dataset introuvable"));

        List<CoupleTexte> tousLesCouples = coupleTexteRepository.findByDatasetId(datasetId);
        List<Tache> tachesRestantes = tacheRepository.findByDatasetId(datasetId);

        // IDs des annotateurs encore actifs sur ce dataset
        List<Long> idsAnnotateursActifs = tachesRestantes.stream()
                .map(t -> t.getAnnotateur().getId())
                .distinct()
                .filter(id -> !id.equals(annotateurRetireId))
                .toList();

        if (idsAnnotateursActifs.size() < 3) {
            throw new IllegalStateException(
                    "Rééquilibrage partiel : il ne reste que " + idsAnnotateursActifs.size()
                            + " annotateur(s) (impossible d'atteindre 3 par couple)");
        }

        java.util.Random random = new java.util.Random();

        for (CoupleTexte ct : tousLesCouples) {
            // Qui a encore ce couple parmi les tâches restantes ?
            List<Long> idsAyantCeCouple = tachesRestantes.stream()
                    .filter(t -> t.getCouples().contains(ct))
                    .map(t -> t.getAnnotateur().getId())
                    .distinct()
                    .toList();

            int nbManquant = 3 - idsAyantCeCouple.size();
            if (nbManquant > 0) {
                // Annotateurs actifs qui n'ont PAS ce couple
                List<Long> idsDisponibles = new java.util.ArrayList<>(idsAnnotateursActifs);
                idsDisponibles.removeAll(idsAyantCeCouple);
                java.util.Collections.shuffle(idsDisponibles, random);

                // Si pas assez d'annotateurs disponibles, on prend ce qu'on peut
                int nbAAjouter = Math.min(nbManquant, idsDisponibles.size());

                for (int i = 0; i < nbAAjouter; i++) {
                    Long aid = idsDisponibles.get(i);
                    Tache tache = tachesRestantes.stream()
                            .filter(t -> t.getAnnotateur().getId().equals(aid))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Tâche introuvable"));

                    if (!tache.getCouples().contains(ct)) {
                        tache.ajouterCouple(ct);
                        tacheRepository.save(tache);
                    }
                }
            }
        }
    }
}