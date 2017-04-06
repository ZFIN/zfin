package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.zfin.util.JsonDateSerializer;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiseaseDTO extends ZfinDTO {

    @JsonProperty("DOid")
    private String doid;
    private String objectId;
    private String objectName;
    @JsonSerialize(using = JsonDateSerializer.class)
    private GregorianCalendar dateAssigned = new GregorianCalendar();
    private DataProvider dataProvider;
    private List<EvidenceDTO> evidence;
    private Set<ExperimentalConditionDTO> experimentalConditions;
    private RelationshipDTO objectRelation;

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

    public GregorianCalendar getDateAssigned() {
        return dateAssigned;
    }

    public void setDateAssigned(GregorianCalendar dateAssigned) {
        this.dateAssigned = dateAssigned;
    }
}
