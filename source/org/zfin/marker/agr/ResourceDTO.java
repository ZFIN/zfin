package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import lombok.Getter;
import lombok.Setter;



@Getter
@Setter

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceDTO {
    private String primaryId;
    private String title;
    private String abbreviation;
    private String medlineAbbreviation;
    private String isoAbbreviation;
    private String publisher;
    private String printISSN;
    private String onlineISSN;
    private List<CrossReferenceDTO> crossReferences;




}
