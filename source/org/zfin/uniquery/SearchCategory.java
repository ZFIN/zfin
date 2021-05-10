package org.zfin.uniquery;

import java.util.List;

/**
 * SearchCategory used in site search
 */
public class SearchCategory {

    private String id;
    private String displayName;
    private List<UrlPattern> urlPatterns;

    public SearchCategory(String id, String displayName, List<UrlPattern> urlPatterns) {
        this.id = id;
        this.displayName = displayName;
        this.urlPatterns = urlPatterns;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<UrlPattern> getUrlPatterns() {
        return urlPatterns;
    }

}
