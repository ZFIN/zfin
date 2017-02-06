package org.zfin.marker.agr;

import java.util.ArrayList;
import java.util.List;

public class GeneDTO {

    private String symbol;
    private String name;
    private String primaryId;
    private String taxonId = "7955";
    private String soTermId;
    private List<String> synonyms = new ArrayList<>();
    private List<CrossReferenceDTO> crossReferences = new ArrayList<>();
    private List<String> secondaryIds = new ArrayList<>();

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
        this.primaryId = primaryId;
    }

    public String getTaxonId() {
        return taxonId;
    }

    public void setTaxonId(String taxonID) {
        this.taxonId = taxonID;
    }

    public String getSoTermId() {
        return soTermId;
    }

    public void setSoTermId(String soTermId) {
        this.soTermId = soTermId;
    }

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

    public List<String> getSecondaryIds() {
        return secondaryIds;
    }

    public void setSecondaryIds(List<String> secondaryIds) {
        this.secondaryIds = secondaryIds;
    }
}
