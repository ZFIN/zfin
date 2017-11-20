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
    private Boolean explain;

    private List<String> typeOptions;
    private List<String> chromosomeOptions;

    private Boolean searchHappened;

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

    public Boolean getSearchHappened() {
        return searchHappened;
    }

    public void setSearchHappened(Boolean searchHappened) {
        this.searchHappened = searchHappened;
    }

    public Boolean getExplain() {
        return explain;
    }

    public void setExplain(Boolean explain) {
        this.explain = explain;
    }

    public Boolean isGenedomResult() {
        return StringUtils.equals(displayType, Marker.TypeGroup.SEARCHABLE_GENE.getDisplayName());
    }

    public Boolean isCloneResult() {
        return StringUtils.equals(displayType, Marker.TypeGroup.SEARCHABLE_CLONE.getDisplayName());
    }

    public Boolean isTranscriptResult() {
        return StringUtils.equals(displayType, Marker.TypeGroup.SEARCHABLE_TRANSCRIPT.getDisplayName());
    }

    public Boolean isStrResult() {
        return StringUtils.equals(displayType, Marker.TypeGroup.SEARCHABLE_STR.getDisplayName());
    }

}
