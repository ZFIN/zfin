package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.alliancegenome.curation_api.model.entities.AGMDiseaseAnnotation;

import java.util.List;

@Getter
@Setter
public class AllDiseaseAnnotationMlDTO {

    private List<AGMDiseaseAnnotation> diseaseList;

}
