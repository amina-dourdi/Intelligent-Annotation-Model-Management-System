package com.ensah.Core.dtos;

public class AnnotateurDTO extends UtilisateurDTO {
    
    private int numberOfTaches;
    
    public AnnotateurDTO() {
        super();
    }
    
    public int getNumberOfTaches() {
        return numberOfTaches;
    }
    
    public void setNumberOfTaches(int numberOfTaches) {
        this.numberOfTaches = numberOfTaches;
    }
}
