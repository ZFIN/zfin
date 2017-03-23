package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiseaseDTO extends ZfinDTO {

    private String DOid;
    private String objectId;
    private String objectName;
    private RelationshipDTO objectRelation;
    private DataProvider dataProvider;
    private List<EvidenceDTO> evidence;
    private Set<ExperimentalConditionDTO> experimentalConditions;
    private Set<String> inferredGeneAssociation;

    public String getDOid() {
        return DOid;
    }

    public void setDOid(String DOid) {
        this.DOid = DOid;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
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

    public Set<String> getInferredGeneAssociation() {
        return inferredGeneAssociation;
    }

    public void setInferredGeneAssociation(Set<String> inferredGeneAssociation) {
        this.inferredGeneAssociation = inferredGeneAssociation;
    }

    public DataProvider getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    public List<EvidenceDTO> getEvidence() {
        return evidence;
    }

    public void setEvidence(List<EvidenceDTO> evidence) {
        this.evidence = evidence;
    }

    public Set<ExperimentalConditionDTO> getExperimentalConditions() {
        return experimentalConditions;
    }

    public void setExperimentalConditions(Set<ExperimentalConditionDTO> experimentalConditions) {
        this.experimentalConditions = experimentalConditions;
    }
}
