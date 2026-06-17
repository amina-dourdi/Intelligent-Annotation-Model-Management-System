package com.ensah.Core.services.Impl;

import com.ensah.Core.dao.IDatasetRepository;
import com.ensah.Core.dao.IEntrainementRepository;
import com.ensah.Core.dao.IUtilisateurRepository;
import com.ensah.Core.model.Administrateur;
import com.ensah.Core.model.Dataset;
import com.ensah.Core.model.EntrainementModele;
import com.ensah.Core.services.IMlTrainingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MlTrainingServiceImpl implements IMlTrainingService {

    private final IEntrainementRepository entrainementRepository;
    private final IDatasetRepository datasetRepository;
    private final IUtilisateurRepository utilisateurRepository;

    public MlTrainingServiceImpl(IEntrainementRepository entrainementRepository,
                                 IDatasetRepository datasetRepository,
                                 IUtilisateurRepository utilisateurRepository) {
        this.entrainementRepository = entrainementRepository;
        this.datasetRepository = datasetRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public EntrainementModele lancerEntrainement(Long datasetId, Long adminId, int epochs, double lr, int batchSize) {
        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new RuntimeException("Dataset introuvable"));
        Administrateur admin = (Administrateur) utilisateurRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin introuvable"));

        EntrainementModele entrainement = new EntrainementModele();
        entrainement.setDataset(dataset);
        entrainement.setAdministrateur(admin);
        entrainement.setHyperparametres(String.format("Epochs: %d, LR: %s, Batch: %d", epochs, lr, batchSize));
        entrainement.setStatut("EN_COURS");
        entrainement = entrainementRepository.save(entrainement);

        try {
            // Résolution du chemin du script Python
            String pythonScriptPath = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "python", "train.py").toString();
            // Création d'un dataset fictif path
            String datasetPath = dataset.getFichierNom() != null ? dataset.getFichierNom() : "dataset_" + datasetId + ".csv";

            ProcessBuilder pb = new ProcessBuilder(
                    "python", pythonScriptPath,
                    "--dataset_path", datasetPath,
                    "--epochs", String.valueOf(epochs),
                    "--lr", String.valueOf(lr),
                    "--batch_size", String.valueOf(batchSize)
            );
            
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder logs = new StringBuilder();
            Double accuracy = null;
            Double f1Score = null;
            String confMatrix = null;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logs.append(line).append("\n");
                    if (line.startsWith("RESULT_JSON=")) {
                        String jsonStr = line.substring("RESULT_JSON=".length());
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode node = mapper.readTree(jsonStr);
                        accuracy = node.has("accuracy") ? node.get("accuracy").asDouble() : null;
                        f1Score = node.has("f1_score") ? node.get("f1_score").asDouble() : null;
                        confMatrix = node.has("confusion_matrix") ? node.get("confusion_matrix").asText() : null;
                    }
                }
            }

            int exitCode = process.waitFor();
            
            entrainement.setLogsConsole(logs.toString());
            
            if (exitCode == 0 && accuracy != null) {
                entrainement.setStatut("TERMINE");
                entrainement.setAccuracy(accuracy);
                entrainement.setF1Score(f1Score);
                entrainement.setConfusionMatrix(confMatrix);
            } else {
                entrainement.setStatut("ERREUR");
            }

        } catch (Exception e) {
            entrainement.setStatut("ERREUR");
            entrainement.setLogsConsole("Erreur d'exécution Java : " + e.getMessage());
        } finally {
            entrainement.setDateFin(LocalDateTime.now());
            entrainementRepository.save(entrainement);
        }

        return entrainement;
    }

    @Override
    public List<EntrainementModele> getHistoriqueByDataset(Long datasetId) {
        return entrainementRepository.findByDatasetIdOrderByDateDebutDesc(datasetId);
    }
}
