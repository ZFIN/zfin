package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneDTO extends ZfinDTO {

    private String symbol;
    private String name;
    private String primaryId;
    private String soTermId;
    private static final String geneLiteratureUrlPrefix = "http://zfin.org/action/marker/citation-list/";
    //private List<SynonymDTO> synonyms;
    private List<String> synonyms;
    private List<CrossReferenceDTO> crossReferences;
    private Set<String> secondaryIds;
    private Set<GenomeLocationDTO> genomeLocations;
    private String geneLiteratureUrl;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getSoTermId() {
        return soTermId;
    }

    public void setSoTermId(String soTermId) {
        this.soTermId = soTermId;
    }

    public List<CrossReferenceDTO> getCrossReferences() {
        return crossReferences;
    }

    public void setCrossReferences(List<CrossReferenceDTO> crossReferences) {
        this.crossReferences = crossReferences;
    }

    public Set<GenomeLocationDTO> getGenomeLocations() {
        return genomeLocations;
    }

    public void setGenomeLocations(Set<GenomeLocationDTO> genomeLocations) {
        this.genomeLocations = genomeLocations;
    }

    //public String getGeneLiteratureUrl() {
    //    String returnString = geneLiteratureUrlPrefix;
    //    if (primaryId.startsWith(ZFIN))
    //        returnString += primaryId.replace(ZFIN, "");
    //    return returnString;
    //}
}
