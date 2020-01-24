package org.zfin.marker.agr;


import com.fasterxml.jackson.annotation.JsonInclude;
import org.zfin.infrastructure.ActiveData;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConstructDTO {

    private String name;
    protected List<String> synonyms;
    protected Set<String> secondaryIds;
    protected String primaryId;
    public static final String ZFIN = "ZFIN:";
    private List<ConstructComponentDTO> constructComponents;


    public List<ConstructComponentDTO> getConstructComponents() {
        return constructComponents;
    }

    public void setConstructComponents(List<ConstructComponentDTO> constructComponents) {
        this.constructComponents = constructComponents;
    }

    public String getName() {
        return name;
    }

    public String getPrimaryId() {
        return primaryId;
    }
    public void setName(String name) {
        this.name = name;
    }

    public static String getZFIN() {
        return ZFIN;
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


    public void setPrimaryId(String primaryId) {
        if (ActiveData.validateActiveData(primaryId))
            this.primaryId = ZFIN;
        this.primaryId += primaryId;
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
