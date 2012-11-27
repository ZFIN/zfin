package org.zfin.uniquery;

/**
 * Convenience class to hold link information
 */
public class UrlLink implements Comparable<UrlLink>  {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UrlLink urlLink = (UrlLink) o;

        if (linkUrl != null ? !linkUrl.equals(urlLink.linkUrl) : urlLink.linkUrl != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return linkUrl != null ? linkUrl.hashCode() : 0;
    }


    @Override
    public int compareTo(UrlLink o) {
        return linkUrl.compareTo(o.getLinkUrl());
    }
}
