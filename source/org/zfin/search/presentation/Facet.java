package org.zfin.search.presentation;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.zfin.search.service.SolrService;

import java.util.ArrayList;
import java.util.List;

public class Facet  {

    String name;
    List<FacetValue> selectedFacetValues;
    List<FacetValue> facetValues;
    List<FacetQuery> facetQueries;
    int nonEmptyDocumentCount;
    Boolean openByDefault;
    Boolean showAutocompleteBox;
    Boolean alwaysShowAllFacets;
    Boolean showIncludeExcludeIcons;

    public Facet(String name) {
        this.name = name;
        this.showAutocompleteBox = true;
        this.alwaysShowAllFacets = false;
        this.showIncludeExcludeIcons = true;
    }

    public List<FacetValue> getSelectedFacetValues() {
        return selectedFacetValues;
    }

    public void setSelectedFacetValues(List<FacetValue> selectedFacetValues) {
        this.selectedFacetValues = selectedFacetValues;
    }

    public List<FacetValue> getFacetValues() {
        return facetValues;
    }

    public void setFacetValues(List<FacetValue> facetValues) {
        this.facetValues = facetValues;
    }

    public int getNonEmptyDocumentCount() {
        return nonEmptyDocumentCount;
    }

    public void setNonEmptyDocumentCount(int nonEmptyDocumentCount) {
        this.nonEmptyDocumentCount = nonEmptyDocumentCount;
    }

    public Boolean getOpenByDefault() {
        return openByDefault;
    }

    public void setOpenByDefault(Boolean openByDefault) {
        this.openByDefault = openByDefault;
    }

    public Boolean getShowAutocompleteBox() {
        return showAutocompleteBox;
    }

    public void setShowAutocompleteBox(Boolean showAutocompleteBox) {
        this.showAutocompleteBox = showAutocompleteBox;
    }

    public Boolean getAlwaysShowAllFacets() {
        return alwaysShowAllFacets;
    }

    public void setAlwaysShowAllFacets(Boolean alwaysShowAllFacets) {
        this.alwaysShowAllFacets = alwaysShowAllFacets;
    }

    public String getLabel() {
        return SolrService.getPrettyFieldName(name);
    }

    public String getName() {
        return name;
    }

    public boolean getHasSelectedValues() {
        if (selectedFacetValues != null && selectedFacetValues.size() > 0)
            return true;
        return false;
    }

    public boolean getEmpty() {
        return (0 == CollectionUtils.size(selectedFacetValues) + CollectionUtils.size(facetValues));
    }

    public boolean getOpen() {
        return getHasSelectedValues() || openByDefault;
    }

    public Boolean getShowIncludeExcludeIcons() {
        return showIncludeExcludeIcons;
    }

    public void setShowIncludeExcludeIcons(Boolean showIncludeExcludeIcons) {
        this.showIncludeExcludeIcons = showIncludeExcludeIcons;
    }

    public List<FacetQuery> getFacetQueries() {
        return facetQueries;
    }

    public void setFacetQueries(List<FacetQuery> facetQueries) {
        this.facetQueries = facetQueries;
    }

    public void addFacetQuery(FacetQuery facetQuery) {
        if (facetQueries == null)
            facetQueries = new ArrayList<FacetQuery>();

        if (facetQuery != null)
            facetQueries.add(facetQuery);
    }
}
