package org.zfin.uniquery;

import org.apache.commons.lang.StringUtils;

import java.net.URL;

/**
 * Convenience class to hold info for the indexer.
 */
public class WebPageSummary {

    private UrlLink urlLink;
    private URL url;
    private String body;
    private String text;
    private String title;
    // set this absolute url when indexing non-zfin sites, such as the wiki.
    private String urlName;
    // array of links on this page
    private String[] urls;
    private Type type;

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public UrlLink getUrlLink() {
        return urlLink;
    }

    public void setUrlLink(UrlLink urlLink) {
        this.urlLink = urlLink;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getUrls() {
        return urls;
    }

    public void setUrls(String[] urls) {
        this.urls = urls;
    }

    public String getUrlName() {
        if (urlName != null)
            return urlName;
        if (!StringUtils.isEmpty(url.getFile()))
            return url.getFile();
        else
            return url.toString();
    }

    public void setUrlName(String urlName) {
        this.urlName = urlName;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }


    enum Type {
        ZFIN, WIKI
    }
}
