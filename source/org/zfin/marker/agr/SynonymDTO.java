package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SynonymDTO {

    private String synonym;
    private String synonymScope;
    private String synonymGroup;

    public SynonymDTO(String synonym) {
        this.synonym = synonym;
    }

    public String getSynonym() {
        return synonym;
    }

    public String getSynonymScope() {
        return synonymScope;
    }

    public void setSynonymScope(String synonymScope) {
        this.synonymScope = synonymScope;
    }

    public String getSynonymGroup() {
        return synonymGroup;
    }

    public void setSynonymGroup(String synonymGroup) {
        this.synonymGroup = synonymGroup;
    }
}
