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
public class ResourceDTO extends ZfinDTO {
    private String name;
    private String abbreviation;
    private String medAbbrev;
    private String isoAbbrev;
    private String publisher;
    private String printIssn;
    private String onlineIssn;
    private String nlmID;
    private List<String> aliases;
    private List<CrossReferenceDTO> crossReferences;




}
