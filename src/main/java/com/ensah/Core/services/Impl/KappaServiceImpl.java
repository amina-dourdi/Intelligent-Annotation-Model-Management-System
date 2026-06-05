package com.ensah.Core.services.Impl;

import com.ensah.Core.dao.IAnnotationRepository;
import com.ensah.Core.dao.ICoupleTexteRepository;
import com.ensah.Core.model.Annotation;
import com.ensah.Core.model.CoupleTexte;
import com.ensah.Core.services.IKappaService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KappaServiceImpl implements IKappaService {

    private final ICoupleTexteRepository coupleTexteRepository;
    private final IAnnotationRepository annotationRepository;

    public KappaServiceImpl(ICoupleTexteRepository coupleTexteRepository,
                            IAnnotationRepository annotationRepository) {
        this.coupleTexteRepository = coupleTexteRepository;
        this.annotationRepository = annotationRepository;
    }

    @Override
    public KappaResult calculerFleissKappa(Long datasetId) {
        List<CoupleTexte> couples = coupleTexteRepository.findByDatasetId(datasetId);

        if (couples.isEmpty()) {
            return new KappaResult(0.0, "Aucune donnée", 0, 0);
        }

        // Récupérer toutes les annotations du dataset
        List<Annotation> allAnnotations = new ArrayList<>();
        for (CoupleTexte ct : couples) {
            allAnnotations.addAll(annotationRepository.findByCoupleTexteId(ct.getId()));
        }

        if (allAnnotations.isEmpty()) {
            return new KappaResult(0.0, "Aucune annotation", 0, couples.size());
        }

        // IDs uniques des annotateurs
        Set<Long> annotateurIds = allAnnotations.stream()
                .map(a -> a.getAnnotateur().getId())
                .collect(Collectors.toSet());

        int N = couples.size();           // Nombre de sujets (couples)
        int n = annotateurIds.size();      // Nombre d'annotateurs
        int k = allAnnotations.stream()     // Nombre de catégories (classes)
                .map(Annotation::getClasseChoisie)
                .distinct()
                .toList().size();

        if (n < 2) {
            return new KappaResult(0.0, "Minimum 2 annotateurs requis", n, N);
        }

        // Index : coupleId → liste des annotations
        Map<Long, List<Annotation>> annParCouple = new HashMap<>();
        for (CoupleTexte ct : couples) {
            annParCouple.put(ct.getId(), annotationRepository.findByCoupleTexteId(ct.getId()));
        }

        // ============================================
        // ÉTAPE 1 : Calculer Pj (proportion de chaque catégorie j)
        // ============================================
        Map<String, Double> pj = new HashMap<>();
        for (String classe : allAnnotations.stream().map(Annotation::getClasseChoisie).distinct().toList()) {
            long count = allAnnotations.stream()
                    .filter(a -> a.getClasseChoisie().equals(classe))
                    .count();
            pj.put(classe, (double) count / (N * n));
        }

        // ============================================
        // ÉTAPE 2 : Calculer Pe (accord par hasard)
        // ============================================
        double Pe = pj.values().stream()
                .mapToDouble(p -> p * p)
                .sum();

        // ============================================
        // ÉTAPE 3 : Calculer Pi (accord pour chaque sujet i)
        // ============================================
        double sumPi = 0.0;
        int sujetsValides = 0;

        for (CoupleTexte ct : couples) {
            List<Annotation> annCouple = annParCouple.getOrDefault(ct.getId(), Collections.emptyList());

            // Ne compter que les couples avec au moins 2 annotations
            if (annCouple.size() >= 2) {
                // Compter les annotations par classe pour ce couple
                Map<String, Long> comptage = annCouple.stream()
                        .collect(Collectors.groupingBy(Annotation::getClasseChoisie, Collectors.counting()));

                // Pi = (1 / (n*(n-1))) * Σ(nij² - nij) où nij = nombre d'annotateurs choisissant classe j pour sujet i
                double nijCarreSum = comptage.values().stream()
                        .mapToLong(nij -> nij * nij)
                        .sum();

                double ni = annCouple.size(); // nombre d'annotateurs pour ce couple
                double Pi = (nijCarreSum - ni) / (ni * (ni - 1));

                sumPi += Pi;
                sujetsValides++;
            }
        }

        if (sujetsValides == 0) {
            return new KappaResult(0.0, "Pas assez de données comparables", n, N);
        }

        // ============================================
        // ÉTAPE 4 : Calculer Po (accord observé moyen)
        // ============================================
        double Po = sumPi / sujetsValides;

        // ============================================
        // ÉTAPE 5 : Calculer Kappa
        // ============================================
        double kappa;
        if (Pe == 1.0) {
            kappa = 1.0; // Tout le monde est d'accord sur tout
        } else {
            kappa = (Po - Pe) / (1 - Pe);
        }

        String interpretation = interpreterKappa(kappa);

        return new KappaResult(
                Math.round(kappa * 1000.0) / 1000.0, // arrondi à 3 décimales
                interpretation,
                n,
                N
        );
    }

    private String interpreterKappa(double kappa) {
        if (kappa < 0) return "Pas d'accord";
        if (kappa <= 0.20) return "Accord léger";
        if (kappa <= 0.40) return "Accord modéré";
        if (kappa <= 0.60) return "Accord moyen";
        if (kappa <= 0.80) return "Accord substantiel";
        return "Accord presque parfait";
    }
}