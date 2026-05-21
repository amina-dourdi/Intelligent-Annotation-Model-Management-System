package com.ensah.Core.services;


import com.ensah.Core.model.CoupleTexte;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

    public interface ICsvHelper {

        List<CoupleTexte> parseFichier(MultipartFile fichier) throws IOException;

        boolean supports(String contentType);
    }

