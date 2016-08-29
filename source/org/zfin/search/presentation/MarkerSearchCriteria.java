package org.zfin.search.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;

import java.util.List;

public class MarkerSearchCriteria {
    private String name;
    private String matchType;
    private String accession;
    private String selectedType;
    private String chromosome;
    private Integer limit;

    private List<MarkerSearchResult> results;
    private Long numFound;
    private Integer page;
    private Integer rows;
    private List<FacetField.Count> typesFound;
    private String displayType;
    private String baseUrl;

    private List<String> typeOptions;
    private List<String> chromosomeOptions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMatchType() { return matchType; }

    public void setMatchType(String matchType) { this.matchType = matchType; }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getSelectedType() {
        return selectedType;
    }

    public void setSelectedType(String selectedType) {
        this.selectedType = selectedType;
    }

    public List<String> getTypeOptions() {
        return typeOptions;
    }

    public void setTypeOptions(List<String> typeOptions) {
        this.typeOptions = typeOptions;
    }

    public List<String> getChromosomeOptions() {
        return chromosomeOptions;
    }

    public void setChromosomeOptions(List<String> chromosomeOptions) {
        this.chromosomeOptions = chromosomeOptions;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Long getNumFound() { return numFound; }

    public void setNumFound(Long numFound) { this.numFound = numFound; }

    public List<MarkerSearchResult> getResults() { return results; }

    public void setResults(List<MarkerSearchResult> results) { this.results = results; }

    public List<FacetField.Count> getTypesFound() { return typesFound; }

    public void setTypesFound(List<FacetField.Count> typesFound) { this.typesFound = typesFound; }

    public String getDisplayType() { return displayType; }

    public void setDisplayType(String displayType) { this.displayType = displayType; }

    public String getBaseUrl() { return baseUrl; }

    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Boolean isGenedomResult() {
        //we don't store the display name in the Marker.Type enum...maybe we should?
        if (StringUtils.equals(displayType, "Gene") || StringUtils.equals(displayType, "Pseudogene")) {
            return true;
        }
        return false;
    }

    public Boolean isCloneResult() {
        if (StringUtils.equals(displayType, "BAC")
            || StringUtils.equals(displayType, "PAC")
            || StringUtils.equals(displayType, "Fosmid")
            || StringUtils.equals(displayType, "STS")) {
            return true;
        }
        return false;
    }

    public Boolean isTranscriptResult() {
        if (StringUtils.equals(displayType, "Transcript")) {
            return true;
        }
        return false;
    }

    public Boolean isStrResult() {
        if (StringUtils.equals(displayType,"Morpholino")
            || StringUtils.equals(displayType,"CRISPR")
            || StringUtils.equals(displayType,"TALEN")) {
            return true;
        }
        return false;
    }

}
