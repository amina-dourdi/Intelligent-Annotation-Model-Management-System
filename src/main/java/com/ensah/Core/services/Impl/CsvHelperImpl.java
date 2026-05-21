package com.ensah.Core.services.Impl;



import com.ensah.Core.model.CoupleTexte;
import com.ensah.Core.services.ICsvHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvHelperImpl implements ICsvHelper {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<CoupleTexte> parseFichier(MultipartFile fichier) throws IOException {
        String contentType = fichier.getContentType();
        if (contentType == null) contentType = "";

        if (contentType.contains("json")) {
            return parseJson(fichier);
        } else {
            return parseCsv(fichier);
        }
    }

    private List<CoupleTexte> parseCsv(MultipartFile fichier) throws IOException {
        List<CoupleTexte> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(fichier.getInputStream()))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                String[] parts = line.split(",", -1);
                if (parts.length >= 2) {
                    CoupleTexte ct = new CoupleTexte();
                    ct.setText1(parts[0].trim());
                    ct.setText2(parts[1].trim());
                    result.add(ct);
                }
            }
        }
        return result;
    }

    private List<CoupleTexte> parseJson(MultipartFile fichier) throws IOException {
        List<CoupleTexte> result = new ArrayList<>();
        JsonNode root = objectMapper.readTree(fichier.getInputStream());
        if (root.isArray()) {
            for (JsonNode node : root) {
                CoupleTexte ct = new CoupleTexte();
                ct.setText1(node.has("text1") ? node.get("text1").asText() : "");
                ct.setText2(node.has("text2") ? node.get("text2").asText() : "");
                result.add(ct);
            }
        }
        return result;
    }

    @Override
    public boolean supports(String contentType) {
        return contentType != null && (
                contentType.contains("csv") || contentType.contains("json") || contentType.contains("text/plain")
        );
    }
}