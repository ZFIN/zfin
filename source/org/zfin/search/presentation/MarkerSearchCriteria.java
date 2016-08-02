package org.zfin.search.presentation;

import org.apache.solr.client.solrj.response.FacetField;

import java.util.List;

public class MarkerSearchCriteria {
    private String name;
    private String accession;
    private List<String> selectedTypes;
    private String chromosome;
    private Integer limit;

    private List<MarkerSearchResult> results;
    private Long numFound;
    private Integer page;
    private Integer rows;
    private List<FacetField.Count> typesFound;
    private String displayType;
    private String baseUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public List<String> getSelectedTypes() {
        return selectedTypes;
    }

    public void setSelectedTypes(List<String> selectedTypes) { this.selectedTypes = selectedTypes; }

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
}
