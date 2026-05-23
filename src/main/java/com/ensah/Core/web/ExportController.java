package com.ensah.Core.web;


import com.ensah.Core.services.IExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/admin/export")
public class ExportController {

    private final IExportService exportService;

    public ExportController(IExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/dataset/{id}")
    public ResponseEntity<byte[]> exportDataset(@PathVariable Long id) throws IOException {
        byte[] csv = exportService.exportDatasetAnnotations(id);
        String filename = "dataset_" + id + "_annotations.csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(exportService.getExportContentType()))
                .body(csv);
    }
}
