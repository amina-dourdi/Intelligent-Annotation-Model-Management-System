package com.ensah.Core.model;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@DiscriminatorValue("Annotateur")
public class Annotateur extends Utilisateur {

    @OneToMany(mappedBy = "annotateur")
    private List<Tache> taches = new ArrayList<>();

    @OneToMany(mappedBy = "annotateur")
    private List<Annotation> annotations = new ArrayList<>();

}

