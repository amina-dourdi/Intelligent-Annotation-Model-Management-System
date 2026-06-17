package com.ensah.Core.services.Impl;

import com.ensah.Core.services.IMachineLearningService;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;

@Service
public class MachineLearningServiceImpl implements IMachineLearningService {

    @Override
    public String predictClass(String text1, String text2, String classes) {
        try {
            // Chemin vers le script python
            String scriptPath = Paths.get("src", "main", "resources", "python", "predict.py").toAbsolutePath().toString();
            
            // Construire la commande
            ProcessBuilder pb = new ProcessBuilder("python", scriptPath, text1, text2, classes);
            
            // Pour des raisons de sécurité ou d'environnement sur Windows
            pb.redirectErrorStream(true);
            
            // Démarrer le processus Python
            Process process = pb.start();
            
            // Lire la sortie de la commande
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0 && output.length() > 0) {
                // Retourner la prédiction (nettoyée des espaces blancs ou retours à la ligne)
                return output.toString().trim();
            } else {
                throw new RuntimeException("Erreur lors de l'exécution du script Python. Code: " + exitCode + ", Sortie: " + output.toString());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return "ERREUR_PREDICTION";
        }
    }
}
