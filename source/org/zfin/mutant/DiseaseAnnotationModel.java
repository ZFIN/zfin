package org.zfin.mutant;

/**
 * Disease model entity:
 */
public class DiseaseAnnotationModel  {

    private long ID;
    private DiseaseAnnotation diseaseAnnotation;
    private FishExperiment fishExperiment;

    public FishExperiment getFishExperiment() {
        return fishExperiment;
    }

    public void setFishExperiment(FishExperiment fishExperiment) {
        this.fishExperiment = fishExperiment;
    }
    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public DiseaseAnnotation getDiseaseAnnotation() {
        return diseaseAnnotation;
    }

    public void setDiseaseAnnotation(DiseaseAnnotation diseaseAnnotation) {
        this.diseaseAnnotation = diseaseAnnotation;
    }

}