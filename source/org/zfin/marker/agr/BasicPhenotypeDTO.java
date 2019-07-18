package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.zfin.util.JsonDateSerializer;

import java.util.GregorianCalendar;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasicPhenotypeDTO  {

    private String objectId;
    private String pubMedId;
    private String pubModId;
    private PublicationAgrDTO evidence;
    private List<PhenotypeTermIdentifierDTO> phenotypeTermIdentifiers;
    private String phenotypeStatement;
    private List<String> primaryGeneticEntityIDs;

    public List<String> getPrimaryGeneticEntityIDs() {
        return primaryGeneticEntityIDs;
    }

    public void setPrimaryGeneticEntityIDs(List<String> primaryGeneticEntityIDs) {
        this.primaryGeneticEntityIDs = primaryGeneticEntityIDs;
    }

    @JsonSerialize(using = JsonDateSerializer.class)
    private GregorianCalendar dateAssigned = new GregorianCalendar();
    public PublicationAgrDTO getEvidence() {
        return evidence;
    }

    public void setEvidence(PublicationAgrDTO evidence) {
        this.evidence = evidence;
    }

    public GregorianCalendar getDateAssigned() {
        return dateAssigned;
    }

    public void setDateAssigned(GregorianCalendar dateAssigned) {
        this.dateAssigned = dateAssigned;
    }
    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }


    public List<PhenotypeTermIdentifierDTO> getPhenotypeTermIdentifiers() {
        return phenotypeTermIdentifiers;
    }

    public void setPhenotypeTermIdentifiers(List<PhenotypeTermIdentifierDTO> phenotypeTermIdentifiers) {
        this.phenotypeTermIdentifiers = phenotypeTermIdentifiers;
    }

    public String getPhenotypeStatement() {
        return phenotypeStatement;
    }

    public void setPhenotypeStatement(String phenotypeStatement) {
        this.phenotypeStatement = phenotypeStatement;
    }




}
