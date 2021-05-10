package org.zfin.search.presentation;

import java.util.*;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.PivotField;

public class FacetValue {
    String label;
    Long count;
    Boolean selected;
    String url;
    String excludeUrl;
    List<FacetValue> childFacets;

    public FacetValue(FacetField.Count facetFieldCount, boolean selected, String url, String excludeUrl) {
        this.label = facetFieldCount.getName();
        this.count = facetFieldCount.getCount();
        this.selected = selected;
        this.url = url;
        this.excludeUrl = excludeUrl;
        this.childFacets = new ArrayList<>();
    }

    public FacetValue(PivotField pivotField, boolean selected, String url, String excludeUrl) {
        this.label = pivotField.getValue().toString();
        this.count = Integer.toUnsignedLong(pivotField.getCount());
        this.selected = selected;
        this.url = url;
        this.excludeUrl = excludeUrl;
        this.childFacets = new ArrayList<>();
    }

    public FacetValue(FacetField.Count facetFieldCount) {
        this.label = facetFieldCount.getName();
        this.count = facetFieldCount.getCount();
    }

    public String getLabel() {
        return label;
    }

    public Long getCount() {
        return count;
    }

    public String getUrl() {
        return url;
    }

    public String getExcludeUrl() {
        return excludeUrl;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) { this.selected = selected; }

    public void addChildFacet(FacetValue childFacet) {
        if (childFacets == null) { childFacets = new ArrayList<>(); }
        childFacets.add(childFacet);
    }

    public List<FacetValue> getChildFacets() {
        return childFacets;
    }

    public void setChildFacets(List<FacetValue> childFacets) {
        this.childFacets = childFacets;
    }
}
