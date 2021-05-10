package org.zfin.search.presentation;


import java.util.ArrayList;
import java.util.List;

public class FacetGroup {
    String label;
    List<Facet> facets;
    List<FacetQuery> facetQueries;
    Boolean openByDefault;
    Boolean rootOnly;

    public FacetGroup(String label) {
        this.label = label;
        this.openByDefault = false;
    }

    public FacetGroup(String label, boolean openByDefault) {
        this.label = label;
        this.openByDefault = openByDefault;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Facet> getFacets() {
        return facets;
    }

    public void setFacets(List<Facet> facets) {
        this.facets = facets;
    }

    public void addFacet(Facet facet) {
        if (facets == null)
            facets = new ArrayList<>();

        if (facet != null)
            facets.add(facet);

    }

    public List<FacetQuery> getFacetQueries() {
        return facetQueries;
    }

    public void setFacetQueries(List<FacetQuery> facetQueries) {
        this.facetQueries = facetQueries;
    }

    public void addFacetQuery(FacetQuery facetQuery) {
        if (facetQueries == null)
            facetQueries = new ArrayList<>();

        if (facetQuery != null)
            facetQueries.add(facetQuery);
    }

    /* Sorry for the terrible name, just want to access it from a jsp... */
    public boolean getHasSelectedValues() {
        if (facets == null)
            return false;

        for (Facet facet : facets)
           if (facet.getHasSelectedValues())
              return true;

        return false;
    }

    public boolean getHasSelectedQueries() {
        if (facetQueries == null)
            return false;

        for (FacetQuery facetQuery : facetQueries) {
            if (facetQuery.getSelected())
                return true;
        }

        return false;
    }

    /* again, ghastly naming... sorry */
    public boolean getHide() {
        if (facets == null)
            return true;

        for (Facet facet : facets) {
            if (!facet.getEmpty())
                return false;
        }

        return true;
    }

    public boolean getOpen() {
        return openByDefault || getHasSelectedValues() || getHasSelectedQueries();
    }


    public Boolean getRootOnly() {
        return rootOnly;
    }

    public void setRootOnly(Boolean rootOnly) {
        this.rootOnly = rootOnly;
    }
}
