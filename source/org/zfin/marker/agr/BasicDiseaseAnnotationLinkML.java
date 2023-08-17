package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import org.alliancegenome.curation_api.model.ingest.dto.AGMDiseaseAnnotationDTO;

import java.util.List;

@Setter
@Getter
@JsonPropertyOrder({"linkMlVersion", "diseaseAgmIngest"})
public class BasicDiseaseAnnotationLinkML {

    @JsonProperty("disease_agm_ingest_set")
    List<AGMDiseaseAnnotationDTO> diseaseAgmIngest;

    @JsonProperty("linkml_version")
    String linkMlVersion;
}
