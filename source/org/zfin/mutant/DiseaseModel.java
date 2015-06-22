package org.zfin.mutant;

import org.zfin.infrastructure.EntityZdbID;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

/**
 * Disease model entity:
 */
public class DiseaseModel implements EntityZdbID {

    private long ID;
    private GenericTerm disease;
    private Publication publication;
    private String evidenceCode;
    private FishExperiment fishExperiment;

    public FishExperiment getFishExperiment() {
        return fishExperiment;
    }

    public void setFishExperiment(FishExperiment fishExperiment) {
        this.fishExperiment = fishExperiment;
    }

    public String getEvidenceCode() {
        return evidenceCode;
    }

    public void setEvidenceCode(String evidenceCode) {
        this.evidenceCode = evidenceCode;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public GenericTerm getDisease() {
        return disease;
    }

    public void setDisease(GenericTerm term) {
        this.disease = term;
    }

    @Override
    public String getAbbreviation() {
        return disease.getTermName();
    }

    @Override
    public String getAbbreviationOrder() {
        return disease.getTermName();
    }

    @Override
    public String getEntityType() {
        return "Disease Model";
    }

    @Override
    public String getEntityName() {
        return disease.getTermName();
    }

    @Override
    public String getZdbID() {
        return String.valueOf(ID);
    }

    @Override
    public void setZdbID(String zdbID) {

    }
}
