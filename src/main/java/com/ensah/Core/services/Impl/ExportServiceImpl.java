package com.ensah.Core.services.Impl;



import com.ensah.Core.model.*;

import com.ensah.Core.dao.*;

import com.ensah.Core.services.IExportService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
public class ExportServiceImpl implements IExportService {

    private final IDatasetRepository datasetRepository;
    private final ICoupleTexteRepository coupleTexteRepository;
    private final IAnnotationRepository annotationRepository;

    public ExportServiceImpl(IDatasetRepository datasetRepository,
                             ICoupleTexteRepository coupleTexteRepository,
                             IAnnotationRepository annotationRepository) {
        this.datasetRepository = datasetRepository;
        this.coupleTexteRepository = coupleTexteRepository;
        this.annotationRepository = annotationRepository;
    }

    @Override
    public byte[] exportDatasetAnnotations(Long datasetId) throws IOException {
        Dataset ds = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new RuntimeException("Dataset introuvable"));

        List<CoupleTexte> couples = coupleTexteRepository.findByDatasetId(datasetId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            pw.println("id,text1,text2,classe,annotateur,date_annotation");
            for (CoupleTexte ct : couples) {
                List<Annotation> anns = annotationRepository.findByCoupleTexteId(ct.getId());
                if (anns.isEmpty()) {
                    pw.printf("%d,\"%s\",\"%s\",,,\n",
                            ct.getId(), escape(ct.getText1()), escape(ct.getText2()));
                } else {
                    for (Annotation a : anns) {
                        pw.printf("%d,\"%s\",\"%s\",\"%s\",\"%s %s\",\"%s\"\n",
                                ct.getId(), escape(ct.getText1()), escape(ct.getText2()),
                                escape(a.getClasseChoisie()),
                                escape(a.getAnnotateur().getNom()),
                                escape(a.getAnnotateur().getPrenom()),
                                a.getDateAnnotation() != null ? a.getDateAnnotation().toString() : "");
                    }
                }
            }
        }
        return baos.toByteArray();
    }

    @Override
    public String getExportContentType() {
        return "text/csv; charset=UTF-8";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\"\"").replace("\n", " ");
    }
}
