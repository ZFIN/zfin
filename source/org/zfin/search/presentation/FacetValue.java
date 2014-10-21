package org.zfin.search.presentation;


import org.apache.solr.client.solrj.response.FacetField;

public class FacetValue {
    FacetField.Count facetFieldCount;
    Boolean selected;
    String url;
    String excludeUrl;

    public FacetValue(FacetField.Count facetFieldCount, boolean selected, String url, String excludeUrl) {
        this.facetFieldCount = facetFieldCount;
        this.selected = selected;
        this.url = url;
        this.excludeUrl = excludeUrl;
    }

    public String getLabel() {
        return facetFieldCount.getName();
    }

    public Long getCount() {
        return facetFieldCount.getCount();
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

}
