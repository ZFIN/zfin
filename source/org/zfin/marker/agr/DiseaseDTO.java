package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.zfin.infrastructure.ActiveData;
import org.zfin.util.JsonDateSerializer;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiseaseDTO {

    @JsonProperty("DOid")
    private String doid;
    private String objectId;
    private String objectName;
    @JsonSerialize(using = JsonDateSerializer.class)
    private GregorianCalendar dateAssigned = new GregorianCalendar();
    private List<DataProviderDTO> dataProviderList;
    private EvidenceDTO evidence;
    private RelationshipDTO objectRelation;
    private List<ConditionRelationDTO> conditionRelations;

    public List<DataProviderDTO> getDataProviderList() {
        return dataProviderList;
    }

    public List<ConditionRelationDTO> getConditionRelations() {
        return conditionRelations;
    }


    private List<String> primaryGeneticEntityIDs;

    public void setConditionRelations(List<ConditionRelationDTO> conditionRelations) {
        this.conditionRelations = conditionRelations;
    }


    public List<String> getPrimaryGeneticEntityIDs() {
        return primaryGeneticEntityIDs;
    }

    public void setPrimaryGeneticEntityIDs(List<String> primaryGeneticEntityIDs) {
        this.primaryGeneticEntityIDs = primaryGeneticEntityIDs;
    }



    public String getDoid() {
        return doid;
    }

    public void setDoid(String doid) {
        this.doid = doid;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        if (ActiveData.validateActiveData(objectId))
            this.objectId = "ZFIN:";
        this.objectId += objectId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public RelationshipDTO getObjectRelation() {
        return objectRelation;
    }

    public void setObjectRelation(RelationshipDTO objectRelation) {
        this.objectRelation = objectRelation;
    }

    public List<DataProviderDTO> getDataProvider() {
        return dataProviderList;
    }

    public void setDataProvider(List<DataProviderDTO> dataProviderList) {
        this.dataProviderList = dataProviderList;
    }

    public EvidenceDTO getEvidence() {
        return evidence;
    }

    public void setEvidence(EvidenceDTO evidence) {
        this.evidence = evidence;
    }

    public GregorianCalendar getDateAssigned() {
        return dateAssigned;
    }

    public void setDateAssigned(GregorianCalendar dateAssigned) {
        this.dateAssigned = dateAssigned;
    }
}
