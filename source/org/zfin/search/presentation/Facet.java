package org.zfin.search.presentation;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.zfin.search.service.SolrService;

import java.util.ArrayList;
import java.util.List;

@Data
public class Facet {

    String name;
    List<FacetValue> selectedFacetValues;
    List<FacetValue> facetValues;
    List<FacetQuery> facetQueries;
    int nonEmptyDocumentCount;
    Boolean openByDefault;
    Boolean showAutocompleteBox;
    Boolean alwaysShowAllFacets;
    Boolean showIncludeExcludeIcons;
    Boolean displayShowAllLink;
    Integer maxValuesToShow = 4;
    String showAllFieldName;

    public Facet(String name) {
        this.name = name;
        this.showAutocompleteBox = true;
        this.alwaysShowAllFacets = false;
        this.showIncludeExcludeIcons = true;
    }

    public String getLabel() {
        return SolrService.getPrettyFieldName(name);
    }

    public String getName() {
        return name;
    }

    public boolean getHasSelectedValues() {
        return selectedFacetValues != null && selectedFacetValues.size() > 0;
    }

    public boolean getEmpty() {
        return (0 == CollectionUtils.size(selectedFacetValues) + CollectionUtils.size(facetValues));
    }

    public boolean getOpen() {
        return getHasSelectedValues() || openByDefault;
    }

    public String getShowAllFieldName() {
        return showAllFieldName == null ? name : showAllFieldName;
    }

    public void addFacetQuery(FacetQuery facetQuery) {
        if (facetQueries == null)
            facetQueries = new ArrayList<>();

        if (facetQuery != null)
            facetQueries.add(facetQuery);
    }

}
