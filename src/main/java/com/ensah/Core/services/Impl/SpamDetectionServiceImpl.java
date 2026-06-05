package com.ensah.Core.services.Impl;

import com.ensah.Core.dao.IAnnotationRepository;
import com.ensah.Core.dao.ICoupleTexteRepository;
import com.ensah.Core.dao.ITacheRepository;
import com.ensah.Core.model.Annotation;
import com.ensah.Core.model.CoupleTexte;
import com.ensah.Core.model.Tache;
import com.ensah.Core.services.ISpamDetectionService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SpamDetectionServiceImpl implements ISpamDetectionService {

    private final ITacheRepository tacheRepository;
    private final IAnnotationRepository annotationRepository;
    private final ICoupleTexteRepository coupleTexteRepository;

    // Seuils selon le CDC : spammeur = annotateur aléatoire
    // Kappa < 0 = pire que le hasard = spammeur évident
    // Kappa < 0.20 = accord léger = suspect
    private static final double SEUIL_SPAMMEUR = 0.0;  // Kappa < 0 = spammeur
    private static final double SEUIL_SUSPECT = 0.20;  // Kappa < 0.20 = suspect

    public SpamDetectionServiceImpl(ITacheRepository tacheRepository,
                                    IAnnotationRepository annotationRepository,
                                    ICoupleTexteRepository coupleTexteRepository) {
        this.tacheRepository = tacheRepository;
        this.annotationRepository = annotationRepository;
        this.coupleTexteRepository = coupleTexteRepository;
    }

    @Override
    public List<Map<String, Object>> detecterSpammeurs(Long datasetId) {
        // Garde l'ancienne méthode pour compatibilité
        return detecterSpammeursParKappa(datasetId);
    }

    @Override
    public List<Map<String, Object>> detecterSpammeursParKappa(Long datasetId) {
        List<Tache> taches = tacheRepository.findByDatasetId(datasetId);
        List<CoupleTexte> couples = coupleTexteRepository.findByDatasetId(datasetId);
        List<Map<String, Object>> resultats = new ArrayList<>();

        // Index : coupleId → annotations
        Map<Long, List<Annotation>> annotationsParCouple = new HashMap<>();
        for (CoupleTexte ct : couples) {
            annotationsParCouple.put(ct.getId(), annotationRepository.findByCoupleTexteId(ct.getId()));
        }

        for (Tache tache : taches) {
            Long annotateurId = tache.getAnnotateur().getId();
            String nom = tache.getAnnotateur().getNom() + " " + tache.getAnnotateur().getPrenom();

            // Calculer le Kappa de cet annotateur vs les autres
            double kappaIndividuel = calculerKappaIndividuel(annotateurId, tache.getCouples(), annotationsParCouple);

            boolean estSpammeur = kappaIndividuel < SEUIL_SPAMMEUR;
            boolean estSuspect = kappaIndividuel < SEUIL_SUSPECT && !estSpammeur;

            Map<String, Object> info = new LinkedHashMap<>();
            info.put("annotateurId", annotateurId);
            info.put("nom", nom);
            info.put("kappa", Math.round(kappaIndividuel * 1000.0) / 1000.0);
            info.put("estSpammeur", estSpammeur);
            info.put("estSuspect", estSuspect);
            info.put("statut", estSpammeur ? "SPAMMEUR" : (estSuspect ? "SUSPECT" : "NORMAL"));
            info.put("raison", genererRaison(kappaIndividuel, estSpammeur, estSuspect));
            resultats.add(info);
        }

        // Trier : spammeurs d'abord, puis suspects, puis normaux
        resultats.sort((a, b) -> {
            double ka = (Double) a.get("kappa");
            double kb = (Double) b.get("kappa");
            return Double.compare(ka, kb);
        });

        return resultats;
    }

    /**
     * Calcule le Kappa entre un annotateur et la consensus des autres
     */
    private double calculerKappaIndividuel(Long annotateurId, List<CoupleTexte> couples,
                                           Map<Long, List<Annotation>> annotationsParCouple) {

        int accord = 0;      // annotateur = consensus
        int desaccord = 0;   // annotateur ≠ consensus
        int nonEvalue = 0;   // pas assez d'annotations pour comparer

        for (CoupleTexte ct : couples) {
            List<Annotation> annCouple = annotationsParCouple.getOrDefault(ct.getId(), Collections.emptyList());

            // Mon annotation sur ce couple
            Optional<Annotation> maAnn = annCouple.stream()
                    .filter(a -> a.getAnnotateur().getId().equals(annotateurId))
                    .findFirst();

            // Annotations des autres
            List<Annotation> autresAnn = annCouple.stream()
                    .filter(a -> !a.getAnnotateur().getId().equals(annotateurId))
                    .toList();

            if (maAnn.isPresent() && !autresAnn.isEmpty()) {
                String maClasse = maAnn.get().getClasseChoisie();
                String consensus = calculerConsensus(autresAnn);

                if (maClasse.equals(consensus)) {
                    accord++;
                } else {
                    desaccord++;
                }
            } else {
                nonEvalue++;
            }
        }

        int totalEvalue = accord + desaccord;
        if (totalEvalue == 0) return 0.0;

        // Kappa simplifié : (accord_observé - accord_hasard) / (1 - accord_hasard)
        double Po = (double) accord / totalEvalue;

        // Probabilité de hasard = proportion des classes chez les autres
        double Pe = 0.0; // Simplifié : on utilise l'approche majoritaire

        // Version simplifiée : Kappa approximé par (2*accord - total) / total
        // C'est une heuristique qui donne -1 (toujours en désaccord) à +1 (toujours d'accord)
        double kappaSimplifie = (2.0 * accord - totalEvalue) / totalEvalue;

        return kappaSimplifie;
    }

    private String calculerConsensus(List<Annotation> annotations) {
        Map<String, Long> comptage = annotations.stream()
                .collect(Collectors.groupingBy(Annotation::getClasseChoisie, Collectors.counting()));
        return comptage.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
    }

    private String genererRaison(double kappa, boolean estSpammeur, boolean estSuspect) {
        if (estSpammeur) {
            return "Kappa = " + Math.round(kappa * 1000.0) / 1000.0 + " : accord pire que le hasard (annotateur aléatoire)";
        }
        if (estSuspect) {
            return "Kappa = " + Math.round(kappa * 1000.0) / 1000.0 + " : accord très faible, comportement suspect";
        }
        if (kappa < 0.40) {
            return "Kappa = " + Math.round(kappa * 1000.0) / 1000.0 + " : accord modéré";
        }
        if (kappa < 0.60) {
            return "Kappa = " + Math.round(kappa * 1000.0) / 1000.0 + " : accord moyen";
        }
        return "Kappa = " + Math.round(kappa * 1000.0) / 1000.0 + " : bon accord avec les autres";
    }
}