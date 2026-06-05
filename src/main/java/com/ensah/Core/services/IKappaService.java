package com.ensah.Core.services;

public interface IKappaService {

    /**
     * Calcule le Fleiss' Kappa pour un dataset
     * @return valeur entre -1 et 1, avec interprétation textuelle
     */
    KappaResult calculerFleissKappa(Long datasetId);

    record KappaResult(double kappa, String interpretation, int nbAnnotateurs, int nbCouples) {}
}