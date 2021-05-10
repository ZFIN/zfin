package org.zfin.mutant;

import org.zfin.infrastructure.EntityZdbID;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.Set;

/**
 * Disease model entity:
 */
public class DiseaseAnnotation implements EntityZdbID {

    private String zdbID;
    private GenericTerm disease;
    private Publication publication;
    private String evidenceCode;

    public List<DiseaseAnnotationModel> getDiseaseAnnotationModel() {
        return diseaseAnnotationModel;
    }

    public void setDiseaseAnnotationModel(List<DiseaseAnnotationModel> diseaseAnnotationModel) {
        this.diseaseAnnotationModel = diseaseAnnotationModel;
    }

    private List<DiseaseAnnotationModel> diseaseAnnotationModel;



    public String getEvidenceCode() {
        return evidenceCode;
    }

    public void setEvidenceCode(String evidenceCode) {
        this.evidenceCode = evidenceCode;
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
    public String getZdbID() {
        return zdbID;
    }

    @Override
    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
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


}
