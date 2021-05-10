package org.zfin.search.presentation;

/**
 * A presentation class for representing Solr facet.query
 */
public class FacetQuery {
    String label;
    Integer count;
    String url;
    Boolean selected;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getSelected() { return selected; }

    public void setSelected(Boolean selected) { this.selected = selected; }
}
