package com.ensah.Core.mappers;

import com.ensah.Core.dtos.*;
import com.ensah.Core.model.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class EntityMapper {

    public UtilisateurDTO toDTO(Utilisateur entity) {
        if (entity == null) return null;
        return new UtilisateurDTO(
                entity.getId(),
                entity.getNom(),
                entity.getPrenom(),
                entity.getLogin(),
                entity.getEmail(),
                entity.getRole() != null ? entity.getRole().getNomRole() : null
        );
    }

    public AnnotateurDTO toDTO(Annotateur entity) {
        if (entity == null) return null;
        AnnotateurDTO dto = new AnnotateurDTO();
        dto.setId(entity.getId());
        dto.setNom(entity.getNom());
        dto.setPrenom(entity.getPrenom());
        dto.setLogin(entity.getLogin());
        dto.setEmail(entity.getEmail());
        dto.setType(entity.getRole() != null ? entity.getRole().getNomRole() : null);
        
        if (entity.getTaches() != null) {
            dto.setNumberOfTaches(entity.getTaches().size());
        }
        
        return dto;
    }

    public DatasetDTO toDTO(Dataset entity) {
        if (entity == null) return null;
        DatasetDTO dto = new DatasetDTO();
        dto.setId(entity.getId());
        dto.setNomDataset(entity.getNomDataset());
        dto.setDateCreation(entity.getDateCreation());
        dto.setDescription(entity.getDescription());
        dto.setFichierNom(entity.getFichierNom());
        
        if (entity.getClassesPossibles() != null) {
            dto.setClassesPossibles(entity.getClassesPossibles().stream()
                    .map(ClassePossible::getTexteClasse)
                    .collect(Collectors.toList()));
        }
        
        if (entity.getCouples() != null) {
            dto.setNumberOfCouples(entity.getCouples().size());
        }
        return dto;
    }

    public TacheDTO toDTO(Tache entity) {
        if (entity == null) return null;
        TacheDTO dto = new TacheDTO();
        dto.setId(entity.getId());
        dto.setDateLimite(entity.getDateLimite());
        
        if (entity.getDataset() != null) {
            dto.setDataset(toDTO(entity.getDataset()));
        }
        
        if (entity.getAnnotateur() != null) {
            dto.setAnnotateur(toDTO(entity.getAnnotateur()));
        }
        
        if (entity.getCouples() != null) {
            dto.setTotalCouples(entity.getCouples().size());
        }
        
        return dto;
    }

    public CoupleTexteDTO toDTO(CoupleTexte entity) {
        if (entity == null) return null;
        CoupleTexteDTO dto = new CoupleTexteDTO();
        dto.setId(entity.getId());
        dto.setTexte1(entity.getTexte1());
        dto.setTexte2(entity.getTexte2());
        
        if (entity.getDataset() != null) {
            dto.setDatasetId(entity.getDataset().getId());
        }
        return dto;
    }

    public AnnotationDTO toDTO(Annotation entity) {
        if (entity == null) return null;
        AnnotationDTO dto = new AnnotationDTO();
        dto.setId(entity.getId());
        dto.setClasseChoisie(entity.getClasseChoisie());
        dto.setDateAnnotation(entity.getDateAnnotation());
        
        if (entity.getAnnotateur() != null) {
            dto.setAnnotateurId(entity.getAnnotateur().getId());
            dto.setAnnotateurNomComplet(entity.getAnnotateur().getPrenom() + " " + entity.getAnnotateur().getNom());
        }
        
        if (entity.getCoupleTexte() != null) {
            dto.setCoupleTexte(toDTO(entity.getCoupleTexte()));
        }
        return dto;
    }
}
