package com.ensah.Core.services;

public interface IMachineLearningService {
    
    /**
     * Exécute un script Python externe pour prédire la classe d'un couple de textes.
     * 
     * @param text1 Le premier texte
     * @param text2 Le deuxième texte
     * @param classes Les classes possibles séparées par des virgules (ex: "Entailment,Neutral,Contradiction")
     * @return La classe prédite par le modèle Python
     */
    String predictClass(String text1, String text2, String classes);
}
