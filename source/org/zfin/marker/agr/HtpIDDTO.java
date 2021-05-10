package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HtpIDDTO {

    private String primaryId;
    private List<String> secondaryId;
    private List<CrossReferenceDTO> crossReferences;
    private CrossReferenceDTO preferredCrossReference;

}
