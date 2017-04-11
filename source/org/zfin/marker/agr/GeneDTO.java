package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.zfin.infrastructure.ActiveData;
import org.zfin.properties.ZfinPropertiesEnum;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneDTO extends ZfinDTO {

    private String symbol;
    private String name;
    private String primaryId;
    private String soTermId;
    private String geneLiteratureUrl;
    private static final String geneLiteratureUrlPrefix = "http://zfin.org/" + ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value() + "?MIval=aa-showpubs.apg";
    //private List<SynonymDTO> synonyms;
    private List<String> synonyms;
    private List<CrossReferenceDTO> crossReferences;
    private Set<String> secondaryIds;
    private Set<GenomeLocationDTO> genomeLocations;

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

    public String getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(String primaryId) {
        if (ActiveData.validateActiveData(primaryId))
            this.primaryId = ZFIN;
        this.primaryId += primaryId;
    }

    public String getSoTermId() {
        return soTermId;
    }

    public void setSoTermId(String soTermId) {
        this.soTermId = soTermId;
    }

/*
    public List<SynonymDTO> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<SynonymDTO> synonyms) {
        this.synonyms = synonyms;
    }
*/


    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    public List<CrossReferenceDTO> getCrossReferences() {
        return crossReferences;
    }

    public void setCrossReferences(List<CrossReferenceDTO> crossReferences) {
        this.crossReferences = crossReferences;
    }

    public Set<String> getSecondaryIds() {
        return secondaryIds;
    }

    public void setSecondaryIds(Set<String> secondaryIds) {
        if (secondaryIds == null)
            return;
        this.secondaryIds = secondaryIds.stream()
                .map((id) -> {
                    String modifiedId = "";
                    if (ActiveData.validateActiveData(id))
                        modifiedId = ZFIN;
                    modifiedId += id;
                    return modifiedId;
                })
                .collect(Collectors.toSet());
    }

    public Set<GenomeLocationDTO> getGenomeLocations() {
        return genomeLocations;
    }

    public void setGenomeLocations(Set<GenomeLocationDTO> genomeLocations) {
        this.genomeLocations = genomeLocations;
    }

    public String getGeneLiteratureUrl() {
        String returnString = geneLiteratureUrlPrefix;
        if (primaryId.startsWith(ZFIN))
            returnString += "&OID=" + primaryId.replace(ZFIN, "");
        returnString += "&name=" + name;
        returnString += "&abbrev=" + symbol;
        return returnString;
    }
}
