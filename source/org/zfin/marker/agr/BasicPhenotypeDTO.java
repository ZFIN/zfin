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
    private List<PhenotypeTermIdentifierDTO> phenotypeTermIdentifiers;
    private String phenotypeStatement;
    @JsonSerialize(using = JsonDateSerializer.class)
    private GregorianCalendar dateAssigned = new GregorianCalendar();

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getPubMedId() {
        return pubMedId;
    }

    public void setPubMedId(String pubMedId) {
        this.pubMedId = pubMedId;
    }

    public String getPubModId() {
        return pubModId;
    }

    public void setPubModId(String pubModId) {
        this.pubModId = pubModId;
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
