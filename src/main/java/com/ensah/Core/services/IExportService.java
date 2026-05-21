package com.ensah.Core.services;


import java.io.IOException;

public interface IExportService {

    byte[] exportDatasetAnnotations(Long datasetId) throws IOException;

    String getExportContentType();
}