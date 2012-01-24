package org.zfin.uniquery;

/**
 * Convenience class to hold link information
 */
public class UrlLink {

    private String linkUrl;
    private String referrer;

    public UrlLink(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public UrlLink(String linkUrl, String referrer) {
        this.linkUrl = linkUrl;
        this.referrer = referrer;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public String getReferrer() {
        return referrer;
    }

    @Override
    public String toString() {
        return "Url [referrer]" + linkUrl +
                " [" + referrer + ']';
    }
}
