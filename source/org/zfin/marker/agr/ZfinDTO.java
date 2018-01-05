package org.zfin.marker.agr;

import org.zfin.infrastructure.ActiveData;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ZfinDTO {

    public static String taxonId = "NCBITaxon:7955";
    public static final String ZFIN = "ZFIN:";
    private List<String> synonyms;
    private Set<String> secondaryIds;
    private String primaryId;

    public String getTaxonId() {
        return taxonId;
    }
    public String getPrimaryId() {
        return primaryId;
    }

    public void setTaxonId(String taxonID) {
        this.taxonId = taxonID;
    }
    public void setPrimaryId(String primaryId) {
        if (ActiveData.validateActiveData(primaryId))
            this.primaryId = ZFIN;
        this.primaryId += primaryId;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
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

}
