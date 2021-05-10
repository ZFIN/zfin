package org.zfin.mutant.presentation;

import org.zfin.mutant.FishExperiment;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.util.Collection;

public class DiseaseModelDisplay implements Comparable<DiseaseModelDisplay> {

    private GenericTerm disease;
    private FishExperiment experiment;
    private Collection<Publication> publications;

    public FishExperiment getExperiment() {
        return experiment;
    }

    public void setExperiment(FishExperiment experiment) {
        this.experiment = experiment;
    }

    public GenericTerm getDisease() {
        return disease;
    }

    public void setDisease(GenericTerm disease) {
        this.disease = disease;
    }

    public Collection<Publication> getPublications() {
        return publications;
    }

    public void setPublications(Collection<Publication> publications) {
        this.publications = publications;
    }

    @Override
    public int compareTo(DiseaseModelDisplay o) {
        return disease.getTermName().compareTo(o.getDisease().getTermName());
    }
}
