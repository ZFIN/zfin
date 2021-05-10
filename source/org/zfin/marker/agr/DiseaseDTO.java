package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.zfin.infrastructure.ActiveData;
import org.zfin.util.JsonDateSerializer;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiseaseDTO {

    @JsonProperty("DOid")
    private String doid;
    private String objectId;
    private String objectName;
    @JsonSerialize(using = JsonDateSerializer.class)
    private GregorianCalendar dateAssigned = new GregorianCalendar();
    private List<DataProviderDTO> dataProvider;
    private EvidenceDTO evidence;
    private RelationshipDTO objectRelation;
    private List<ConditionRelationDTO> conditionRelations;

    private List<String> primaryGeneticEntityIDs;
}
