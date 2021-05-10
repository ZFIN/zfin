package org.zfin.search.presentation;

import org.zfin.framework.presentation.LookupEntry;

/**
 * Converted to JSON for use in a facet autocompleter
 */
public class FacetLookupEntry extends LookupEntry {

    String count;
    String fq;


    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getFq() {
        return fq;
    }

    public void setFq(String fq) {
        this.fq = fq;
    }

}
